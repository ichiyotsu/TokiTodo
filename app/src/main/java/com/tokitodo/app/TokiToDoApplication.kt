package com.tokitodo.app

import android.app.Application

/**
 * TokiToDo 没有网络、没有后台服务，Application 只保留一个稳定的入口，
 * 方便以后接入桌面小组件或局域网助手。
 */
class TokiToDoApplication : Application()
