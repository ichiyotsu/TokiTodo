package com.tokitodo.app.data

import com.tokitodo.app.ui.theme.ColorPreset

enum class ThemeMode { System, Light, Dark }

/** 计时页时钟字体。 */
enum class ClockFont(val label: String) {
    Sans("默认"),
    Serif("衬线"),
    Mono("等宽"),
    Digital("时钟"),
}

/** 桌面小组件的几种背景。 */
enum class WidgetStyle(val label: String) {
    Forest("森林晨雾"),
    Paper("纸感留白"),
    Sunrise("日出渐变"),
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val dynamicColor: Boolean = false,
    val amoled: Boolean = false,
    val colorPreset: ColorPreset = ColorPreset.Pine,
    val reduceMotion: Boolean = false,
    /** 专注计时：一次专注、短休、长休、长休间隔。 */
    val focusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val sessionsBeforeLongBreak: Int = 4,
    val autoStartNext: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    /** 强制学习模式总开关；开启后计时中尝试阻止其他 App。 */
    val studyMode: Boolean = false,
    /** 严格模式：计时中不能暂停，只能提前完成或取消。 */
    val strictMode: Boolean = false,
    /** 拦截其他 App 的通知（需要系统通知使用权）。 */
    val blockNotifications: Boolean = false,
    /** 提升强制性选项。 */
    val blockRecents: Boolean = false,
    val hideInRecents: Boolean = false,
    val lockBlockHome: Boolean = false,
    /** 全局白名单包名。 */
    val whitelistPackages: Set<String> = emptySet(),
    /** 循环专注：学习 / 休息交替。 */
    val cycleEnabled: Boolean = false,
    val cycleStudyMinutes: Int = 25,
    val cycleBreakMinutes: Int = 5,
    val cycleCount: Int = 4,
    val cycleInfinite: Boolean = true,
    /** 计时页屏幕常亮。 */
    val keepScreenAwake: Boolean = true,
    /** 计时页时钟字体。 */
    val clockFont: ClockFont = ClockFont.Sans,
    /** 计时页自定义背景图，保存在应用私有目录。 */
    val timerBackgroundPath: String? = null,
    /** 待办编辑器的时长预设，可长按修改。 */
    val durationPresets: List<Int> = listOf(5, 10, 15, 25, 45, 60, 90),
    /** 锁机页的强制性选项。 */
    val lockBlockNotifications: Boolean = true,
    val lockAllowEmergency: Boolean = true,
    val lockShowCountdown: Boolean = true,
    /** 底栏是否显示文字标签。 */
    val navLabels: Boolean = true,
    /** 启动时默认进入的一级页面（Tab.name）。 */
    val defaultTab: String = "Todos",
    val widgetStyle: WidgetStyle = WidgetStyle.Forest,
    val desktopAssistant: Boolean = false,
    val desktopPairingCode: String = "",
    val dailyGoalMinutes: Int = 120,
    val userName: String = "番茄同学",
    val userEmoji: String = "🍅",
    val userSignature: String = "把注意力留给真正重要的事",
    val userAvatarPath: String? = null,
)
