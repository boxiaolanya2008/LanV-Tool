package github.boxiaolanya2008.lanv_tool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import github.boxiaolanya2008.lanv_tool.app.LanVToolApp
import github.boxiaolanya2008.lanv_tool.data.model.PropertyCategory
import github.boxiaolanya2008.lanv_tool.ui.screens.CategoryDetailRoute
import github.boxiaolanya2008.lanv_tool.ui.screens.FpsRoute
import github.boxiaolanya2008.lanv_tool.ui.screens.PropertyRoute
import github.boxiaolanya2008.lanv_tool.ui.screens.settings.SettingsRoute
import github.boxiaolanya2008.lanv_tool.ui.theme.LanVToolTheme
import github.boxiaolanya2008.lanv_tool.ui.viewmodel.FpsViewModel
import github.boxiaolanya2008.lanv_tool.ui.viewmodel.PropertyViewModel
import github.boxiaolanya2008.lanv_tool.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        LanVToolApp.instance = this.application as LanVToolApp
        setContent {
            LanVToolTheme {
                PrivacyGate()
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PrivacyGate() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("lanv_tool_privacy", Context.MODE_PRIVATE) }
    var showPrivacy by remember { mutableStateOf(!prefs.getBoolean("privacy_accepted", false)) }

    if (showPrivacy) {
        PrivacyDialog(
            onAccept = {
                prefs.edit().putBoolean("privacy_accepted", true).apply()
                showPrivacy = false
            },
            onReject = {
                (context as? ComponentActivity)?.finish()
            }
        )
    } else {
        AppNavigation()
    }
}

sealed interface Screen {
    data object CategoryList : Screen
    data class CategoryDetail(val categoryName: String) : Screen
    data object Settings : Screen
    data object FpsSettings : Screen

