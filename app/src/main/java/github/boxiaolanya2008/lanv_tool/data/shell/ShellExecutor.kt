package github.boxiaolanya2008.lanv_tool.data.shell

import java.io.BufferedReader
import java.io.InputStreamReader

class ShellResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String
) {
    val isSuccess: Boolean get() = exitCode == 0
}

object ShellExecutor {

    fun execute(command: String): ShellResult {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val stdout = BufferedReader(InputStreamReader(process.inputStream)).readText().trim()
            val stderr = BufferedReader(InputStreamReader(process.errorStream)).readText().trim()
            val exitCode = process.waitFor()
            ShellResult(exitCode, stdout, stderr)
        } catch (e: Exception) {
            ShellResult(-1, "", e.message ?: "Unknown error")
        }
    }

    fun executeWithRoot(command: String): ShellResult {
        val suPaths = listOf(
            "su",
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/data/adb/ksu/bin/su",
            "/data/adb/magisk/su"
        )

        for (suPath in suPaths) {
            val result = try {
                val process = Runtime.getRuntime().exec(arrayOf(suPath, "-c", command))
                val stdout = BufferedReader(InputStreamReader(process.inputStream)).readText().trim()
                val stderr = BufferedReader(InputStreamReader(process.errorStream)).readText().trim()
                val exitCode = process.waitFor()
                ShellResult(exitCode, stdout, stderr)
            } catch (e: Exception) {
                continue
            }
            if (result.isSuccess) return result
        }

        return ShellResult(-1, "", "No su binary found")
    }

    fun checkRoot(): Boolean {
        val idResult = execute("id")
        if (idResult.isSuccess && idResult.stdout.contains("uid=0")) {
            return true
        }

        val suResult = executeWithRoot("id")
        if (suResult.isSuccess && suResult.stdout.contains("uid=0")) {
            return true
        }

        val whoResult = execute("whoami")
        if (whoResult.isSuccess && whoResult.stdout == "root") {
            return true
        }

        return false
    }
}
