package github.boxiaolanya2008.lanv_tool.data.model

data class AppInfo(
    val packageName: String,
    val appName: String
)

enum class FpsLevel(val label: String, val prefKey: String) {
    OFF("关闭", "key_prefs_set_pkg_state_off"),
    LOW("60Hz", "key_prefs_set_pkg_state_low"),
    MID("90Hz", "key_prefs_set_pkg_state_mid"),
    HIGH("120Hz", "key_prefs_set_pkg_state_high"),
    HIGH_166("165Hz", "key_prefs_set_pkg_state_high166"),
    ON("开启", "key_prefs_set_pkg_state_on"),
    HIDDEN("隐藏", "key_prefs_set_pkg_state_hidden");

    companion object {
        fun fromPrefKey(key: String): FpsLevel? = entries.find { it.prefKey == key }
    }
}

data class AppFpsState(
    val app: AppInfo,
    val fpsLevel: FpsLevel
)
