package com.happyworldgames.messenger.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.happyworldgames.messenger.ChatActivity
import com.happyworldgames.messenger.R
import com.happyworldgames.messenger.data.Contact
import com.happyworldgames.messenger.data.DataBase
import com.happyworldgames.messenger.data.Room
import com.happyworldgames.messenger.databinding.ContactItemBinding

class ContactsRecyclerAdapter(private val contacts: Set<Contact>)
    : RecyclerView.Adapter<ContactsRecyclerAdapter.ContactHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.contact_item,
            parent, false)
        return ContactHolder(view)
    }

    override fun onBindViewHolder(holder: ContactHolder, position: Int) {
        val element = contacts.elementAt(position)
        val currentUser = DataBase.getCurrentUser()

        holder.contactItemBinding.contactName.text = element.name
        holder.contactItemBinding.status.text = element.phoneNumber

        holder.contactItemBinding.root.setOnClickListener {
            val context = holder.contactItemBinding.root.context
            val intent = Intent(context, ChatActivity::class.java)

            DataBase.getUidByPhoneNumber(element.phoneNumber) { uid ->
                if(uid == null || uid.isEmpty()) return@getUidByPhoneNumber
                DataBase.getDatabaseReferenceResult(DataBase.getUserRoomsByUid(currentUser.uid)) { rooms ->
                    if(rooms == null) return@getDatabaseReferenceResult

                    rooms.children.forEachIndexed { index, gRoom ->
                        val room = gRoom.getValue(Room::class.java) ?: return@forEachIndexed
                        val ref = if(room.room_type == "chat") DataBase.getChatByChatId(room.room_id)
                            else return@forEachIndexed
                        ref.child("members").get().addOnSuccessListener { gMembers ->
                            val s: HashMap<String, Boolean> = gMembers.value as HashMap<String, Boolean>
                            if(s.contains(uid)) {
                                intent.apply {
                                    putExtra("room_type", room.room_type)
                                    putExtra("room_id", room.room_id)
                                }
                                context.startActivity(intent)
                            }else if(index == rooms.childrenCount.toInt() - 1){
                                DataBase.createChat(context, uid)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = contacts.size

    class ContactHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val contactItemBinding = ContactItemBinding.bind(itemView)
    }

}