    val category: PropertyCategory?
        get() = when (this) {
            is CategoryDetail -> PropertyCategory.entries.find { it.name == categoryName }
            else -> null
        }
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.CategoryList) }
    var predictiveBackEnabled by remember { mutableStateOf(true) }

    val app = LanVToolApp.instance

    LaunchedEffect(Unit) {
        app.settingsRepository.observePredictiveBack().collectLatest { enabled ->
            predictiveBackEnabled = enabled
        }
    }

    val canGoBack = currentScreen !is Screen.CategoryList

    BackHandler(enabled = canGoBack && predictiveBackEnabled) {
        currentScreen = Screen.CategoryList
    }

    val propertyViewModel: PropertyViewModel = viewModel(
        factory = PropertyViewModel.Factory(
            observePropertiesUseCase = app.observePropertiesUseCase,
            refreshPropertiesUseCase = app.refreshPropertiesUseCase,
            applyPropertyUseCase = app.applyPropertyUseCase,
            restorePropertyUseCase = app.restorePropertyUseCase,
            restoreAllUseCase = app.restoreAllUseCase,
            checkRootUseCase = app.checkRootUseCase
        )
    )
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(
            getSettingsUseCase = app.getSettingsUseCase,
            updateSettingsUseCase = app.updateSettingsUseCase,
            propertyRepository = app.propertyRepository,
            settingsRepository = app.settingsRepository
        )
    )
    val fpsViewModel: FpsViewModel = viewModel(
        factory = FpsViewModel.Factory(app.fpsRepository)
    )

    when (val screen = currentScreen) {
        Screen.CategoryList -> PropertyRoute(
            viewModel = propertyViewModel,
            onNavigateToCategory = { category ->
                currentScreen = Screen.CategoryDetail(category.name)
            },
            onNavigateToSettings = {
                currentScreen = Screen.Settings
            },
            onNavigateToFps = {
                currentScreen = Screen.FpsSettings
            }
        )
        is Screen.CategoryDetail -> screen.category?.let { category ->
            CategoryDetailRoute(
                category = category,
                viewModel = propertyViewModel,
                onNavigateBack = {
                    currentScreen = Screen.CategoryList
                }
            )
        } ?: run {
            currentScreen = Screen.CategoryList
            PropertyRoute(
                viewModel = propertyViewModel,
                onNavigateToCategory = { category ->
                    currentScreen = Screen.CategoryDetail(category.name)
                },
                onNavigateToSettings = {
                    currentScreen = Screen.Settings
                },
                onNavigateToFps = {
                    currentScreen = Screen.FpsSettings
                }
            )
        }
        Screen.Settings -> SettingsRoute(
            viewModel = settingsViewModel,
            onNavigateBack = {
                currentScreen = Screen.CategoryList
            }
        )
        Screen.FpsSettings -> FpsRoute(
            viewModel = fpsViewModel,
            onNavigateBack = {
                currentScreen = Screen.CategoryList
            }
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PrivacyDialog(onAccept: () -> Unit, onReject: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    var canProceed by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val pages = listOf("隐私条款", "免责声明", "软件协议", "版权声明", "联系我们")
    val totalPages = pages.size

    val pageContents = listOf(
        """
# 隐私政策

> [!WARNING]
> **请您仔细阅读本隐私政策，您同意本协议即视为已充分理解全部内容。**

---

## 一、信息收集

### 1.1 我们收集的信息

本软件在运行过程中会收集以下信息：

- **设备信息**：包括设备型号、系统版本、屏幕分辨率等基础硬件信息
- **Root权限状态**：检测设备是否已获取Root权限
- **系统属性数据**：读取并备份设备的系统属性（getprop）

### 1.2 我们不收集的信息

我们郑重承诺**不会收集**以下信息：

- 不会收集您的个人身份信息（姓名、身份证号等）
- 不会收集您的通讯录、短信、通话记录
- 不会收集您的地理位置信息
- 不会收集您的照片、视频等媒体文件
- 不会收集您的网络浏览记录
- 不会收集您的账号密码信息

---

## 二、信息使用

### 2.1 使用目的

收集的信息仅用于以下目的：

1. 实现软件核心功能（系统属性备份与恢复）
2. 检测软件运行环境（Root权限检测）
3. 优化软件性能和用户体验

### 2.2 数据存储

- 所有数据**仅存储在本地设备**中
- 不会上传到任何远程服务器
- 不会与第三方共享您的数据
- 您可以随时清除所有本地数据

---

## 三、数据安全

### 3.1 安全措施

我们采取以下措施保护您的数据安全：

- 所有数据加密存储在设备本地
- 不会通过网络传输任何用户数据
- 软件不包含任何网络通信模块

### 3.2 数据删除

您可以通过以下方式删除数据：

- 在设置中清除所有备份
- 卸载软件（将删除所有相关数据）
- 手动删除软件数据目录

---

## 四、权限说明

本软件需要以下权限：

| 权限 | 用途 |
|------|------|
| Root权限 | 读取/写入系统属性 |
| 存储权限 | 备份数据本地存储 |

---

## 五、协议变更

我们保留随时更新本隐私政策的权利。如发生重大变更，将通过软件内公告通知您。

---

**最后更新日期：2026年7月27日**
        """.trimIndent(),

        """
# 免责声明

> [!IMPORTANT]
> **使用本软件前，请仔细阅读并充分理解本免责声明。**

---

## 一、风险提示

### 1.1 软件性质

本软件是一款**系统工具类应用**，需要获取设备的Root权限才能正常运行。使用Root权限存在一定风险，包括但不限于：

- 可能导致设备保修失效
- 不当操作可能导致系统不稳定
- 可能导致数据丢失
- 可能导致设备无法正常启动

### 1.2 使用风险

使用本软件可能带来以下风险：

1. **系统风险**：修改系统属性可能导致系统不稳定或异常
2. **数据风险**：操作不当可能导致应用数据或系统数据丢失
3. **安全风险**：Root权限可能被恶意软件利用
4. **硬件风险**：不当的超频/修改可能导致硬件损坏

---

## 二、责任限制

### 2.1 免责范围

在法律允许的最大范围内，对于因使用或无法使用本软件导致的任何损害，我们不承担赔偿责任，包括但不限于：

- 直接损害、间接损害、附带损害
- 利润损失、数据丢失
- 业务中断
- 任何其他损害

### 2.2 用户责任

您理解并同意：

- 使用本软件的风险由您自行承担
- 您对自己的行为负责
- 您应当谨慎操作，自行判断操作的风险

---

## 三、使用建议

### 3.1 操作前准备

- 请确保已备份重要数据
- 请确保设备电量充足
- 请确保了解每项操作的作用

### 3.2 注意事项

- 不要随意修改不了解的属性
- 建议先备份再修改
- 遇到问题请及时恢复原设置

---

**继续使用本软件即表示您已阅读、理解并接受以上全部条款。**
        """.trimIndent(),

        """
# 软件使用协议

> [!NOTE]
> **本协议是您与软件开发者之间关于使用本软件的法律协议。**

---

## 一、定义

### 1.1 软件

本协议中的"软件"指 LanV Tool 应用程序及其所有相关组件、更新、补丁和文档。

### 1.2 用户

本协议中的"用户"指下载、安装、使用本软件的任何个人或组织。

---

## 二、授权范围

### 2.1 使用授权

我们授予您一项**个人的、不可转让的、非独占的**使用许可，允许您：

- 在一台设备上安装和使用本软件
- 出于个人非商业目的使用本软件
- 制作一份备份副本

### 2.2 使用限制

您不得：

- 复制、修改、翻译本软件
- 反向工程、反编译、反汇编本软件
- 出租、租赁、出借本软件
- 移除或修改任何版权声明
- 将本软件用于任何非法目的

---

## 三、用户义务

### 3.1 合法使用

您承诺：

- 遵守所有适用的法律法规
- 不得利用本软件从事违法活动
- 不得损害他人合法权益
- 不得干扰软件正常运行

### 3.2 账号安全

您应当：

- 妥善保管您的设备
- 对设备上的所有操作负责
- 如发现安全问题及时反馈

---

## 四、服务变更

我们保留以下权利：

- 修改或终止软件功能
- 暂停或终止软件服务
- 更新软件版本
- 修改使用协议

---

**使用本软件即表示您同意本协议的全部条款。**
        """.trimIndent(),

        """
# 版权声明

> [!CAUTION]
> **本软件受著作权法和国际条约保护，未经授权请勿侵权。**

---

## 一、知识产权

### 1.1 软件所有权

本软件的**一切权利**归以下主体所有：

# **KC-Offical**

包括但不限于：

- 软件著作权
- 源代码所有权
- 界面设计权
- 文档著作权
- 商标权
- 其他知识产权

### 1.2 法律保护

本软件受以下法律保护：

- 《中华人民共和国著作权法》
- 《计算机软件保护条例》
- 《伯尔尼公约》
- 其他国际知识产权条约

---

## 二、侵权处理

### 2.1 侵权行为

以下行为构成侵权：

- 未经授权复制、分发本软件
- 未经授权修改、逆向本软件
- 未经授权用于商业用途
- 未经授权移除版权声明
- 其他侵犯知识产权的行为

### 2.2 法律责任

侵权者将承担：
- 民事责任（赔偿损失）
- 行政责任（罚款、没收）
- 刑事责任（情节严重者）

---

## 三、开源声明

### 3.1 开源组件

本软件可能包含开源组件，其使用受其各自开源许可证的约束。

### 3.2 致谢

感谢所有开源社区开发者的贡献。

---

**本软件所有权归 KC-Offical 所有，侵权必究。**

© 2026 KC-Offical. All Rights Reserved.
        """.trimIndent(),

        """
# 联系我们

> [!NOTE]
> **感谢使用本软件！如有问题或建议，欢迎联系我们。**

---

## 开发者介绍

### 栖晞

- 坚持ReviewCode
- 啥都会一点点
- 努力找对象ing.....

---

## 联系方式

| 平台 | 账号 |
|------|------|
| GitHub | boxiaolanya2008 |
| QQ | 3520687734 |

---

## 软件归属

本软件由 **KC-Offical** 开发并拥有

- 软件所有权：KC-Offical
- 开发者：栖晞

---

**再次感谢您的使用！**
        """.trimIndent()
    )

    AlertDialog(
        onDismissRequest = {},
        title = {
            Column {
                Text(
                    text = "用户协议与隐私条款",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pages[currentPage],
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${currentPage + 1} / $totalPages",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { (currentPage + 1).toFloat() / totalPages },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .verticalScroll(scrollState)
            ) {
                FullMarkdownRenderer(pageContents[currentPage])
            }
        },
        confirmButton = {
            if (currentPage == totalPages - 1) {
                TextButton(onClick = onAccept) {
                    Text("我已阅读并同意")
                }
            } else {
                TextButton(
                    onClick = {
                        if (canProceed) {
                            currentPage++
                            canProceed = false
                        }
                    },
                    enabled = canProceed
                ) {
                    Text("下一页")
                }
            }
        },
        dismissButton = {
            if (currentPage == 0) {
                TextButton(onClick = onReject) {
                    Text("不同意并退出")
                }
            } else {
                TextButton(onClick = {
                    currentPage--
                    canProceed = true
                }) {
                    Text("上一页")
                }
            }
        }
    )

    LaunchedEffect(scrollState.value, scrollState.maxValue) {
        if (scrollState.maxValue > 0 && scrollState.value >= scrollState.maxValue - 10) {
            canProceed = true
        }
    }

    LaunchedEffect(currentPage) {
        scrollState.scrollTo(0)
        canProceed = false
    }
}

@Composable
fun FullMarkdownRenderer(markdown: String) {
    val lines = markdown.lines()
    var inTable = false

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            when {
                // GitHub 风格警告
                line.startsWith("> [!WARNING]") -> {
                    val content = mutableListOf<String>()
                    if (line.length > 12) content.add(line.substring(12))
                    i++
                    while (i < lines.size) {
                        val l = lines[i]
                        if (l.startsWith("> ")) {
                            content.add(l.substring(2))
                            i++
                        } else if (l.isBlank()) {
                            i++
                            break
                        } else {
                            break
                        }
                    }
                    GitHubAlert("WARNING", content.joinToString("\n"), MaterialTheme.colorScheme.error)
                }
                line.startsWith("> [!IMPORTANT]") -> {
                    val content = mutableListOf<String>()
                    if (line.length > 14) content.add(line.substring(14))
                    i++
                    while (i < lines.size) {
                        val l = lines[i]
                        if (l.startsWith("> ")) {
                            content.add(l.substring(2))
                            i++
                        } else if (l.isBlank()) {
                            i++
                            break
                        } else {
                            break
                        }
                    }
                    GitHubAlert("IMPORTANT", content.joinToString("\n"), MaterialTheme.colorScheme.primary)
                }
                line.startsWith("> [!NOTE]") -> {
                    val content = mutableListOf<String>()
                    if (line.length > 10) content.add(line.substring(10))
                    i++
                    while (i < lines.size) {
                        val l = lines[i]
                        if (l.startsWith("> ")) {
                            content.add(l.substring(2))
                            i++
                        } else if (l.isBlank()) {
                            i++
                            break
                        } else {
                            break
                        }
                    }
                    GitHubAlert("NOTE", content.joinToString("\n"), MaterialTheme.colorScheme.tertiary)
                }
                line.startsWith("> [!CAUTION]") -> {
                    val content = mutableListOf<String>()
                    if (line.length > 13) content.add(line.substring(13))
                    i++
                    while (i < lines.size) {
                        val l = lines[i]
                        if (l.startsWith("> ")) {
                            content.add(l.substring(2))
                            i++
                        } else if (l.isBlank()) {
                            i++
                            break
                        } else {
                            break
                        }
                    }
                    GitHubAlert("CAUTION", content.joinToString("\n"), MaterialTheme.colorScheme.error)
                }
                line.startsWith("> ") -> {
                    Text(
                        text = line.substring(2),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    i++
                }
                line.startsWith("### ") -> {
                    Text(
                        text = line.removePrefix("### "),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    i++
                }
                line.startsWith("## ") -> {
                    Text(
                        text = line.removePrefix("## "),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    i++
                }
                line.startsWith("# ") -> {
                    Text(
                        text = line.removePrefix("# "),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    i++
                }
                line.startsWith("---") -> {
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(4.dp))
                    i++
                }
                line.startsWith("|") -> {
                    val tableLines = mutableListOf<String>()
                    while (i < lines.size && lines[i].startsWith("|")) {
                        tableLines.add(lines[i])
                        i++
                    }
                    MarkdownTable(tableLines)
                }
                line.isBlank() -> {
                    Spacer(modifier = Modifier.height(2.dp))
                    i++
                }
                else -> {
                    MarkdownTextLine(line)
                    i++
                }
            }
        }
    }
}

@Composable
fun GitHubAlert(type: String, content: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
        Column {
            Text(
                text = type,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun MarkdownTextLine(text: String) {
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
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 1.dp)
    )
}

@Composable
fun MarkdownTable(lines: List<String>) {
    if (lines.size < 2) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        lines.forEachIndexed { index, line ->
            val cells = line.split("|").filter { it.isNotBlank() }.map { it.trim() }
            if (index == 1 && cells.all { it.matches(Regex("^[:\\-]+$")) }) return@forEachIndexed

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                cells.forEach { cell ->
                    Text(
                        text = cell,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f),
                        textAlign = if (cells.indexOf(cell) == 0) TextAlign.Start else TextAlign.Center
                    )
                }
            }
            if (index == 0) {
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}
