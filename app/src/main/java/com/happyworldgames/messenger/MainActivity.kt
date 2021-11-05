package com.happyworldgames.messenger

import android.Manifest
import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.Query
import com.happyworldgames.messenger.adapters.ChatsRecyclerAdapter
import com.happyworldgames.messenger.data.DataBase
import com.happyworldgames.messenger.data.Room
import com.happyworldgames.messenger.databinding.ActivityMainBinding

// The authority for the sync adapter's content provider
const val AUTHORITY = "com.happyworldgames.com.happyworldgames.messenger.provider"
// An account type, in the form of a domain name
const val ACCOUNT_TYPE = "com.happyworldgames.com.happyworldgames.messenger"
// The account name
const val ACCOUNT = "Private Chat"

class MainActivity : AppCompatActivity() {

    private lateinit var mAccount: Account
    private val activityMain: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private var adapter: ChatsRecyclerAdapter? = null

    private fun saveLastSyncResult(){
        if(adapter == null || adapter!!.itemCount <= 0) return
        val newRooms = arrayListOf<Room>()
        for(i in 0..adapter!!.itemCount) {
            val room = adapter!!.getItem(i)
            DataBase.getRoomLastMessageByRoom(room) { message ->
                if(message == null) return@getRoomLastMessageByRoom
                room.reverse_time_last_message = message.time_message
                newRooms.add(room)
                DataBase.saveLastSyncResult(this, newRooms)
            }
        }
    }
    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }
    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
        saveLastSyncResult()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMain.root)

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result ->
            if (result.resultCode == Activity.RESULT_OK){
                DataBase.updateUserInfo()

                authTrue()
                Snackbar.make(activityMain.root, getString(R.string.auth_accept), Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(activityMain.root, getString(R.string.auth_failed), Snackbar.LENGTH_LONG).show()
                //finish()
            }
        }

        val providers = arrayListOf(AuthUI.IdpConfig.PhoneBuilder().build())

        if(FirebaseAuth.getInstance().currentUser == null)
            resultLauncher.launch(AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers).build())
        else authTrue()
    }

    private fun authTrue() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 1)
        }
        mAccount = createSyncAccount()
        DataBase.getInstance().setPersistenceEnabled(true)
        ContentResolver.addPeriodicSync(mAccount, AUTHORITY, Bundle.EMPTY, 60)

        activityMain.addChatFab.visibility = View.VISIBLE

        val query: Query = DataBase.getUserRoomsByUid(DataBase.getCurrentUser().uid)
            .orderByChild("reverse_time_last_message").limitToLast(50)

        val options = FirebaseRecyclerOptions.Builder<Room>()
            .setQuery(query, Room::class.java)
            .build()

        adapter = ChatsRecyclerAdapter(options)
        adapter?.startListening()
        saveLastSyncResult()

        activityMain.chatsRecycler.layoutManager = LinearLayoutManager(this)
        activityMain.chatsRecycler.adapter = adapter

        activityMain.addChatFab.setOnClickListener {
            startActivity(Intent(this, ContactsActivity::class.java))
        }
        activityMain.swipe.setOnRefreshListener {
            adapter?.notifyItemRangeChanged(0, adapter!!.itemCount)
            activityMain.swipe.isRefreshing = false
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    private fun createSyncAccount(): Account {
        val accountManager = getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
        return Account(ACCOUNT, ACCOUNT_TYPE).also { newAccount ->
            /*
             * Add the account and account type, no password or user data
             * If successful, return the Account object, otherwise report an error.
             */
            if (accountManager.addAccountExplicitly(newAccount, null, null)) {
                /*
                 * If you don't set android:syncable="true" in
                 * in your <provider> element in the manifest,
                 * then call context.setIsSyncable(account, AUTHORITY, 1)
                 * here.
                 */
            } else {
                /*
                 * The account exists or some other error occurred. Log this, report it,
                 * or handle it internally.
                 */
            }
        }
    }
}