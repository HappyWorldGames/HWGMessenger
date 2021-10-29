package com.happyworldgames.privatechat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.happyworldgames.privatechat.adapters.ContactsRecyclerAdapter
import com.happyworldgames.privatechat.data.Contact
import com.happyworldgames.privatechat.data.DataBase
import com.happyworldgames.privatechat.databinding.ActivityContactsBinding

class ContactsActivity : AppCompatActivity() {

    private val readContactsPermissionRequestCode = 1
    private val activityContacts: ActivityContactsBinding by lazy { ActivityContactsBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityContacts.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), readContactsPermissionRequestCode)
        }else start()
    }

    private fun start() {
        activityContacts.contactsRecycler.layoutManager = LinearLayoutManager(this)
        activityContacts.contactsRecycler.adapter = ContactsRecyclerAdapter(getUseAppContacts(Contact.getContacts(this)))
    }

    private fun getUseAppContacts(contacts: List<Contact>): Set<Contact> {
        val result = HashSet<Contact>()
        contacts.forEach { contact ->
            DataBase.getUidByPhoneNumber(contact.phoneNumber) { uid ->
                if (uid == null) return@getUidByPhoneNumber

                result.add(contact)
                activityContacts.contactsRecycler.adapter?.notifyItemRangeChanged(0, result.size)
            }
        }
        return result
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == readContactsPermissionRequestCode && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            start()
        }
    }
}