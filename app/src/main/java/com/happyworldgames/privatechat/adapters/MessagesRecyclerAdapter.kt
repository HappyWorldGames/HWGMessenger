package com.happyworldgames.privatechat.adapters

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.happyworldgames.privatechat.R
import com.happyworldgames.privatechat.data.Contact
import com.happyworldgames.privatechat.data.DataBase
import com.happyworldgames.privatechat.data.Message
import com.happyworldgames.privatechat.data.User
import com.happyworldgames.privatechat.databinding.MessageItemBinding

class MessagesRecyclerAdapter(options: FirebaseRecyclerOptions<Message>, val roomType: String) : FirebaseRecyclerAdapter<Message, MessagesRecyclerAdapter.MessageViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int, model: Message) {
        val context = holder.messageItemBinding.root.context

        if(model.send_by == DataBase.getCurrentUser().uid) (holder.messageItemBinding.cardView.layoutParams as ViewGroup.MarginLayoutParams).marginEnd = 0
        else (holder.messageItemBinding.cardView.layoutParams as ViewGroup.MarginLayoutParams).marginStart = 0

        if(roomType == "group") DataBase.getUserByUid(model.send_by).get().addOnSuccessListener {
            val user = it.getValue(User::class.java)?: return@addOnSuccessListener
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
        }
        holder.messageItemBinding.message.text = model.text_message
        holder.messageItemBinding.timeMessage.text = DateFormat.format("HH:mm", model.time_message)
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageItemBinding = MessageItemBinding.bind(itemView)
    }
}