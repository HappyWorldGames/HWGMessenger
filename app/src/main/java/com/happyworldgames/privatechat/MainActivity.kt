package com.happyworldgames.privatechat

import android.Manifest
import android.app.Activity
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
import com.happyworldgames.privatechat.adapters.ChatsRecyclerAdapter
import com.happyworldgames.privatechat.data.DataBase
import com.happyworldgames.privatechat.data.Room
import com.happyworldgames.privatechat.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val activityMain: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private var adapter: ChatsRecyclerAdapter? = null

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }
    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
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

        activityMain.addChatFab.visibility = View.VISIBLE

        val query: Query = DataBase.getUserRoomsByUid(DataBase.getCurrentUser().uid)
            .limitToLast(50)

        val options = FirebaseRecyclerOptions.Builder<Room>()
            .setQuery(query, Room::class.java)
            .build()

        adapter = ChatsRecyclerAdapter(options)

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
}