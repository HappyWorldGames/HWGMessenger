package com.happyworldgames.privatechat.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.provider.ContactsContract

class Contact(var name: String = "", var phoneNumber: String = ""){

    companion object {

        private fun getAcceptContactPermission(context: Context): Boolean = Build.VERSION.SDK_INT < 23 ||
                context.checkSelfPermission(Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED

        fun getContacts(context: Context): List<Contact> {
            if(!getAcceptContactPermission(context)) return emptyList()
            val phones: Cursor = context.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
                ?: return arrayListOf()

            val contacts = arrayListOf<Contact>()
            while (phones.moveToNext()) {
                val name: String = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber: String = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                contacts.add(Contact(name, phoneNumber.filter { it.isDigit() || it == '+' }))
            }
            phones.close()

            return contacts.distinctBy { it.phoneNumber }
        }
    }

}