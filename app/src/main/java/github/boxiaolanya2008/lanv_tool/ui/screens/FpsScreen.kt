package github.boxiaolanya2008.lanv_tool.ui.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import github.boxiaolanya2008.lanv_tool.data.model.AppInfo
import github.boxiaolanya2008.lanv_tool.data.model.FpsLevel
import github.boxiaolanya2008.lanv_tool.ui.viewmodel.FpsAction
import github.boxiaolanya2008.lanv_tool.ui.viewmodel.FpsUiState
import github.boxiaolanya2008.lanv_tool.ui.viewmodel.FpsViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun FpsRoute(
    viewModel: FpsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("lanv_tool_fps", Context.MODE_PRIVATE) }
    var showWarning by remember { mutableStateOf(!prefs.getBoolean("fps_warning_shown", false)) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onAction(FpsAction.ClearMessage)
        }
    }

    if (showWarning) {
        FpsWarningDialog(
            onDismiss = {
                prefs.edit().putBoolean("fps_warning_shown", true).apply()
                showWarning = false
            }
        )
    }

    FpsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack
    )
}

@androidx.compose.material3.ExperimentalMaterial3Api
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@androidx.compose.runtime.Composable
internal fun FpsScreen(
    state: FpsUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (FpsAction) -> Unit,
    onNavigateBack: () -> Unit
) {
    var showAppDialog by remember { mutableStateOf<FpsLevel?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("自定义应用FPS") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when (state) {
            FpsUiState.Loading -> FpsLoadingContent(paddingValues)
            is FpsUiState.Error -> FpsErrorContent(
                paddingValues = paddingValues,
                message = state.message,
                onRetry = { onAction(FpsAction.LoadData) }
            )
            is FpsUiState.Success -> FpsMainContent(
                paddingValues = paddingValues,
                state = state,
                onSelectLevel = { level -> showAppDialog = level }
            )
        }
    }

    showAppDialog?.let { level ->
        AppSelectionDialog(
            level = level,
            state = state as? FpsUiState.Success,
            onDismiss = { showAppDialog = null },
            onSelectApp = { app ->
                onAction(FpsAction.SetFps(app.packageName, level))
                showAppDialog = null
            }
        )
    }
}

@Composable
private fun FpsLoadingContent(paddingValues: PaddingValues) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
private fun FpsErrorContent(
    paddingValues: PaddingValues,
    message: String,
    onRetry: () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onRetry) {
                Text("重试")
            }
        }
    }
}

@Composable
private fun FpsMainContent(
    paddingValues: PaddingValues,
    state: FpsUiState.Success,
    onSelectLevel: (FpsLevel) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            FpsLevelCard(
                label = "165Hz",
                desc = "极致流畅 耗电拉满",
                count = state.appStates.count { it.fpsLevel == FpsLevel.HIGH_166 },
                onClick = { onSelectLevel(FpsLevel.HIGH_166) }
            )
        }
        item {
            FpsLevelCard(
                label = "120Hz",
                desc = "丝滑流畅 推荐日常",
                count = state.appStates.count { it.fpsLevel == FpsLevel.HIGH },
                onClick = { onSelectLevel(FpsLevel.HIGH) }
            )
        }
        item {
            FpsLevelCard(
                label = "90Hz",
                desc = "够用省电 游戏党福利",
                count = state.appStates.count { it.fpsLevel == FpsLevel.MID },
                onClick = { onSelectLevel(FpsLevel.MID) }
            )
        }
        item {
            FpsLevelCard(
                label = "60Hz",
                desc = "最省电 肉眼可见掉帧",
                count = state.appStates.count { it.fpsLevel == FpsLevel.LOW },
                onClick = { onSelectLevel(FpsLevel.LOW) }
            )
        }
    }
}

