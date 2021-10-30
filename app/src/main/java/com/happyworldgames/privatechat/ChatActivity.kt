package com.happyworldgames.privatechat

import android.os.Bundle
import android.text.format.DateFormat
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.Query
import com.happyworldgames.privatechat.adapters.MessagesRecyclerAdapter
import com.happyworldgames.privatechat.data.DataBase
import com.happyworldgames.privatechat.data.Message
import com.happyworldgames.privatechat.data.Room
import com.happyworldgames.privatechat.data.Storage
import com.happyworldgames.privatechat.databinding.ActivityChatBinding
import com.happyworldgames.privatechat.databinding.ChatActionBarBinding
import java.util.*

class ChatActivity : AppCompatActivity() {

    private val activityChat: ActivityChatBinding by lazy { ActivityChatBinding.inflate(layoutInflater) }
    private val chatActionBar: ChatActionBarBinding by lazy { ChatActionBarBinding.bind(supportActionBar!!.customView) }
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

        adapter = MessagesRecyclerAdapter(options, room.room_type)

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
                if(activityChat.messageText.text.toString().isEmpty()) return@getRoomLastMessageByRoom
                val con = fun(){
                    m.setValue(Message(DataBase.getCurrentUser().uid, activityChat.messageText.text.toString(),
                        if (room.room_type == "group") -1 else 0))
                    activityChat.messageText.setText("")
                }
                if(message != null) {
                    val calendar = GregorianCalendar()
                    calendar.timeInMillis = message.time_message
                    val calendar2 = GregorianCalendar()
                    if(calendar.get(Calendar.DAY_OF_YEAR) < calendar2.get(Calendar.DAY_OF_YEAR)){
                        sendMessage(true, DateFormat.format("dd.MM.yyyy", System.currentTimeMillis()).toString())
                    }
                    con()
                }else con()
            }
        }else if(systemText.isNotEmpty()) m.setValue(Message("system", systemText, -1))
    }

    override fun onBackPressed() {
        supportFinishAfterTransition()
        //startActivity(Intent(this, MainActivity::class.java))
    }
}