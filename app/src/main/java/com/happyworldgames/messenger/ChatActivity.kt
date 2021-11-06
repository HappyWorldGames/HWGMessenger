package com.happyworldgames.messenger

import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.Query
import com.happyworldgames.messenger.adapters.MessagesRecyclerAdapter
import com.happyworldgames.messenger.data.DataBase
import com.happyworldgames.messenger.data.Message
import com.happyworldgames.messenger.data.Room
import com.happyworldgames.messenger.data.Storage
import com.happyworldgames.messenger.databinding.ChatActionBarBinding
import com.happyworldgames.messenger.databinding.ActivityChatBinding
import java.util.*

class ChatActivity : AppCompatActivity() {

    private val activityChat: ActivityChatBinding by lazy { ActivityChatBinding.inflate(layoutInflater) }
    private val chatActionBar: ChatActionBarBinding by lazy { ChatActionBarBinding.bind(supportActionBar!!
        .customView) }
    private val room by lazy { Room(intent.getStringExtra("room_type")!!,
        intent.getStringExtra("room_id")!!) }

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
        supportActionBar?.setCustomView(R.layout.chat_action_bar)
        supportActionBar?.setDisplayShowCustomEnabled(true)

        DataBase.getRoomNameAndAvatarByRoom(this, room) { name, avatarPath ->
            chatActionBar.actionTitle.text = name
            Storage.getAvatarUriByPath(avatarPath) {
                Glide.with(this).load(it).into(chatActionBar.actionIcon)
            }
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

        adapter = MessagesRecyclerAdapter(options, room.room_type, this)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true

        activityChat.messagesRecycler.layoutManager = linearLayoutManager
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
        chatActionBar.actionTitle.setOnClickListener {
            println("Click!")
        }
        chatActionBar.actionIconBox.setOnClickListener {
            onBackPressed()
        }
        activityChat.swipe.setOnRefreshListener {
            adapter?.notifyItemRangeChanged(0, adapter!!.itemCount)
            activityChat.swipe.isRefreshing = false
        }
    }

    private fun sendMessage(isSystem: Boolean = false, systemText: String = "") {
        val databaseReference = when(room.room_type){
            "chat" -> DataBase.getChatByChatId(room.room_id)
            "group" -> DataBase.getGroupByGroupId(room.room_id)
            else -> return
        }

        val m = databaseReference.child("messages").push()
        if(!isSystem) {
            DataBase.getRoomLastMessageByRoom(room) { message ->
                val text = activityChat.messageText.text.toString().trim()
                if(text.isEmpty()) return@getRoomLastMessageByRoom

                val con = fun(){
                    m.setValue(Message(DataBase.getCurrentUser().uid, text,
                        if (room.room_type == "group") -1 else 0))
                    activityChat.messageText.setText("")
                    scrollDown()
                }
                if(message != null) {
                    val calendar = GregorianCalendar()
                    calendar.timeInMillis = message.time_message
                    val calendar2 = GregorianCalendar()
                    if(calendar.get(Calendar.DAY_OF_YEAR) < calendar2.get(Calendar.DAY_OF_YEAR)){
                        sendMessage(true, DateFormat.format("dd.MM.yyyy",
                            System.currentTimeMillis()).toString())
                    }
                    con()
                }else con()
            }
        }else if(systemText.isNotEmpty()) m.setValue(Message("system", systemText, -1))
    }

    fun scrollDown() {
        if(adapter != null) activityChat.messagesRecycler
            .smoothScrollToPosition(adapter!!.itemCount - 1)
    }

    override fun onBackPressed() {
        //supportFinishAfterTransition()
        startActivity(Intent(this, MainActivity::class.java))
    }
}