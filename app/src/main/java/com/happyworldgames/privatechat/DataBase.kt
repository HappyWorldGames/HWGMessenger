package com.happyworldgames.privatechat

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.happyworldgames.privatechat.data.User

class DataBase {

    companion object {
        fun getInstance(): FirebaseDatabase = FirebaseDatabase.getInstance("https://private-chat-629f1-default-rtdb.europe-west1.firebasedatabase.app/")

        fun getUserByUid(uid: String): DatabaseReference = getInstance().reference.child(uid)
        fun getChatsByUserUid(uid: String): DatabaseReference = getUserByUid(uid).child("chats")
        fun getUserInfoByUid(uid: String): DatabaseReference = getUserByUid(uid).child("info")

        fun getCurrentUser(): FirebaseUser = FirebaseAuth.getInstance().currentUser!!
        fun updateUserInfo() {
            val currentUser = getCurrentUser()
            val userInfo = getUserInfoByUid(currentUser.uid)

            val name: String = if(currentUser.displayName != null) currentUser.displayName!! else "No Name"
            val email: String = if(currentUser.email != null) currentUser.email!! else "no email"

            userInfo.setValue(User(name, email, currentUser.uid))
        }
    }
}