package com.happyworldgames.privatechat.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.happyworldgames.privatechat.ChatActivity
import com.happyworldgames.privatechat.R
import com.happyworldgames.privatechat.data.Contact
import com.happyworldgames.privatechat.data.DataBase
import com.happyworldgames.privatechat.databinding.ContactItemBinding

class ContactsRecyclerAdapter(private val contacts: Set<Contact>)
    : RecyclerView.Adapter<ContactsRecyclerAdapter.ContactHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.contact_item,
            parent, false)
        return ContactHolder(view)
    }

    override fun onBindViewHolder(holder: ContactHolder, position: Int) {
        val element = contacts.elementAt(position)

        holder.contactItemBinding.contactName.text = element.name
        holder.contactItemBinding.status.text = element.phoneNumber

        holder.contactItemBinding.root.setOnClickListener {
            val context = holder.contactItemBinding.root.context
            val intent = Intent(context, ChatActivity::class.java)

            DataBase.getUidByPhoneNumber(element.phoneNumber) { uid ->
                TODO()
            }
            intent.apply {
                //putExtra("room_type", room.room_type)
                //putExtra("room_id", room.room_id)
            }

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = contacts.size

    class ContactHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val contactItemBinding = ContactItemBinding.bind(itemView)
    }

}