@Composable
private fun FpsLevelCard(
    label: String,
    desc: String,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (count > 0) {
                Text(
                    text = "${count}个应用",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun AppSelectionDialog(
    level: FpsLevel,
    state: FpsUiState.Success?,
    onDismiss: () -> Unit,
    onSelectApp: (AppInfo) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSystemApps by remember { mutableStateOf(false) }
    var showHidden by remember { mutableStateOf(false) }

    val allApps = state?.installedApps ?: emptyList()
    val currentPkgs = state?.appStates?.filter { it.fpsLevel == level }?.map { it.app.packageName }?.toSet() ?: emptySet()

    val filteredApps = allApps.filter { app ->
        val matchesSearch = searchQuery.isBlank() || app.appName.contains(searchQuery, ignoreCase = true) || app.packageName.contains(searchQuery, ignoreCase = true)
        val matchesType = if (showSystemApps) app.packageName.startsWith("com.android.") || app.packageName.startsWith("com.vivo.") || app.packageName.startsWith("com.bbk.") else true
        matchesSearch && matchesType
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = when (level) {
                        FpsLevel.HIGH_166 -> "选择 165Hz 应用"
                        FpsLevel.HIGH -> "选择 120Hz 应用"
                        FpsLevel.MID -> "选择 90Hz 应用"
                        FpsLevel.LOW -> "选择 60Hz 应用"
                        else -> "选择应用"
                    },
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("搜索应用") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !showSystemApps,
                        onClick = { showSystemApps = false },
                        label = { Text("用户应用") }
                    )
                    FilterChip(
                        selected = showSystemApps,
                        onClick = { showSystemApps = true },
                        label = { Text("系统应用") }
                    )
                    FilterChip(
                        selected = showHidden,
                        onClick = { showHidden = !showHidden },
                        label = { Text("隐藏") }
                    )
                }
            }
        },
        text = {
            if (filteredApps.isEmpty()) {
                Text(
                    text = "未找到应用",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { app ->
                        val isSelected = app.packageName in currentPkgs
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectApp(app) }
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Apps,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.appName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = app.packageName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
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
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun FpsWarningDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "⚠️ 警告",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                MarkdownText(
                    markdown = """
> **这是实验性功能，请自行测试是否生效！**

---

### 注意事项

- 部分刷新率需要**设备硬件支持**才能生效
- 设置后建议**重启应用**或**重启手机**验证效果
- 如果设置无效，可能是系统限制或设备不支持
- **165Hz** 和 **120Hz** 需要屏幕硬件支持高刷新率
- **90Hz** 和 **60Hz** 大多数设备都支持

---

### 刷新率说明

| 刷新率 | 流畅度 | 耗电 |
|--------|--------|------|
| 165Hz | ⭐⭐⭐⭐⭐ | 极高 |
| 120Hz | ⭐⭐⭐⭐ | 高 |
| 90Hz | ⭐⭐⭐ | 中等 |
| 60Hz | ⭐⭐ | 低 |

---

**请谨慎使用，后果自负。**
                    """.trimIndent()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("我已了解")
            }
        }
    )
}

@Composable
private fun MarkdownText(markdown: String) {
    val lines = markdown.lines()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        lines.forEach { line ->
            when {
                line.startsWith("> ") -> {
                    val text = line.removePrefix("> ")
                    MarkdownTextLine(text, isQuote = true)
                }
                line.startsWith("### ") -> {
                    Text(
                        text = line.removePrefix("### "),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                line.startsWith("---") -> {
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.material3.HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                line.startsWith("|") -> {
                    MarkdownTableLine(line)
                }
                line.isBlank() -> {
                    Spacer(modifier = Modifier.height(2.dp))
                }
                else -> {
                    MarkdownTextLine(line)
                }
            }
        }
    }
}

@Composable
private fun MarkdownTextLine(text: String, isQuote: Boolean = false) {
    val annotatedString = buildAnnotatedString {
        var remaining = text
        while (remaining.isNotEmpty()) {
            val boldStart = remaining.indexOf("**")
            if (boldStart == -1) {
                append(remaining)
                break
            }
            append(remaining.substring(0, boldStart))
            val boldEnd = remaining.indexOf("**", boldStart + 2)
            if (boldEnd == -1) {
                append(remaining.substring(boldStart))
                break
            }
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(remaining.substring(boldStart + 2, boldEnd))
            }
            remaining = remaining.substring(boldEnd + 2)
        }
    }

    Text(
        text = annotatedString,
        style = if (isQuote) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
        color = if (isQuote) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
        modifier = if (isQuote) Modifier.padding(start = 8.dp) else Modifier
    )
}

@Composable
private fun MarkdownTableLine(line: String) {
    val cells = line.split("|").filter { it.isNotBlank() }.map { it.trim() }
    if (cells.size >= 3 && cells[0].matches(Regex("^[:\\-]+$"))) return

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        cells.forEachIndexed { index, cell ->
            Text(
                text = cell,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (index == 0) FontWeight.Medium else FontWeight.Normal,
                modifier = Modifier.weight(1f),
                textAlign = if (index == 0) TextAlign.Start else TextAlign.Center
            )
        }
    }
}
