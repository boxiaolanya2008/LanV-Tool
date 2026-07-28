package github.boxiaolanya2008.lanv_tool.data.model

sealed class PropertyType {
    data class Selection(val options: List<String>, val allowEmpty: Boolean = true, val allowCustom: Boolean = false) : PropertyType()
    data class Range(val min: Int, val max: Int, val step: Int = 1) : PropertyType()
}

data class SystemProperty(
    val key: String,
    val displayName: String,
    val description: String,
    val type: PropertyType,
    val category: PropertyCategory
)

enum class PropertyCategory(val label: String) {
    MEMORY("内存管理"),
    DISPLAY("显示设置"),
    CHARGING("充电设置"),
    GAME("游戏设置"),
    NETWORK("网络设置"),
    PERFORMANCE("性能设置")
}

object PropertyRegistry {

    val properties: List<SystemProperty> = listOf(
        SystemProperty(
            key = "persist.vendor.vivo.zramwb.size",
            displayName = "内存融合",
            description = "设置内存融合大小（单位 MB）",
            type = PropertyType.Selection(listOf("8192", "12288", "16384"), allowEmpty = true),
            category = PropertyCategory.MEMORY
        ),
        SystemProperty(
            key = "persist.sys.promotion_supported",
            displayName = "UltraMotion灵动帧率",
            description = "无缝变帧和画面运动感知，自动调整刷新率",
            type = PropertyType.Selection(listOf("true"), allowEmpty = true),
            category = PropertyCategory.MEMORY
        ),
        SystemProperty(
            key = "persist.vivo.displayic.software",
            displayName = "显示 蓝图影像芯片 版本",
            description = "蓝图影像芯片版本（0=自动 1=V2 2=V3）",
            type = PropertyType.Selection(listOf("0", "1", "2"), allowEmpty = true),
            category = PropertyCategory.DISPLAY
        ),
        SystemProperty(
            key = "persist.vivo.imagingic.ver",
            displayName = "蓝图自研影像双芯版本",
            description = "蓝图自研影像双芯版本",
            type = PropertyType.Selection(listOf("GC1_GC1.0_V4.0", "GC1_GC1.0_V5.0"), allowEmpty = true),
            category = PropertyCategory.DISPLAY
        ),
        SystemProperty(
            key = "persist.vivo.predict_chg_time",
            displayName = "充电时间预测",
            description = "显示充满电所需时间的预测",
            type = PropertyType.Selection(listOf("0", "1"), allowEmpty = true),
            category = PropertyCategory.CHARGING
        ),
        SystemProperty(
            key = "persist.vivo.charging_power_max",
            displayName = "激活200W充电（UI显示）",
            description = "激活200W充电UI（范围 1-200）",
            type = PropertyType.Range(min = 1, max = 200, step = 1),
            category = PropertyCategory.CHARGING
        ),
        SystemProperty(
            key = "persist.vivo.displayic.ver",
            displayName = "显示 Q芯片 版本",
            description = "显示 Q芯片 版本（可切换 Q 1.0/2.0/3.0）",
            type = PropertyType.Selection(listOf(
                "iQOO0301_DPIC2.0_Q1.0",
                "iQOO0301_DPIC2.0_Q2.0",
                "iQOO0301_DPIC2.0_Q3.0"
            ), allowEmpty = true),
            category = PropertyCategory.DISPLAY
        ),
        SystemProperty(
            key = "ro.vivo.gamebox.live.support",
            displayName = "主播模式",
            description = "游戏盒子直播推流功能（0=关闭 1=开启）",
            type = PropertyType.Selection(listOf("0", "1"), allowEmpty = true),
            category = PropertyCategory.GAME
        ),
        SystemProperty(
            key = "persist.vivo.game_bypass_charge_support",
            displayName = "旁路充电",
            description = "游戏时旁路充电，减少发热（0=关闭 1=开启）",
            type = PropertyType.Selection(listOf("0", "1"), allowEmpty = true),
            category = PropertyCategory.GAME
        ),
        SystemProperty(
            key = "sys.game.vaa.gles.enable",
            displayName = "游戏画面防撕裂（GLES）",
            description = "OpenGL ES 渲染下的画面防撕裂（0=关闭 1=开启）",
            type = PropertyType.Selection(listOf("0", "1"), allowEmpty = true),
            category = PropertyCategory.GAME
        ),
        SystemProperty(
            key = "sys.game.vaa.vivo.enable",
            displayName = "游戏画面防撕裂（vivo）",
            description = "vivo 自研渲染下的画面防撕裂（0=关闭 1=开启）",
            type = PropertyType.Selection(listOf("0", "1"), allowEmpty = true),
            category = PropertyCategory.GAME
        ),
        SystemProperty(
            key = "sys.game.vaa.vulkan.enable",
            displayName = "游戏画面防撕裂（Vulkan）",
            description = "Vulkan 渲染下的画面防撕裂（0=关闭 1=开启）",
            type = PropertyType.Selection(listOf("0", "1"), allowEmpty = true),
            category = PropertyCategory.GAME
        ),
        SystemProperty(
            key = "sys.game.vsr.pp.enable",
            displayName = "游戏超分画质增强",
            description = "游戏超分辨率画质增强处理（0=关闭 1=开启）",
            type = PropertyType.Selection(listOf("0", "1"), allowEmpty = true),
            category = PropertyCategory.GAME
        ),
        SystemProperty(
            key = "sys.game.xpq.gles.enable",
            displayName = "游戏插帧（GLES）",
            description = "OpenGL ES 渲染下的游戏插帧补帧（0=关闭 1=开启）",
            type = PropertyType.Selection(listOf("0", "1"), allowEmpty = true),
            category = PropertyCategory.GAME
        ),
        SystemProperty(
            key = "sys.game.xpq.vivo.enable",
            displayName = "游戏插帧（vivo）",
            description = "vivo 自研渲染下的游戏插帧补帧（0=关闭 1=开启）",
            type = PropertyType.Selection(listOf("0", "1"), allowEmpty = true),
            category = PropertyCategory.GAME
        ),
        SystemProperty(
            key = "sys.game.xpq.vulkan.enable",
            displayName = "游戏插帧（Vulkan）",
            description = "Vulkan 渲染下的游戏插帧补帧（0=关闭 1=开启）",
            type = PropertyType.Selection(listOf("0", "1"), allowEmpty = true),
            category = PropertyCategory.GAME
        ),
        SystemProperty(
            key = "persist.sys.dc1p_supported_fps",
            displayName = "DC1P支持帧率",
            description = "DC1P支持的刷新率模式（60Hz/120Hz）",
            type = PropertyType.Selection(listOf("60", "120"), allowEmpty = true, allowCustom = true),
            category = PropertyCategory.DISPLAY
        ),
        SystemProperty(
            key = "persist.sys.private_fps",
            displayName = "私有帧率",
            description = "私有刷新率模式（144Hz）",
            type = PropertyType.Selection(listOf("144"), allowEmpty = true, allowCustom = true),
            category = PropertyCategory.DISPLAY
        ),
        SystemProperty(
            key = "persist.vivo.gamecube.support_fps",
            displayName = "游戏魔方帧率支持",
            description = "游戏魔方帧率支持模式",
            type = PropertyType.Selection(listOf("1"), allowEmpty = true, allowCustom = true),
            category = PropertyCategory.GAME
        ),
        SystemProperty(
            key = "persist.vivo.phone.fps_default",
            displayName = "默认帧率模式",
            description = "手机默认刷新率模式",
            type = PropertyType.Selection(listOf("1"), allowEmpty = true, allowCustom = true),
            category = PropertyCategory.DISPLAY
        ),
        SystemProperty(
            key = "persist.vivo.phone.fps_max",
            displayName = "最大帧率",
            description = "手机支持的最大刷新率（120Hz）",
            type = PropertyType.Selection(listOf("120"), allowEmpty = true, allowCustom = true),
            category = PropertyCategory.DISPLAY
        ),
        SystemProperty(
            key = "persist.rms.big_data",
            displayName = "RMS大数据",
            description = "关闭RMS大数据收集（false=关闭 true=开启）",
            type = PropertyType.Selection(listOf("false", "true"), allowEmpty = true),
            category = PropertyCategory.PERFORMANCE
        ),
        SystemProperty(
            key = "persist.vivo.charge_capacity_up_limit_support",
            displayName = "充电限制（UI显示）",
            description = "充电限制功能开关（0=关闭 1=开启）",
            type = PropertyType.Selection(listOf("0", "1"), allowEmpty = true),
            category = PropertyCategory.CHARGING
        ),
        SystemProperty(
            key = "persist.vivo.battery_charge_limit",
            displayName = "自定义充电上限",
            description = "设置电池充电上限百分比（范围 1-100）",
            type = PropertyType.Range(min = 1, max = 100, step = 1),
            category = PropertyCategory.CHARGING
        ),
        SystemProperty(
            key = "persist.temperature.limit.counts",
            displayName = "关闭限频",
            description = "温度限频阈值（数值越大越不容易限频）",
            type = PropertyType.Selection(listOf("9999999", "0", "1"), allowEmpty = true),
            category = PropertyCategory.PERFORMANCE
        ),
        SystemProperty(
            key = "persist.vivo.support.triplewifi",
            displayName = "三WiFi（UI显示）",
            description = "三WiFi加速功能（yes=开启 no=关闭）",
            type = PropertyType.Selection(listOf("yes", "no"), allowEmpty = true),
            category = PropertyCategory.NETWORK
        ),
        SystemProperty(
            key = "ro.boot.vivo.satellite.support",
            displayName = "卫星通信（UI显示）",
            description = "卫星通信功能开关（0=关闭 1=开启）",
            type = PropertyType.Selection(listOf("0", "1"), allowEmpty = true),
            category = PropertyCategory.NETWORK
        )
    )

    fun getByKey(key: String): SystemProperty? = properties.find { it.key == key }
}
