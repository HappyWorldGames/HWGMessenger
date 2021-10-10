package com.happyworldgames.privatechat.data

import java.util.*

class Message(val sendBy: String = "", var textMessage: String = "", var timeMessage: Long = Date().time)