package github.boxiaolanya2008.lanv_tool.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import github.boxiaolanya2008.lanv_tool.data.model.PropertyCategory
import github.boxiaolanya2008.lanv_tool.ui.components.PropertyToggleItem
import github.boxiaolanya2008.lanv_tool.ui.viewmodel.PropertyAction
import github.boxiaolanya2008.lanv_tool.ui.viewmodel.PropertyUiState
import github.boxiaolanya2008.lanv_tool.ui.viewmodel.PropertyViewModel

@Composable
fun CategoryDetailRoute(
    category: PropertyCategory,
    viewModel: PropertyViewModel,
    onNavigateBack: () -> Unit
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

    CategoryDetailScreen(
        category = category,
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CategoryDetailScreen(
    category: PropertyCategory,
    state: PropertyUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (PropertyAction) -> Unit,
    onNavigateBack: () -> Unit
) {
    val properties = when (state) {
        is PropertyUiState.Success -> state.properties.filter { it.property.category == category }
        else -> emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category.label) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = properties,
                key = { it.property.key }
            ) { propertyState ->
                PropertyToggleItem(
                    state = propertyState,
                    isApplying = state is PropertyUiState.Success && state.applyingKey == propertyState.property.key,
                    onApply = { value -> onAction(PropertyAction.ApplyValue(propertyState.property.key, value)) },
                    onRestore = { onAction(PropertyAction.Restore(propertyState.property.key)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
