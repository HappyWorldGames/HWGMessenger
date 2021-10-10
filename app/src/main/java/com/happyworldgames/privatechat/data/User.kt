package com.happyworldgames.privatechat.data

class User(var name: String = "", var email: String = "", var uid: String = "") {
    companion object {
        fun getUserFromHashMap(hashMap: HashMap<String, String>): User {
            return User(hashMap["name"]!!, hashMap["email"]!!, hashMap["uid"]!!)
        }
    }
}