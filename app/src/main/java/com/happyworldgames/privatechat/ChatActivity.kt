package com.happyworldgames.privatechat

import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.Query
import com.happyworldgames.privatechat.adapters.MessagesRecyclerAdapter
import com.happyworldgames.privatechat.data.Message
import com.happyworldgames.privatechat.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private val activityChat: ActivityChatBinding by lazy { ActivityChatBinding.inflate(layoutInflater) }
    private val chatName by lazy { intent.getStringExtra("chat_name")?: "" }
    private val userUid by lazy { intent.getStringExtra("user_uid")?: "" }

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

        if(chatName == "" || userUid == "") return
        title = chatName

        val query: Query = DataBase.getChatsByUserUid(DataBase.getCurrentUser().uid).child(chatName).child("message")
            .limitToLast(40)
        val options = FirebaseRecyclerOptions.Builder<Message>()
            .setQuery(query, Message::class.java)
            .build()

        adapter = MessagesRecyclerAdapter(options)

        activityChat.messagesRecycler.layoutManager = LinearLayoutManager(this)
        activityChat.messagesRecycler.adapter = adapter

        activityChat.sendFab.setOnClickListener {
            sendMessage()
        }
        activityChat.messageText.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                sendMessage()
                true
            }else false
        }
    }

    private fun sendMessage() {
        if(activityChat.messageText.text.toString() == "") return
        DataBase.getChatsByUserUid(DataBase.getCurrentUser().uid).child(chatName).child("message").push().setValue(Message("you", activityChat.messageText.text.toString()))
        if(DataBase.getCurrentUser().uid != userUid) DataBase.getChatsByUserUid(userUid).child(DataBase.getCurrentUser().displayName!!).child("message").push().setValue(Message("", activityChat.messageText.text.toString()))
        activityChat.messageText.setText("")
    }
}