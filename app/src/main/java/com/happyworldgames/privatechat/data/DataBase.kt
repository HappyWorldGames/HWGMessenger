package com.happyworldgames.privatechat.data

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

/*
/users/%uid%{phone_number, avatar_path}                                 //getUserByUid, getUidByPhoneNumber
/rooms/%uid%/%room%{                                                    //getUserChatsByUid
    room_type = (chat, group)
    room_id = if room_type == "chat" then %chat_id% else %group_id%
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
        private fun getInstance(): FirebaseDatabase = FirebaseDatabase
            .getInstance("https://private-chat-629f1-default-rtdb.europe-west1.firebasedatabase.app/")

        fun getUidByPhoneNumber(phoneNumber: String, result: (uid: String?) -> Unit) {
            getInstance().getReference("users").orderByChild("phone_number")
                .equalTo(phoneNumber).get().addOnSuccessListener {
                    result(it.getValue(User::class.java)?.uid)
            }
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
                    val group = it.getValue(Group::class.java) ?: return@addOnSuccessListener
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
                        result(snapshot.getValue(Message::class.java))
                    }

                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                        TODO()
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }

        fun getCurrentUser(): FirebaseUser = FirebaseAuth.getInstance().currentUser!!
        fun updateUserInfo(avatar_path: String = "/default_avatar.png") {
            val currentUser = getCurrentUser()
            val userInfo = getUserByUid(currentUser.uid)

            userInfo.setValue(User(currentUser.uid, currentUser.phoneNumber!!), avatar_path)
        }
    }
}