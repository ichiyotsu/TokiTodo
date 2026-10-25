package com.tokitodo.app.ui

/** 一级 Tab。底栏只在这五个目的地显示。 */
enum class Tab(val label: String) {
    Todos("待办"),
    Collections("待办集"),
    Lock("锁机"),
    Stats("统计数据"),
    Profile("我的"),
}

enum class SettingsPage(val title: String) {
    Timer("专注计时核心设置"),
    Appearance("海报背景和外观"),
    NavBar("自定义底栏模块"),
    Theme("主题颜色搭配"),
    Widget("桌面小组件选项"),
    Desktop("连接电脑端专注助手"),
    Other("其他设置"),
}

sealed interface Dest {
    data object Home : Dest
    data class Detail(val page: SettingsPage) : Dest
    data object History : Dest
    data object Countdown : Dest
    data object WishWall : Dest
    data object Vip : Dest
    data object StudyMode : Dest
    data object LockSettings : Dest

    /** 返回栈深度，用于决定转场方向。 */
    fun depth(): Int = when (this) {
        Home -> 0
        else -> 1
    }
}
