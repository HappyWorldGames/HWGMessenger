package com.happyworldgames.privatechat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.Query
import com.happyworldgames.privatechat.adapters.MessagesRecyclerAdapter
import com.happyworldgames.privatechat.data.DataBase
import com.happyworldgames.privatechat.data.Message
import com.happyworldgames.privatechat.data.Room
import com.happyworldgames.privatechat.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private val activityChat: ActivityChatBinding by lazy { ActivityChatBinding.inflate(layoutInflater) }
    private val room by lazy { Room(intent.getStringExtra("room_type")!!, intent.getStringExtra("room_id")!!) }
    private lateinit var userId: String

    private var adapter: MessagesRecyclerAdapter? = null

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }
    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityChat.root)

        DataBase.getRoomNameAndAvatarByRoom(this, room) { name, avatarPath ->
            title = name
            //actionBar.setIcon()
        }

        val databaseReference = when(room.room_type){
            "chat" -> DataBase.getChatByChatId(room.room_id)
            "group" -> DataBase.getGroupByGroupId(room.room_id)
            else -> return
        }

        val query: Query = databaseReference.child("messages")
            .orderByChild("time_message").limitToLast(40)
        val options = FirebaseRecyclerOptions.Builder<Message>()
            .setQuery(query, Message::class.java)
            .build()

        adapter = MessagesRecyclerAdapter(options, room.room_type)

        activityChat.messagesRecycler.layoutManager = LinearLayoutManager(this)
        activityChat.messagesRecycler.adapter = adapter

        activityChat.sendFab.setOnClickListener {
            sendMessage()
        }
        /*activityChat.messageText.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                sendMessage()
                true
            }else false
        }*/
    }

    private fun sendMessage() {
        val databaseReference = when(room.room_type){
            "chat" -> DataBase.getChatByChatId(room.room_id)
            "group" -> DataBase.getGroupByGroupId(room.room_id)
            else -> return
        }

        if(activityChat.messageText.text.toString() == "") return
        databaseReference.child("messages").push().setValue(Message(DataBase.getCurrentUser().uid, activityChat.messageText.text.toString()))
        activityChat.messageText.setText("")
    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}