package com.tokitodo.app.data

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread

/**
 * 极简局域网 WebUI：同一 WiFi 下用浏览器打开手机地址，输入配对码后查看
 * 今日专注、累计数据和最近的待办。服务只监听本机局域网，不连接云端。
 */
object WebUiServer {
    private var server: ServerSocket? = null
    private var worker: Thread? = null
    @Volatile private var sessionsProvider: () -> List<FocusSession> = { emptyList() }
    @Volatile private var todosProvider: () -> List<Todo> = { emptyList() }

    @Volatile var pairingCode: String = ""
        private set

    @Volatile var running: Boolean = false
        private set

    fun start(
        port: Int = 8080,
        code: String,
        sessions: () -> List<FocusSession>,
        todos: () -> List<Todo>,
    ): Boolean {
        if (running) return true
        pairingCode = code
        sessionsProvider = sessions
        todosProvider = todos
        return runCatching {
            val socket = ServerSocket(port)
            server = socket
            running = true
            worker = thread(isDaemon = true, name = "TokiTodo-WebUI") {
                while (running && !socket.isClosed) {
                    val client = runCatching { socket.accept() }.getOrNull() ?: break
                    runCatching { handle(client) }
                        .onFailure { runCatching { client.close() } }
                }
            }
            true
        }.getOrDefault(false)
    }

    fun stop() {
        running = false
        runCatching { server?.close() }
        server = null
        worker = null
    }

    fun localAddress(): String {
        return runCatching {
            NetworkInterface.getNetworkInterfaces()
                .asSequence()
                .filter { it.isUp && !it.isLoopback }
                .flatMap { it.inetAddresses.asSequence() }
                .filterIsInstance<Inet4Address>()
                .firstOrNull { it.isSiteLocalAddress }
                ?.hostAddress
        }.getOrNull() ?: "手机 IP"
    }

    private fun handle(client: Socket) {
        client.use { socket ->
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val requestLine = reader.readLine().orEmpty()
            while (true) {
                val line = reader.readLine() ?: break
                if (line.isBlank()) break
            }
            val path = requestLine.split(" ").getOrNull(1).orEmpty()
            val query = path.substringAfter("?", "")
            val params = query.split("&").mapNotNull { pair ->
                if (!pair.contains("=")) null else {
                    val (key, value) = pair.split("=", limit = 2)
                    URLDecoder.decode(key, "UTF-8") to URLDecoder.decode(value, "UTF-8")
                }
            }.toMap()
            val authorized = params["code"] == pairingCode && pairingCode.isNotEmpty()
            val html = if (authorized) dashboardHtml() else loginHtml()
            val bytes = html.toByteArray(Charsets.UTF_8)
            val header = buildString {
                append("HTTP/1.1 200 OK\r\n")
                append("Content-Type: text/html; charset=utf-8\r\n")
                append("Content-Length: ${bytes.size}\r\n")
                append("Connection: close\r\n\r\n")
            }.toByteArray(Charsets.UTF_8)
            val output: OutputStream = socket.getOutputStream()
            output.write(header)
            output.write(bytes)
            output.flush()
        }
    }

    private fun loginHtml(): String = """
        <!doctype html><html lang="zh-CN"><meta charset="utf-8">
        <meta name="viewport" content="width=device-width,initial-scale=1">
        <title>TokiTodo 专注助手</title>
        <style>
        body{margin:0;background:#f6f1e7;color:#25332c;font:16px/1.6 -apple-system,BlinkMacSystemFont,"Segoe UI",sans-serif;display:grid;place-items:center;min-height:100vh}
        .card{background:#fff;border-radius:28px;padding:36px;max-width:420px;box-shadow:0 24px 60px #25332c22}
        h1{margin:0 0 6px;font-size:28px}p{color:#6a7b70}
        input{width:100%;box-sizing:border-box;border:1px solid #d9d2c5;border-radius:16px;padding:14px 16px;font-size:20px;letter-spacing:6px;background:#f6f1e7}
        button{margin-top:14px;width:100%;border:0;border-radius:16px;padding:14px;background:#356859;color:#fff;font-size:16px}
        </style>
        <form class="card" method="get">
          <h1>TokiTodo</h1>
          <p>在手机「我的 → 桌面端专注助手」查看六位配对码。</p>
          <input name="code" inputmode="numeric" maxlength="6" placeholder="配对码" autofocus>
          <button>进入专注面板</button>
        </form>
        </html>
    """.trimIndent()

    private fun dashboardHtml(): String {
        val sessions = sessionsProvider()
        val todos = todosProvider()
        val today = LocalDate.now()
        val todayMinutes = Stats.minutesOn(sessions, today)
        val total = Stats.totalMinutes(sessions)
        val recent = sessions.filter { it.kind == SessionKind.Focus || it.kind == SessionKind.Lock }
            .sortedByDescending { it.endedAt }
            .take(8)
        val dateFormat = DateTimeFormatter.ofPattern("MM-dd HH:mm")
        val recentRows = recent.joinToString("") { session ->
            val date = java.time.Instant.ofEpochMilli(session.endedAt)
                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime().format(dateFormat)
            "<tr><td>${escape(session.title.ifBlank { "自由专注" })}</td><td>$date</td><td>${session.minutes} 分钟</td></tr>"
        }
        val todoRows = todos.filterNot { it.done }.take(8).joinToString("") { todo ->
            "<li>${escape(todo.title)} <span>${todo.minutes} 分钟</span></li>"
        }
        return """
            <!doctype html><html lang="zh-CN"><meta charset="utf-8">
            <meta name="viewport" content="width=device-width,initial-scale=1">
            <title>TokiTodo 专注助手</title>
            <style>
            body{margin:0;background:#f6f1e7;color:#25332c;font:15px/1.6 -apple-system,BlinkMacSystemFont,"Segoe UI",sans-serif}
            main{max-width:860px;margin:0 auto;padding:32px 20px 60px}
            h1{font-size:30px;margin:0 0 4px} .sub{color:#6a7b70;margin:0 0 24px}
            .grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:14px}
            .stat{background:#fff;border-radius:24px;padding:20px}.num{font-size:32px;font-weight:700}.label{color:#6a7b70;font-size:13px}
            section{background:#fff;border-radius:24px;padding:22px;margin-top:16px}
            table{width:100%;border-collapse:collapse}td,th{padding:9px 4px;border-bottom:1px solid #eee7dc;text-align:left}
            li{margin:8px 0}li span{color:#6a7b70;font-size:12px;margin-left:8px}
            a{color:#356859}
            </style>
            <main>
              <h1>TokiTodo</h1>
              <p class="sub">局域网专注面板 · 数据只读 · 来自这台手机</p>
              <div class="grid">
                <div class="stat"><div class="num">$todayMinutes</div><div class="label">今日专注分钟</div></div>
                <div class="stat"><div class="num">$total</div><div class="label">累计专注分钟</div></div>
                <div class="stat"><div class="num">${Stats.activeDays(sessions)}</div><div class="label">专注天数</div></div>
                <div class="stat"><div class="num">${todos.count { !it.done }}</div><div class="label">未完成待办</div></div>
              </div>
              <section><h2>最近记录</h2><table><tr><th>内容</th><th>时间</th><th>时长</th></tr>$recentRows</table></section>
              <section><h2>接下来要做</h2><ul>$todoRows</ul></section>
              <p class="sub">刷新页面获取最新数据。</p>
            </main>
            </html>
        """.trimIndent()
    }

    private fun escape(value: String): String = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}
