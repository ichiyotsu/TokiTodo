package com.tokitodo.app.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.tokitodo.app.ui.components.CircleAction
import com.tokitodo.app.ui.components.SectionCard
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object Latest : UpdateState
    data class Available(val version: String, val url: String) : UpdateState
    data object Downloading : UpdateState
    data class Error(val message: String) : UpdateState
}

@Composable
fun AboutScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "0.1.0-beta.1"
    }
    var state by remember { mutableStateOf<UpdateState>(UpdateState.Idle) }

    fun checkUpdate() {
        if (state is UpdateState.Checking) return
        state = UpdateState.Checking
        scope.launch {
            state = withContext(Dispatchers.IO) {
                runCatching {
                    val connection = URL("https://api.github.com/repos/ichiyotsu/TokiTodo/releases?per_page=1")
                        .openConnection() as HttpURLConnection
                    connection.connectTimeout = 12_000
                    connection.readTimeout = 12_000
                    connection.setRequestProperty("User-Agent", "TokiTodo")
                    connection.setRequestProperty("Accept", "application/vnd.github+json")
                    val body = connection.inputStream.bufferedReader().use { it.readText() }
                    val releases = org.json.JSONArray(body)
                    if (releases.length() == 0) return@runCatching UpdateState.Error("还没有发布版本")
                    val json = releases.getJSONObject(0)
                    val latest = json.optString("tag_name").removePrefix("v")
                    val asset = json.optJSONArray("assets")?.let { assets ->
                        (0 until assets.length())
                            .map { assets.getJSONObject(it) }
                            .firstOrNull { it.optString("name").endsWith(".apk") }
                    }
                    if (asset == null) {
                        UpdateState.Error("这个版本还没有 APK")
                    } else if (compareVersions(latest, versionName) <= 0) {
                        UpdateState.Latest
                    } else {
                        UpdateState.Available(latest, asset.optString("browser_download_url"))
                    }
                }.getOrElse { UpdateState.Error("检查更新失败，请检查网络") }
            }
        }
    }

    fun downloadAndInstall(available: UpdateState.Available) {
        state = UpdateState.Downloading
        scope.launch {
            val file = withContext(Dispatchers.IO) {
                runCatching {
                    val dir = File(context.cacheDir, "updates").apply { mkdirs() }
                    val target = File(dir, "TokiTodo-${available.version}.apk")
                    val connection = URL(available.url).openConnection() as HttpURLConnection
                    connection.connectTimeout = 20_000
                    connection.readTimeout = 60_000
                    connection.setRequestProperty("User-Agent", "TokiTodo")
                    connection.inputStream.use { input ->
                        target.outputStream().use { output -> input.copyTo(output) }
                    }
                    target
                }.getOrNull()
            }
            if (file == null) {
                state = UpdateState.Error("下载失败，请稍后再试")
                return@launch
            }
            state = UpdateState.Idle
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                !context.packageManager.canRequestPackageInstalls()
            ) {
                runCatching {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:${context.packageName}"),
                        ),
                    )
                }
                return@launch
            }
            runCatching {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file,
                )
                context.startActivity(
                    Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/vnd.android.package-archive")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    },
                )
            }.onFailure {
                state = UpdateState.Error("无法打开安装器，请检查安装权限")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(bottom = 40.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleAction(
                icon = Icons.Rounded.ArrowBack,
                contentDescription = "返回",
                onClick = onBack,
                container = MaterialTheme.colorScheme.surfaceContainerLow,
                size = 42,
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "关于 TokiTodo",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier.padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        text = "TokiTodo",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "版本 $versionName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "一个安静、本地优先的自控工具。待办、专注、锁机、统计和许愿墙都在这一台设备上。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            SectionCard {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.CloudDownload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "软件更新",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "从 GitHub Release 检查并下载最新 APK",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    when (val current = state) {
                        UpdateState.Idle -> Button(
                            onClick = ::checkUpdate,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = CircleShape,
                        ) {
                            Text("检查更新")
                        }

                        UpdateState.Checking -> Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("正在检查…", style = MaterialTheme.typography.bodySmall)
                        }

                        UpdateState.Latest -> Text(
                            text = "已经是最新版本",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )

                        is UpdateState.Available -> {
                            Text(
                                text = "发现新版本 ${current.version}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = { downloadAndInstall(current) },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = CircleShape,
                            ) {
                                Text("下载并安装")
                            }
                        }

                        UpdateState.Downloading -> Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("正在下载安装包…", style = MaterialTheme.typography.bodySmall)
                        }

                        is UpdateState.Error -> {
                            Text(
                                text = current.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = ::checkUpdate,
                                shape = CircleShape,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("重试")
                            }
                        }
                    }
                }
            }

            SectionCard {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "说明",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "检查更新需要访问 GitHub；下载完成后会交给系统安装器。数据仍然只保存在本机。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "开发者：ichiyotsu\n项目：github.com/ichiyotsu/TokiTodo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun compareVersions(a: String, b: String): Int {
    val left = a.split(".").mapNotNull { it.toIntOrNull() }
    val right = b.split(".").mapNotNull { it.toIntOrNull() }
    for (i in 0 until maxOf(left.size, right.size)) {
        val l = left.getOrElse(i) { 0 }
        val r = right.getOrElse(i) { 0 }
        if (l != r) return l.compareTo(r)
    }
    return 0
}
