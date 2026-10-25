package com.tokitodo.app.data

import com.tokitodo.app.ui.theme.ColorPreset

enum class ThemeMode { System, Light, Dark }

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
    /** 学霸模式：专注中不允许暂停，离开计时页会提示。 */
    val studyMode: Boolean = false,
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
    val dailyGoalMinutes: Int = 120,
    val userName: String = "番茄同学",
    val userEmoji: String = "🍅",
)
