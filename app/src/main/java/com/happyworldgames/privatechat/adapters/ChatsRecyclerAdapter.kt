package com.happyworldgames.privatechat.adapters

import android.content.Intent
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.happyworldgames.privatechat.ChatActivity
import com.happyworldgames.privatechat.data.Chat
import com.happyworldgames.privatechat.R
import com.happyworldgames.privatechat.databinding.ChatItemBinding

class ChatsRecyclerAdapter(options: FirebaseRecyclerOptions<Chat>) : FirebaseRecyclerAdapter<Chat, ChatsRecyclerAdapter.ChatViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int, model: Chat) {
        holder.chatItemBinding.chatName.text = model.chatName
        holder.chatItemBinding.lastMessage.text = if(model.lastMessage.length > 20) model.lastMessage.substring(0, 20) else model.lastMessage
        holder.chatItemBinding.timeLastMessage.text = DateFormat.format("HH:mm", model.timeLastMessage)
        if(model.chatIcon != null) holder.chatItemBinding.avatarIcon.setImageBitmap(model.chatIcon)
        else holder.chatItemBinding.avatarIcon.setImageResource(R.drawable.ic_avatar)

        holder.chatItemBinding.root.setOnClickListener {
            val context = holder.chatItemBinding.root.context
            val intent = Intent(context, ChatActivity::class.java)

            intent.apply {
                putExtra("chat_name", model.chatName)
                putExtra("user_uid", model.userUid)
            }

            context.startActivity(intent)
        }
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chatItemBinding = ChatItemBinding.bind(itemView)
    }
}