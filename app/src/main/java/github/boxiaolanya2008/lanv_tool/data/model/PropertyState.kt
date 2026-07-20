package github.boxiaolanya2008.lanv_tool.data.model

data class PropertyState(
    val property: SystemProperty,
    val isAvailable: Boolean,
    val originalValue: String?,
    val currentValue: String?,
    val isModified: Boolean = false
)
