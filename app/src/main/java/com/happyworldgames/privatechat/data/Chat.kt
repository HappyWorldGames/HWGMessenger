package com.happyworldgames.privatechat.data

import android.graphics.Bitmap
import java.util.*
import kotlin.collections.HashMap

class Chat(var chatName: String = "name", var userUid: String = "", var lastMessage: String = "", var chatIcon: Bitmap? = null) {
    var timeLastMessage: Long = Date().time
}