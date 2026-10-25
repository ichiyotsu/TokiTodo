# TokiToDo — 交接文档

> 更新：2026-10-25 · 版本 0.1.0（versionCode 1，debug）
> 工作区：`D:\DSH\TokiToDo` · 设计参考：`D:\DSH\TokiNote\project`
> 新会话先读 `README.md`，再看 `screenshots/` 和本文。

## 1. 当前状态

五个一级页 + 全部二级页可运行，`assembleDebug` 通过。

- 一级：待办 / 待办集 / 锁机 / 统计数据 / 我的
- 二级：专注历史、未来倒计时、许愿墙、季度卡、学霸模式、七个设置子页
- 覆盖层：专注 / 休息 / 小憩 / 锁机全屏计时，完成弹窗
- 数据：SharedPreferences + JSON，首启种子 2 个待办集 + 4 件待办
- 备份：`其他设置 → 导出备份 / 导入备份`（本地 JSON，覆盖式恢复）
- 自动化：定时锁机每 20 秒检查一次时段；计时结束按设置播放提示音 / 震动
- 截图：`screenshots/01–14`（含深色），来自 API 31 模拟器
- 可安装包：`dist/TokiToDo-0.1.0.apk`（release + R8，debug 签名，约 6.6 MB）
- GitHub：<https://github.com/ichiyotsu/TokiTodo> · Release `0.1.0` 已附带 APK
- 推送方式：github.com:443 在本机被阻断，使用 SSH deploy key
  （`C:\Users\tokisaki\.ssh\tokitodo_ed25519`，只授权本仓库）推送到 `git@github.com:ichiyotsu/TokiTodo.git`

## 2. 环境

```powershell
$env:JAVA_HOME="D:\Application\Android Studio\jbr"
$env:ANDROID_HOME="C:\Users\tokisaki\AppData\Local\Android\Sdk"
cd D:\DSH\TokiToDo
.\gradlew.bat assembleDebug --console=plain
```

- AVD：`Toki_API31`（1080×2400 / 420dpi / 3-button nav）
- 模拟器启动（headless）：
  `emulator -avd Toki_API31 -no-snapshot-load -no-audio -no-boot-anim -gpu swiftshader_indirect -no-window`
- **首次构建需要联网**：离线缓存中缺少 Compose 1.12.1 的模块元数据，
  联网构建一次后即可离线。Gradle 用户目录为 `C:\Users\tokisaki\.gradle`。
- 手势注入：`adb shell input tap x y`；底栏可点击区域 `y ≈ 2095–2237`
  （窗口只到 2274，`y=2280` 会落在系统导航区，点了没反应）。

## 3. 关键文件

| 需求 | 文件 |
| --- | --- |
| 五页导航 / 返回栈 / 计时覆盖 | `ui/TokiToDoApp.kt` |
| 插画封面（Canvas） | `ui/components/Artwork.kt` |
| 毛玻璃底栏 | `ui/components/NavBar.kt` + `ui/theme/Glass.kt` |
| 图表 / 热力图 / 时段条 | `ui/components/Charts.kt` |
| 待办卡 + 编辑弹层 | `ui/todos/` |
| 锁机三卡 + 时段 + 设置 | `ui/lock/LockScreen.kt` |
| 统计四段 + 月历 | `ui/stats/StatsScreen.kt` |
| 我的 + Hero | `ui/profile/ProfileScreen.kt` |
| 设置子页 | `ui/settings/SettingsScreens.kt` |
| 计时器状态机 | `data/TimerViewModel.kt` |
| 统计纯函数 | `data/Stats.kt` |

## 4. 设计约束（必须保持）

- 底栏是**单层**：`shadow → clip → 0.97 底色 → Haze → 1dp 描边`。
  不要再套第二层 Surface / background，否则会出现双层玻璃。
- 毛玻璃回退色必须足够不透明（模拟器没有 RenderEffect 时会走 fallback）。
- 插画卡文字必须带 `Shadow` 且 Artwork 自带底部暗角，浅色封面（晴空）也要可读。
- 主题色由 `ColorPreset` 控制；不要在页面里写死品牌色，除锁机三卡的固定渐变
  （小憩绿 / 锁机珊瑚 / 时段蓝）——那是参考图的功能色，不随主题变。
- 汉字写入文件只用 `write`/`edit` 工具。**不要用 PowerShell `Get-Content/Set-Content`
  改 Kotlin 源文件**：本机是 GBK ANSI，来回一次会变乱码（本会话已踩过）。
- 编辑弹层、锁机弹层等全部使用 `ModalBottomSheet` + `SheetGrabber()`，圆角用 `SheetShape`。
- 数据首启种子由 `TokiStore.isSeeded()` 控制；删除全部待办后不会重新播种。

## 5. 待办 / 下一步

1. 真机回归：Haze 模糊、深色模式、状态栏图标切换（计时页会强制浅色图标）。
2. 桌面小组件：设置页已保存 `WidgetStyle`，只差 `AppWidgetProvider` 与 RemoteViews/Glance。
3. 电脑端助手：当前只有配对码与说明，没有局域网服务（README 已如实说明）。
4. 后台计时：目前计时器是进程内 ViewModel；若要锁屏后继续，需要前台服务 + 通知权限。
5. 真实使用下的统计：可考虑「专注目标达成提醒」和按待办集过滤统计。
6. Pad / 横屏未适配，双栏布局可参考 TokiNote 的 `wideSettings` 分支。

## 6. 已知坑

- `WindowInsets.safeDrawing` 下 Scaffold 底部会让出系统导航；悬浮底栏在 Scaffold 外，
  需要用 `navigationBarsPadding()`，各页列表底部留 148dp 避免最后一张卡被挡。
- 锁机页是 `Column + verticalScroll`，内容超过一屏时底部空状态需要滚动才能看全。
- `TimerViewModel` 用墙钟差推进，切后台再回来不会漂移，但进程被杀会丢计时。
- 定时锁机是应用内 20 秒轮询，不在后台常驻；要真正防退出需要前台服务。
- 统计页「本月」与分布图使用独立月份状态，切月后另一张卡不会跟着变。
- 备份导出/导入使用系统文件选择器（`CreateDocument` / `OpenDocument`），无需存储权限；
  导入是覆盖式，恢复前最好先导出一份。云同步没有接入，是刻意取舍。
- 截图里的统计数据来自本地注入的示例记录（31 段），只存在于模拟器数据中，
  不属于应用资产；首次安装看到的是 0，符合参考图的空状态。
