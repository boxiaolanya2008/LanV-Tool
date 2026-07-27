package github.boxiaolanya2008.lanv_tool.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import github.boxiaolanya2008.lanv_tool.data.model.PropertyCategory
import github.boxiaolanya2008.lanv_tool.ui.viewmodel.PropertyAction
import github.boxiaolanya2008.lanv_tool.ui.viewmodel.PropertyUiState
import github.boxiaolanya2008.lanv_tool.ui.viewmodel.PropertyViewModel

@Composable
fun PropertyRoute(
    viewModel: PropertyViewModel,
    onNavigateToCategory: (PropertyCategory) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onAction(PropertyAction.ClearMessage)
        }
    }

    CategoryListScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onNavigateToCategory = onNavigateToCategory,
        onNavigateToSettings = onNavigateToSettings
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CategoryListScreen(
    state: PropertyUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (PropertyAction) -> Unit,
    onNavigateToCategory: (PropertyCategory) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "LanV Tool",
                            style = MaterialTheme.typography.titleLarge
                        )
                        RootStatusIndicator(state = state)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "设置")
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
            PropertyUiState.Loading -> LoadingContent(paddingValues)
            is PropertyUiState.Error -> ErrorContent(
                paddingValues = paddingValues,
                message = state.message,
                isNoRoot = state.isNoRoot,
                onRetry = { onAction(PropertyAction.Retry) }
            )
            is PropertyUiState.Success -> CategoryListContent(
                paddingValues = paddingValues,
                state = state,
                onAction = onAction,
                onNavigateToCategory = onNavigateToCategory
            )
        }
    }
}

@Composable
private fun RootStatusIndicator(state: PropertyUiState) {
    val hasRoot = state is PropertyUiState.Success
    val isLoading = state is PropertyUiState.Loading
    val isNoRoot = state is PropertyUiState.Error && state.isNoRoot

    if (isLoading) return

    val (color, text) = when {
        isNoRoot -> MaterialTheme.colorScheme.error to "无 ROOT 权限"
        hasRoot -> MaterialTheme.colorScheme.primary to "已获取 ROOT"
        else -> MaterialTheme.colorScheme.error to "未知状态"
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

@Composable
private fun LoadingContent(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "正在检测 Root 权限并备份属性...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(
    paddingValues: PaddingValues,
    message: String,
    isNoRoot: Boolean,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("重新检测")
            }
        }
    }
}

@Composable
private fun CategoryListContent(
    paddingValues: PaddingValues,
    state: PropertyUiState.Success,
    onAction: (PropertyAction) -> Unit,
    onNavigateToCategory: (PropertyCategory) -> Unit
) {
    val categoryStats = state.categories.map { category ->
        val properties = state.properties.filter { it.property.category == category }
        val modifiedCount = properties.count { it.isModified }
        CategoryStat(category, properties.size, modifiedCount)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.modifiedCount > 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${state.modifiedCount} 项属性已修改",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        TextButton(onClick = { onAction(PropertyAction.RestoreAll) }) {
                            Text("全部还原")
                        }
                    }
                }
            }
        }

        items(categoryStats, key = { it.category.name }) { stat ->
            CategoryCard(
                stat = stat,
                onClick = { onNavigateToCategory(stat.category) }
            )
        }
    }
}

data class CategoryStat(
    val category: PropertyCategory,
    val totalCount: Int,
    val modifiedCount: Int
)

@Composable
private fun CategoryCard(
    stat: CategoryStat,
    onClick: () -> Unit
) {
    val icon = when (stat.category) {
        PropertyCategory.MEMORY -> Icons.Filled.Memory
        PropertyCategory.DISPLAY -> Icons.Filled.PhoneAndroid
        PropertyCategory.CHARGING -> Icons.Filled.BatteryChargingFull
        PropertyCategory.GAME -> Icons.Filled.SportsEsports
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stat.category.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (stat.modifiedCount > 0) {
                        "${stat.totalCount} 项功能 · ${stat.modifiedCount} 项已修改"
                    } else {
                        "${stat.totalCount} 项功能"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (stat.modifiedCount > 0) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
