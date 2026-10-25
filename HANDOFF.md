# TokiToDo — 交接文档

> 更新：2026-10-26 · 版本 0.2.0（versionCode 2）
> 工作区：`D:\DSH\TokiToDo` · 仓库：<https://github.com/ichiyotsu/TokiTodo>
> 设计参考：`D:\DSH\TokiNote\project`（只借设计语言与 Compose 结构）

## 1. 当前状态

五页 + 全部二级页可运行；`assembleDebug` / `assembleRelease` 通过。

- 底栏：TokiNote 风格 M3 悬浮胶囊，选中展开文字，未选中图标；Haze 实时模糊。
- 页面：待办 / 待办集 / 锁机 / 统计数据 / 我的；不显示页面大标题。
- 计时：独立全屏页，五功能栏（常亮 / 背景音 / 循环 / 4 分钟暂停 / 跳过），完成后回主页。
- 强制学习：弹窗 + 提升强制性二级页 + 白名单应用选择（策略持久化，系统执行层预留）。
- 编辑器：悬浮卡片、相册封面 + 裁剪、设置子页、待办集内新建、长按预设改时长。
- 历史：记录编辑 / 心得 / 周纵览 / 周总结 / 补记入口 / 海报（饼图 + 保存相册）。
- 我的：整页渐变；历史主入口、备份恢复、设置全部可用。
- 截图：`screenshots/`（部分仍是旧版，后续真机回归时统一更新）。
- 可安装包：`dist/TokiToDo-0.2.0.apk`（release + R8，debug 签名）。
- Release `0.1.0` 是旧实现；0.2.0 发版后以新 Release 为准。

## 2. 环境

```powershell
$env:JAVA_HOME="D:\Application\Android Studio\jbr"
$env:ANDROID_HOME="C:\Users\tokisaki\AppData\Local\Android\Sdk"
cd D:\DSH\TokiToDo
.\gradlew.bat assembleDebug --console=plain
```

- AVD：`Toki_API31`（Pixel 6 profile，1080×2400 / 420dpi / API 31 / 3-button nav）
- 可见启动：`emulator -avd Toki_API31 -no-snapshot-load -no-audio -no-boot-anim -gpu host`
- 底栏可点击区域只覆盖胶囊本身，不能按旧的全宽坐标点；用 `uiautomator dump` 找 bounds。
- 首次联网构建一次后，`--offline` 可复用依赖元数据。
- 推送：`github.com:443` 在本机被阻断，使用 SSH deploy key
  `C:\Users\tokisaki\.ssh\tokitodo_ed25519`（只授权本仓库）推送到
  `git@github.com:ichiyotsu/TokiTodo.git`；本仓库 `core.sshCommand` 已配好。

## 3. 关键文件

| 需求 | 文件 |
| --- | --- |
| 五页 / 返回栈 / 计时覆盖 / 完成页 | `ui/TokiToDoApp.kt`、`ui/timer/CompletionScreen.kt` |
| 底栏 M3 胶囊 | `ui/components/NavBar.kt` + `theme/Glass.kt` |
| 插画 / 封面 | `ui/components/Artwork.kt`、`ui/todos/CoverImage.kt` |
| 计时状态机 | `data/TimerViewModel.kt`、`ui/timer/TimerScreen.kt` |
| 背景音合成 | `data/AmbientPlayer.kt` |
| 强制学习 | `ui/force/ForceStudy.kt`、`data/AppSettings.kt` |
| 待办编辑器 | `ui/todos/TodoEditorSheet.kt` |
| 历史 / 周视图 / 海报 | `ui/history/HistoryCenterScreen.kt`、`ui/history/FocusPosterSheet.kt` |
| 统计纯函数 | `data/Stats.kt` |

## 4. 设计 / 工程约束

- 底栏是单层：`shadow → clip → 0.86 底色 → Haze → 1dp 描边`；不要再套第二层 Surface。
- 页面不放大标题；只有二级页保留标题（返回箭头 + 标题）。
- 我的页渐变铺到底，不要再加固定高度 Hero 容器。
- 插画卡必须保留暗角与文字阴影，浅色封面也要可读。
- 汉字只通过 `write`/`edit` 写源码；**不要用 PowerShell `Get-Content/Set-Content` 改 Kotlin**（GBK 会乱码）。
- 计时页的完成页会强制浅色状态栏 / 深色系统导航，退出时恢复，注意别在别处覆盖。
- 备份导出 / 导入包含 Todo 新字段、Session 起止 / 心得 / 完成状态、设置里的强制学习与循环字段。
- 时长上限：UI 与存储都按 1440 分钟（24 小时）；预设可长按逐项修改。

## 5. 待办 / 下一步

1. **真机回归**：Haze 实际模糊、深色、状态栏、悬浮编辑器 IME、封面裁剪手势。
2. **系统级强制学习**：接 `SYSTEM_ALERT_WINDOW` + 前台服务做跨 App 遮罩；`NotificationListenerService` 做通知拦截；Device Owner 才能可靠禁止卸载 / 任务列表控制。当前 UI 已就绪，执行层预留。
3. **补记保存**：按需求确定按钮仍置灰，文案“暂不可用”；接数据层后恢复。
4. 桌面小组件 `AppWidgetProvider` / Glance。
5. 海报用 `rememberGraphicsLayer` 捕获海报 Surface；模拟器已保存到相册，真机若空白可改离屏 Canvas。
6. 历史记录的完成进度目前按当日 Focus 记录统计；如需按待办目标/习惯计算，需要进一步聚合。
7. 循环结束后 `Completed` 事件里的分钟数是最后一轮学习时长，不是总时长；完成页文案可再优化。

## 6. 已知坑

- `Stats.inSchedule` 跨零点用“昨天开启的时段”判断；修改时注意。
- 定时锁机是应用内 20 秒轮询，不在后台常驻。
- `TimerViewModel` 用墙钟差推进；进程被杀会丢当前计时。
- 相机 / 相册：使用 Photo Picker（`PickVisualMedia`），无需存储权限；裁剪输出到 `files/covers`，备份 JSON 不包含图片文件本身。
- 分享海报保存到 `Pictures/TokiToDo`，需要 Android 10+ 的 MediaStore；模拟器已验证。
- 删除 `dist/` 不会影响构建；APK 通过 Release 或 `dist/` 分发。
