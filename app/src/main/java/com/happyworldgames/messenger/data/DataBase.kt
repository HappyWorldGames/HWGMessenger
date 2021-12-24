package com.happyworldgames.messenger.data

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.happyworldgames.messenger.ChatActivity

/*
/users/%uid%{phone_number, avatar_path}                                 //getUserByUid, getUidByPhoneNumber
/rooms/%uid%/{                                                          //getUserRoomsByUid
    %room%{
        room_type = (chat, group)
        room_id = if room_type == "chat" then %chat_id% else %group_id%
    }
}
/chats/%chat_id%{                                                       //getChatByChatId
    messages/%message%{
        send_by = (%uid%, system)
        text_message
        read_status
        time_message
    }
    members/%uid%{true}
}
/groups/%group_id%{                                                     //getGroupByGroupId
    info/{
        group_name = %group_name%
        icon_path = "/default_avatar.png"
    }
    messages/%message%{
        send_by = (%uid%, system)
        text_message
        read_status
        time_message
    }
    members/%uid%{is_admin: boolean}
}
*/
class DataBase {

    companion object {
        fun getInstance(): FirebaseDatabase = FirebaseDatabase
            .getInstance("https://private-chat-629f1-default-rtdb.europe-west1.firebasedatabase.app/")
        fun getDatabaseReferenceResult(databaseReference: DatabaseReference,
                                       result: (dataSnapshot: DataSnapshot?) -> Unit) {
            databaseReference.get().addOnSuccessListener {
                result(it)
            }.addOnFailureListener {
                result(null)
            }
        }

        fun getUidByPhoneNumber(phoneNumber: String, result: (uid: String?) -> Unit) {
            getInstance().getReference("users").orderByChild("phone_number")
                .equalTo(phoneNumber).addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        result(snapshot.getValue(User::class.java)?.uid)
                    }
                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                    override fun onChildRemoved(snapshot: DataSnapshot) {}
                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                    override fun onCancelled(error: DatabaseError) {}
                })
        }
        fun getUserByUid(uid: String): DatabaseReference = getInstance()
            .getReference("users").child(uid)
        fun getUserRoomsByUid(uid: String): DatabaseReference = getInstance()
            .getReference("rooms").child(uid)
        fun getChatByChatId(chatId: String): DatabaseReference = getInstance()
            .getReference("chats").child(chatId)
        fun getGroupByGroupId(groupId: String): DatabaseReference = getInstance()
            .getReference("groups").child(groupId)

        fun getRoomNameAndAvatarByRoom(context: Context, room: Room,
                                       result: (name: String, avatarPath: String) -> Unit) {
            if(room.room_type == "chat"){
                getChatByChatId(room.room_id).child("members").get().addOnSuccessListener { v ->
                    val r = v.value as HashMap<*, *>
                    r.remove(getCurrentUser().uid)
                    getUserByUid(r.keys.toList()[0].toString()).get().addOnSuccessListener user@ { obj ->
                        val user = obj.getValue(User::class.java) ?: return@user
                        var name: String = user.phone_number
                        Contact.getContacts(context).forEach { contact ->
                            if(contact.phoneNumber == user.phone_number){
                                name = contact.name
                                return@forEach
                            }
                        }
                        result(name, user.avatar_path)
                    }
                }
            }else if(room.room_type == "group") {
                getGroupByGroupId(room.room_id).child("info").get().addOnSuccessListener {
                        gGroup ->
                    val group = gGroup.getValue(Group::class.java) ?: return@addOnSuccessListener
                    result(group.group_name, group.icon_path)
                }
            }
        }
        fun getRoomLastMessageByRoom(room: Room, result: (lastMessage: Message?) -> Unit) {
            val databaseReference: DatabaseReference = when(room.room_type){
                "chat" -> getChatByChatId(room.room_id)
                "group" -> getGroupByGroupId(room.room_id)
                else -> null
            } ?: return
            databaseReference.child("messages")
                .orderByChild("time_message").limitToLast(1)
                .addChildEventListener(object : ChildEventListener{
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        val message = snapshot.getValue(Message::class.java)
                        if(message != null){
                            getUserRoomsByUid(getCurrentUser().uid).orderByChild("room_id")
                                .equalTo(room.room_id).addChildEventListener(object : ChildEventListener {
                                    override fun onChildAdded(snapshot: DataSnapshot,
                                                              previousChildName: String?) {
                                        val room2 = snapshot.getValue(Room::class.java) ?: return
                                        room2.reverse_time_last_message =
                                            Long.MAX_VALUE - message.time_message
                                        snapshot.ref.setValue(room2)
                                    }
                                    override fun onChildChanged(snapshot: DataSnapshot,
                                                                previousChildName: String?) {}
                                    override fun onChildRemoved(snapshot: DataSnapshot) {}
                                    override fun onChildMoved(snapshot: DataSnapshot,
                                                              previousChildName: String?) {}
                                    override fun onCancelled(error: DatabaseError) {}

                                })
                        }
                        result(message)
                    }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
        }

        fun getCurrentUser(): FirebaseUser = FirebaseAuth.getInstance().currentUser!!
        fun updateUserInfo(avatar_path: String = "/default_avatar.png") {
            val currentUser = getCurrentUser()
            val userInfo = getUserByUid(currentUser.uid)

            userInfo.setValue(User(currentUser.uid, currentUser.phoneNumber!!), avatar_path)
        }
        fun createChat(context: Context, otherUid: String) {
            val currentUid = getCurrentUser().uid

            val chatReference = getInstance().getReference("chats").push()
            if(chatReference.key == null) return
            chatReference.child("members").setValue(hashMapOf(currentUid to true,
                otherUid to true))
            chatReference.child("messages").push()
                .setValue(Message("system", "chat created"), -1)

            val createChatByUid = fun(uid: String) {
                getUserRoomsByUid(uid).push().setValue(Room("chat", chatReference.key!!))
            }
            createChatByUid(currentUid)
            createChatByUid(otherUid)

            val intent = Intent(context, ChatActivity::class.java)
            intent.apply {
                putExtra("room_type", "chat")
                putExtra("room_id", chatReference.key)
            }
            context.startActivity(intent)
        }

        fun saveLastSyncResult(context: Context, rooms: List<Room>) {
            val chatShared = context.getSharedPreferences("chats", Context.MODE_PRIVATE).edit()
            val groupShared = context.getSharedPreferences("groups", Context.MODE_PRIVATE).edit()
            chatShared.clear()
            groupShared.clear()
            rooms.forEach { room ->
                val r = when(room.room_type){
                    "chat" -> chatShared
                    "group" -> groupShared
                    else -> null
                }
                r?.putLong(room.room_id, room.reverse_time_last_message)
            }
            chatShared.apply()
            groupShared.apply()
        }
        fun loadLastSyncResult(context: Context): List<Room> {
            val chatShared = context.getSharedPreferences("chats", Context.MODE_PRIVATE)
            val groupShared = context.getSharedPreferences("groups", Context.MODE_PRIVATE)
            val rooms = arrayListOf<Room>()

            val sharedAddToRooms = fun(roomType: String, sharedPreferences: SharedPreferences){
                if(sharedPreferences.all.isNotEmpty()) for(entry in sharedPreferences.all.entries) {
                    val chatId = entry.key
                    val timeLastMessage = entry.value as Long
                    rooms.add(Room(roomType, chatId, timeLastMessage))
                }
            }

            sharedAddToRooms("chat", chatShared)
            sharedAddToRooms("group", groupShared)

            return rooms
        }
    }
}