package github.boxiaolanya2008.lanv_tool.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import github.boxiaolanya2008.lanv_tool.data.model.PropertyState
import github.boxiaolanya2008.lanv_tool.data.model.PropertyType

@Composable
fun PropertyToggleItem(
    state: PropertyState,
    isApplying: Boolean,
    onApply: (String) -> Unit,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isModified = state.isModified
    val isEmpty = state.currentValue?.isEmpty() == true && state.originalValue != null
    val isEnabled = isModified || isEmpty

    val containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        when (val type = state.property.type) {
            is PropertyType.Selection -> {
                if (type.options.size == 2 && type.options.contains("0") && type.options.contains("1")) {
                    BinaryToggleContent(state, isApplying, onApply, onRestore)
                } else if (type.options.size == 1 && type.options.contains("true")) {
                    BinaryToggleContent(state, isApplying, onApply, onRestore)
                } else {
                    MultiOptionContent(state, type, isApplying, onApply, onRestore)
                }
            }
            is PropertyType.Range -> {
                RangeSliderContent(state, type, isApplying, onApply)
            }
        }
    }
}

@Composable
private fun BinaryToggleContent(
    state: PropertyState,
    isApplying: Boolean,
    onApply: (String) -> Unit,
    onRestore: () -> Unit
) {
    val isOn = state.currentValue == "1" || state.currentValue == "true"
    val isModified = state.isModified
    val tag = getPropertyTag(state.property.key)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = state.property.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (tag != null) {
                    PropertyTag(text = tag)
                    if (isModified) Spacer(modifier = Modifier.width(6.dp))
                }
                if (isModified) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = state.property.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        if (isApplying) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Switch(
                checked = isOn,
                onCheckedChange = { checked ->
                    val newValue = if (state.property.type is PropertyType.Selection &&
                        state.property.type.options.contains("true")
                    ) {
                        if (checked) "true" else ""
                    } else {
                        if (checked) "1" else "0"
                    }
                    onApply(newValue)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
private fun MultiOptionContent(
    state: PropertyState,
    type: PropertyType.Selection,
    isApplying: Boolean,
    onApply: (String) -> Unit,
    onRestore: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val isModified = state.isModified
    val tag = getPropertyTag(state.property.key)
    val displayValue = when {
        state.currentValue?.isEmpty() == true -> "已清空"
        state.currentValue != null -> state.currentValue
        else -> "未设置"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isApplying) { showDialog = true }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = state.property.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (tag != null) {
                    PropertyTag(text = tag)
                    if (isModified) Spacer(modifier = Modifier.width(6.dp))
                }
                if (isModified) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = state.property.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isModified) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.clickable(enabled = !isApplying) { showDialog = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayValue,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = if (isModified) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showDialog) {
        MultiOptionDialog(
            state = state,
            options = type.options,
            allowEmpty = type.allowEmpty,
            allowCustom = type.allowCustom,
            onDismiss = { showDialog = false },
            onSelect = { value ->
                showDialog = false
                onApply(value)
            }
        )
    }
}

@Composable
private fun MultiOptionDialog(
    state: PropertyState,
    options: List<String>,
    allowEmpty: Boolean,
    allowCustom: Boolean,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var showCustomInput by remember { mutableStateOf(false) }
    var customValue by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = state.property.displayName,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                Text(
                    text = state.property.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                options.forEach { option ->
                    val isSelected = state.currentValue == option
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge,
                                fontFamily = FontFamily.Monospace,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (allowCustom) {
                    if (!showCustomInput) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCustomInput = true },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "自定义值...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        val formatHint = if (state.property.key == "persist.sys.dc1p_supported_fps") {
                            "格式: 60|120 或 144|144"
                        } else null
                        OutlinedTextField(
                            value = customValue,
                            onValueChange = { customValue = it },
                            label = { Text("自定义值") },
                            supportingText = if (formatHint != null) {
                                { Text(formatHint) }
                            } else null,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showCustomInput = false }) {
                                Text("取消")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = {
                                    if (customValue.isNotBlank()) {
                                        onSelect(customValue.trim())
                                    }
                                },
                                enabled = customValue.isNotBlank()
                            ) {
                                Text("应用")
                            }
                        }
                    }
                }

                if (allowEmpty) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val isEmptySelected = state.currentValue?.isEmpty() == true
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect("") },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isEmptySelected) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "空值（禁用）",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isEmptySelected) MaterialTheme.colorScheme.onErrorContainer
                                else MaterialTheme.colorScheme.error
                            )
                            if (isEmptySelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun RangeSliderContent(
    state: PropertyState,
    type: PropertyType.Range,
    isApplying: Boolean,
    onApply: (String) -> Unit
) {
    var sliderValue by remember(state.currentValue) {
        mutableFloatStateOf(state.currentValue?.toFloatOrNull() ?: type.min.toFloat())
    }
    val isModified = state.isModified
    val tag = getPropertyTag(state.property.key)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.property.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (tag != null) {
                        PropertyTag(text = tag)
                        if (isModified) Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (isModified) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = state.property.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isModified) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "${sliderValue.toInt()}W",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isModified) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${type.min}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = { onApply(sliderValue.toInt().toString()) },
                valueRange = type.min.toFloat()..type.max.toFloat(),
                steps = ((type.max - type.min) / type.step) - 1,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                enabled = !isApplying,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
            Text(
                text = "${type.max}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PropertyTag(text: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

private fun getPropertyTag(key: String): String? = when (key) {
    "persist.vendor.vivo.zramwb.size" -> "内存融合"
    "persist.vivo.charging_power_max" -> "UI显示"
    "persist.vivo.displayic.software" -> "UI显示"
    "persist.vivo.imagingic.ver" -> "UI显示"
    "persist.vivo.displayic.ver" -> "UI显示"
    "persist.vivo.predict_chg_time" -> "UI显示"
    "persist.sys.promotion_supported" -> "UI显示"
    "persist.sys.dc1p_supported_fps" -> "帧率"
    "persist.sys.private_fps" -> "帧率"
    "persist.vivo.gamecube.support_fps" -> "帧率"
    "persist.vivo.phone.fps_default" -> "帧率"
    "persist.vivo.phone.fps_max" -> "帧率"
    "persist.rms.big_data" -> "大数据"
    "persist.vivo.charge_capacity_up_limit_support" -> "UI显示"
    "persist.vivo.battery_charge_limit" -> "充电"
    "persist.temperature.limit.counts" -> "性能"
    "persist.vivo.support.triplewifi" -> "UI显示"
    "ro.boot.vivo.satellite.support" -> "UI显示"
    else -> null
}
