package com.happyworldgames.messenger.adapters

import android.app.Activity
import android.content.Intent
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.happyworldgames.messenger.ChatActivity
import com.happyworldgames.messenger.R
import com.happyworldgames.messenger.data.DataBase
import com.happyworldgames.messenger.data.Room
import com.happyworldgames.messenger.data.Storage
import com.happyworldgames.messenger.databinding.ChatItemBinding

class ChatsRecyclerAdapter(options: FirebaseRecyclerOptions<Room>) : FirebaseRecyclerAdapter<Room,
        ChatsRecyclerAdapter.ChatViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent,
            false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int, model: Room) {
        val context = holder.chatItemBinding.root.context

        DataBase.getRoomNameAndAvatarByRoom(context, model) { name, avatarPath ->
            holder.chatItemBinding.chatName.text = name

            Storage.getAvatarUriByPath(avatarPath){
                Glide.with(context)
                    .load(it)
                    .into(holder.chatItemBinding.avatarIcon)
            }
        }
        DataBase.getRoomLastMessageByRoom(model){ lastMessage ->
            if(lastMessage == null) return@getRoomLastMessageByRoom

            val textMessage = lastMessage.text_message
            val timeMessage = lastMessage.time_message
            holder.chatItemBinding.lastMessage.text = if(textMessage.length > 20) textMessage.substring(0, 20)
            else textMessage
            holder.chatItemBinding.timeLastMessage.text = DateFormat.format("HH:mm", timeMessage)
        }
        holder.chatItemBinding.root.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)

            intent.apply {
                putExtra("room_type", model.room_type)
                putExtra("room_id", model.room_id)
            }

            val p1 = Pair.create(holder.chatItemBinding.chatName as View, "chat_name")
            val p2 = Pair.create(holder.chatItemBinding.avatarIcon as View, "avatar_icon")

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(context as Activity, p1, p2)

            context.startActivity(intent, options.toBundle())
        }
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chatItemBinding = ChatItemBinding.bind(itemView)
    }
}