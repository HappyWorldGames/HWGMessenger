package com.happyworldgames.privatechat.data

import java.util.*

class Room(var room_type: String = "", var room_id: String = "",
           var reverse_time_last_message: Long = Long.MAX_VALUE - Date().time)