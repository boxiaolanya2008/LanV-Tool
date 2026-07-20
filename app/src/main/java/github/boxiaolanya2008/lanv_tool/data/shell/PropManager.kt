package github.boxiaolanya2008.lanv_tool.data.shell

object PropManager {

    fun getProp(key: String): String? {
        val result = ShellExecutor.executeWithRoot("getprop $key")
        if (!result.isSuccess || result.stdout.isBlank() || result.stdout == "null") {
            return null
        }
        return result.stdout
    }

    fun setProp(key: String, value: String): Boolean {
        return if (value.isEmpty()) {
            val result = ShellExecutor.executeWithRoot("setprop $key ''")
            result.isSuccess
        } else {
            val result = ShellExecutor.executeWithRoot("setprop $key \"$value\"")
            result.isSuccess
        }
    }
}
