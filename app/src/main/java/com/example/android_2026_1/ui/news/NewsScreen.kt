package com.example.android_2026_1.ui.news

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.android_2026_1.data.ImageUtils
import com.example.android_2026_1.data.NewsItem
import com.example.android_2026_1.R
import com.example.android_2026_1.data.SteamAppItem

@Composable
fun NewsRoute(
    modifier: Modifier = Modifier,
    viewModel: NewsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    NewsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
fun NewsScreen(
    uiState: NewsUiState,
    onEvent: (NewsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSettingsOpen by rememberSaveable { mutableStateOf(false) }
    var selectedNewsUrl by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { isSettingsOpen = true }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.cd_settings)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NewsSearchBar(
                text = uiState.text,
                suggestions = uiState.suggestions,
                onTextChange = { onEvent(NewsEvent.TextChanged(it)) },
                onSearch = { onEvent(NewsEvent.Search) },
                onSelect = { onEvent(NewsEvent.SelectItem(it)) }
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                NewsList(
                    newsList = uiState.newsList,
                    showImg = uiState.showImg,
                    onNewsClick = { selectedNewsUrl = it }
                )
            }
        }
    }

    if (isSettingsOpen) {
        SettingsDialog(
            uiState = uiState,
            onDismiss = { isSettingsOpen = false },
            onApply = { img, dev, ext, empty ->
                onEvent(NewsEvent.UpdateSettings(img, dev, ext, empty))
                isSettingsOpen = false
            }
        )
    }

    selectedNewsUrl?.let { url ->
        NewsWebViewDialog(
            url = url,
            onDismiss = { selectedNewsUrl = null }
        )
    }
}

@Composable
private fun NewsSearchBar(
    text: String,
    suggestions: List<SteamAppItem>,
    onTextChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.search_hint)) },
                singleLine = true,
                trailingIcon = {
                    if (text.isNotEmpty()) {
                        IconButton(onClick = { onTextChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.cd_close)
                            )
                        }
                    }
                }
            )
            IconButton(onClick = onSearch) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.cd_search)
                )
            }
        }

        if (suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column {
                    suggestions.take(5).forEach { item ->
                        Text(
                            text = item.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(item.name) }
                                .padding(12.dp),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsList(
    newsList: List<NewsItem>,
    showImg: Boolean,
    onNewsClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(newsList) { item ->
            NewsCard(
                item = item,
                showImg = showImg,
                onClick = { onNewsClick(item.url) }
            )
        }
    }
}

@Composable
private fun NewsCard(
    item: NewsItem,
    showImg: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val imgUrl = ImageUtils.extractImageUrl(item.contents)
            if (showImg && imgUrl != null) {
                AsyncImage(
                    model = imgUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium
            )
            if (item.author.isNotEmpty()) {
                Text(
                    text = item.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsDialog(
    uiState: NewsUiState,
    onDismiss: () -> Unit,
    onApply: (Boolean, Boolean, Boolean, Boolean) -> Unit
) {
    var img by remember { mutableStateOf(uiState.showImg) }
    var dev by remember { mutableStateOf(uiState.dev) }
    var ext by remember { mutableStateOf(uiState.external) }
    var empty by remember { mutableStateOf(uiState.showEmpty) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.show_images_label))
                    Switch(checked = img, onCheckedChange = { img = it })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = dev, onCheckedChange = { dev = it })
                    Text(stringResource(R.string.news_type_dev))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = ext, onCheckedChange = { ext = it })
                    Text(stringResource(R.string.news_type_external))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = empty, onCheckedChange = { empty = it })
                    Text("기타 (빈 값)")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(img, dev, ext, empty) }) {
                Text(stringResource(R.string.btn_apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

@Composable
private fun NewsWebViewDialog(
    url: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.cd_close)
                        )
                    }
                }
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webViewClient = WebViewClient()
                            loadUrl(url)
                        }
                    },
                    onRelease = { webView ->
                        webView.stopLoading()
                        webView.destroy()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}