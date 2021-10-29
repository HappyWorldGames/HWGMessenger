package com.happyworldgames.privatechat.data

import java.util.Date

/*
    readStatus {
        -1 = no send status,
        0 = not delivery,
        1 = delivery,
        2 = read
    }
*/
class Message(val send_by: String = "", var text_message: String = "", var read_status: Int = 0, var time_message: Long = Date().time)