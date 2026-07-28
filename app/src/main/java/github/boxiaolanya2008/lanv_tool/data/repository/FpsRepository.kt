package github.boxiaolanya2008.lanv_tool.data.repository

import github.boxiaolanya2008.lanv_tool.data.model.AppFpsState
import github.boxiaolanya2008.lanv_tool.data.model.AppInfo
import github.boxiaolanya2008.lanv_tool.data.model.FpsLevel
import github.boxiaolanya2008.lanv_tool.data.shell.ShellExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface FpsRepository {
    suspend fun getInstalledApps(): Result<List<AppInfo>>
    suspend fun getFpsStates(): Result<List<AppFpsState>>
    suspend fun setFpsLevel(packageName: String, level: FpsLevel): Result<Unit>
    suspend fun removeApp(packageName: String): Result<Unit>
}

internal class FpsRepositoryImpl : FpsRepository {

    private val contentUri = "content://com.vivo.gamewatch.fpsprovider/manual_fps_set"

    override suspend fun getInstalledApps(): Result<List<AppInfo>> = withContext(Dispatchers.IO) {
        runCatching {
            val result = ShellExecutor.executeWithRoot("pm list packages -3")
            if (!result.isSuccess) throw IllegalStateException("Failed to get app list")

            result.stdout.lines()
                .filter { it.startsWith("package:") }
                .map { it.removePrefix("package:").trim() }
                .map { pkg -> AppInfo(pkg, pkg) }
                .sortedBy { it.appName.lowercase() }
        }
    }

    override suspend fun getFpsStates(): Result<List<AppFpsState>> = withContext(Dispatchers.IO) {
        runCatching {
            val queryResult = ShellExecutor.executeWithRoot(
                "content query --uri $contentUri --where \"update_type=0\""
            )
            if (!queryResult.isSuccess) return@runCatching emptyList()

            parseFpsStates(queryResult.stdout)
        }
    }

    override suspend fun setFpsLevel(packageName: String, level: FpsLevel): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val fpsValue = when (level) {
                FpsLevel.HIGH_166 -> 165
                FpsLevel.HIGH -> 120
                FpsLevel.MID -> 90
                FpsLevel.LOW -> 60
                else -> return@runCatching
            }

            val whereArg = "'{\"update_type\": 0}'"
            val cmd = "content update --uri $contentUri --where $whereArg " +
                "--bind packagename:s:$packageName " +
                "--bind state:i:$fpsValue"

            val result = ShellExecutor.executeWithRoot(cmd)
            if (!result.isSuccess) throw IllegalStateException("Failed to set FPS: ${result.stderr}")
        }
    }

    override suspend fun removeApp(packageName: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val whereArg = "'{\"update_type\": 0, \"packagename\": \"$packageName\"}'"
            val cmd = "content delete --uri $contentUri --where $whereArg"

            val result = ShellExecutor.executeWithRoot(cmd)
            if (!result.isSuccess) throw IllegalStateException("Failed to remove app")
            Unit
        }
    }

    private fun parseFpsStates(queryOutput: String): List<AppFpsState> {
        val appStates = mutableListOf<AppFpsState>()

        val rowRegex = Regex("Row: \\d+ packagename=([^,]+), state=(\\d+)")
        rowRegex.findAll(queryOutput).forEach { match ->
            val pkg = match.groupValues[1]
            val state = match.groupValues[2].toIntOrNull() ?: return@forEach

            val level = when (state) {
                165 -> FpsLevel.HIGH_166
                120 -> FpsLevel.HIGH
                90 -> FpsLevel.MID
                60 -> FpsLevel.LOW
                else -> return@forEach
            }

            appStates.add(AppFpsState(AppInfo(pkg, pkg), level))
        }

        return appStates.sortedBy { it.app.appName }
    }
}
