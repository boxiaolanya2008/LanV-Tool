package github.boxiaolanya2008.lanv_tool.data.shell

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

class ShellResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String
) {
    val isSuccess: Boolean get() = exitCode == 0
}

object ShellExecutor {

    private var suPath: String? = null

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
        if (suPath != null) {
            val result = runSu(suPath!!, command)
            if (result.isSuccess) return result
        }

        val whichResult = execute("which su")
        if (whichResult.isSuccess && whichResult.stdout.isNotBlank()) {
            val path = whichResult.stdout.lines().first().trim()
            val result = runSu(path, command)
            if (result.isSuccess) {
                suPath = path
                return result
            }
        }

        val suPaths = listOf(
            "su",
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/vendor/bin/su",
            "/data/adb/ksu/bin/su",
            "/data/adb/ksu/su",
            "/data/adb/magisk/su",
            "/data/adb/apd/su",
            "/su/bin/su",
            "/system/sbin/su"
        )

        for (path in suPaths) {
            val result = runSu(path, command)
            if (result.isSuccess) {
                suPath = path
                return result
            }
        }

        return ShellResult(-1, "", "No su binary found")
    }

    private fun runSu(path: String, command: String): ShellResult {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf(path, "-c", command))
            val stdout = BufferedReader(InputStreamReader(process.inputStream)).readText().trim()
            val stderr = BufferedReader(InputStreamReader(process.errorStream)).readText().trim()
            val exitCode = process.waitFor()
            ShellResult(exitCode, stdout, stderr)
        } catch (e: Exception) {
            ShellResult(-1, "", e.message ?: "Failed")
        }
    }

    fun executeWithRootInteractive(command: String): ShellResult {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("$command\n")
            os.writeBytes("exit\n")
            os.flush()

            val stdout = BufferedReader(InputStreamReader(process.inputStream)).readText().trim()
            val stderr = BufferedReader(InputStreamReader(process.errorStream)).readText().trim()
            val exitCode = process.waitFor()
            ShellResult(exitCode, stdout, stderr)
        } catch (e: Exception) {
            ShellResult(-1, "", e.message ?: "Failed")
        }
    }

    fun checkRoot(): Boolean {
        val idResult = execute("id")
        if (idResult.isSuccess && idResult.stdout.contains("uid=0")) {
            return true
        }

        val whichResult = execute("which su")
        if (whichResult.isSuccess && whichResult.stdout.isNotBlank()) {
            return true
        }

        val suResult = executeWithRoot("id")
        if (suResult.isSuccess && suResult.stdout.contains("uid=0")) {
            return true
        }

        return false
    }
}
