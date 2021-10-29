package com.happyworldgames.privatechat.data

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

class Storage {

    companion object {
        private fun getInstance(): FirebaseStorage = FirebaseStorage
            .getInstance("gs://private-chat-629f1.appspot.com")

        fun getAvatarUriByPath(path: String, result: (url: Uri) -> Unit) {
            val storage = getInstance().reference
            val imageUrl = storage.child(path)
            imageUrl.downloadUrl.addOnSuccessListener {
                result(it)
            }
        }
    }
}