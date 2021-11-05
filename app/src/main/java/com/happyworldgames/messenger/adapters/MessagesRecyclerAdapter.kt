package com.happyworldgames.messenger.adapters

import android.graphics.Color
import android.text.format.DateFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.happyworldgames.messenger.ChatActivity
import com.happyworldgames.messenger.R
import com.happyworldgames.messenger.data.Contact
import com.happyworldgames.messenger.data.DataBase
import com.happyworldgames.messenger.data.Message
import com.happyworldgames.messenger.data.User
import com.happyworldgames.messenger.databinding.MessageItemBinding

class MessagesRecyclerAdapter(options: FirebaseRecyclerOptions<Message>, private val roomType: String,
                              private val chatActivity: ChatActivity)
    : FirebaseRecyclerAdapter<Message, MessagesRecyclerAdapter.MessageViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.message_item,
            parent, false)
        val holder = MessageViewHolder(view)

        when(viewType){
            0 -> (holder.messageItemBinding.cardView.layoutParams
                    as ViewGroup.MarginLayoutParams).marginEnd = 0
            1 -> {
                holder.messageItemBinding.message.gravity = Gravity.CENTER
                holder.messageItemBinding.message.setTextColor(Color.RED)
                holder.messageItemBinding.cardView.setCardBackgroundColor(Color.TRANSPARENT)
            }
            2 -> (holder.messageItemBinding.cardView.layoutParams
                    as ViewGroup.MarginLayoutParams).marginStart = 0
        }

        return holder
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return when(message.send_by){
            DataBase.getCurrentUser().uid -> 0
            "system" -> 1
            else -> 2
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int, message: Message) {
        if(itemCount - 1 == position) chatActivity.scrollDown()
        val context = holder.messageItemBinding.root.context
        val currentUser = DataBase.getCurrentUser()

        if(roomType == "group" && message.send_by != "system") DataBase.getUserByUid(message.send_by).get()
            .addOnSuccessListener { snap ->
            val user = snap.getValue(User::class.java)?: return@addOnSuccessListener
            var userName = user.phone_number

            Contact.getContacts(context).forEach { contact ->
                if(contact.phoneNumber == user.phone_number){
                    userName = contact.name
                    return@forEach
                }
            }

            (holder.messageItemBinding.message.layoutParams as ViewGroup.MarginLayoutParams).topMargin = 0
            holder.messageItemBinding.userName.visibility = View.VISIBLE
            holder.messageItemBinding.userName.text = userName
        }else if(roomType == "chat") {
            if((message.read_status == 0 || message.read_status == 1) && message.send_by != currentUser.uid) {
                // TODO()
            }
        }
        holder.messageItemBinding.message.text = message.text_message
        holder.messageItemBinding.timeMessage.text = if(message.send_by != "system")
            DateFormat.format("HH:mm", message.time_message) else ""
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageItemBinding = MessageItemBinding.bind(itemView)
    }
}