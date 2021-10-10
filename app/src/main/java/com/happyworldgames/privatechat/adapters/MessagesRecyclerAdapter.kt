package com.happyworldgames.privatechat.adapters

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.happyworldgames.privatechat.R
import com.happyworldgames.privatechat.data.Message
import com.happyworldgames.privatechat.databinding.MessageItemBinding

class MessagesRecyclerAdapter(options: FirebaseRecyclerOptions<Message>) : FirebaseRecyclerAdapter<Message, MessagesRecyclerAdapter.MessageViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int, model: Message) {
        if(model.sendBy == "you") (holder.messageItemBinding.cardView.layoutParams as ViewGroup.MarginLayoutParams).marginEnd = 0
        else (holder.messageItemBinding.cardView.layoutParams as ViewGroup.MarginLayoutParams).marginStart = 0
        holder.messageItemBinding.message.text = model.textMessage
        holder.messageItemBinding.timeMessage.text = DateFormat.format("HH:mm", model.timeMessage)
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageItemBinding = MessageItemBinding.bind(itemView)
    }
}