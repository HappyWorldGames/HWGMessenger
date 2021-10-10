package com.happyworldgames.privatechat

import android.app.Activity
import android.content.Intent
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
import com.happyworldgames.privatechat.data.Chat
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

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK){
                authTrue()

                DataBase.updateUserInfo()
                Snackbar.make(activityMain.root, getString(R.string.auth_accept), Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(activityMain.root, getString(R.string.auth_failed), Snackbar.LENGTH_LONG).show()
                finish()
            }
        }
        if(FirebaseAuth.getInstance().currentUser == null)
            resultLauncher.launch(AuthUI.getInstance().createSignInIntentBuilder().build())
        else authTrue()
    }

    private fun authTrue() {
        activityMain.addChatFab.visibility = View.VISIBLE

        val query: Query = DataBase.getChatsByUserUid(DataBase.getCurrentUser().uid)
            .limitToLast(50)

        val options = FirebaseRecyclerOptions.Builder<Chat>()
            .setQuery(query, Chat::class.java)
            .build()

        adapter = ChatsRecyclerAdapter(options)

        activityMain.chatsRecycler.layoutManager = LinearLayoutManager(this)
        activityMain.chatsRecycler.adapter = adapter

        activityMain.addChatFab.setOnClickListener {
            startActivity(Intent(this, FindUserActivity::class.java))
        }
    }
}