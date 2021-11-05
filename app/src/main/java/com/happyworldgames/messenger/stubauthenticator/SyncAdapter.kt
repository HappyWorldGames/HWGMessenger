package com.happyworldgames.messenger.stubauthenticator

import android.accounts.Account
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import com.google.firebase.FirebaseApp
import com.happyworldgames.messenger.R
import com.happyworldgames.messenger.data.Contact
import com.happyworldgames.messenger.data.DataBase
import com.happyworldgames.messenger.data.Room
import com.happyworldgames.messenger.data.User

class SyncAdapter @JvmOverloads constructor(
    context: Context,
    autoInitialize: Boolean,
    /**
     * Using a default argument along with @JvmOverloads
     * generates constructor for both method signatures to maintain compatibility
     * with Android 3.0 and later platform versions
     */
    allowParallelSyncs: Boolean = false,
    /*
     * If your app uses a content resolver, get an instance of it
     * from the incoming Context
     */
    val mContentResolver: ContentResolver = context.contentResolver
) : AbstractThreadedSyncAdapter(context, autoInitialize, allowParallelSyncs) {

    override fun onPerformSync(account: Account, extras: Bundle, authority: String,
                               provider: ContentProviderClient,
                               syncResult: SyncResult) {
        FirebaseApp.initializeApp(context)
        val newMessage = fun(room: Room){
            noti(room)
        }
        DataBase.getDatabaseReferenceResult(DataBase.getUserRoomsByUid(DataBase.getCurrentUser().uid)){ r ->
            if(r == null) return@getDatabaseReferenceResult

            val lastRooms = DataBase.loadLastSyncResult(context)
            val newRooms = arrayListOf<Room>()

            //if(lastRooms.isEmpty() && r.childrenCount > 0) newMessage()   //fix new message at new user
            r.children.forEach { dataSnapshot ->
                val room = dataSnapshot.getValue(Room::class.java)
                if(room != null){
                    DataBase.getRoomLastMessageByRoom(room) { message ->
                        if(message == null) return@getRoomLastMessageByRoom
                        room.reverse_time_last_message = message.time_message
                        newRooms.add(room)
                        if(lastRooms.isNotEmpty()) lastRooms.forEach lastRoomsFor@ { lastRoom ->
                            if(lastRoom.room_type == room.room_type && lastRoom.room_id == room.room_id) {
                                if(lastRoom.reverse_time_last_message < room.reverse_time_last_message)
                                    newMessage(room)
                                else return@lastRoomsFor
                            }
                        }
                        DataBase.saveLastSyncResult(context, newRooms)
                    }
                }
            }
        }
    }

    fun noti(room: Room) {
        val NOTIFICATION_ID = 101
        val CHANNEL_ID = "channelID"

        DataBase.getRoomLastMessageByRoom(room) { message ->
            if(message == null) return@getRoomLastMessageByRoom
            DataBase.getDatabaseReferenceResult(DataBase.getUserByUid(message.send_by)) {
                if(it == null) return@getDatabaseReferenceResult
                val user = it.getValue(User::class.java) ?: return@getDatabaseReferenceResult
                var name = user.phone_number
                Contact.getContacts(context).forEach { contact ->
                    if(contact.phoneNumber == user.phone_number){
                        name = contact.name
                        return@forEach
                    }
                }

                val person = Person.Builder().setName(name).build()
                val messagingStyle = NotificationCompat.MessagingStyle(person)
                    .addMessage(message.text_message, message.time_message, person)

                val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_avatar)
                    /*.setContentTitle(name)
                    .setContentText(message.text_message)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)*/
                    .setStyle(messagingStyle)

                with(NotificationManagerCompat.from(context)) {
                    createChannel(CHANNEL_ID, "New Message")
                    notify(NOTIFICATION_ID, builder.build())
                }
            }
        }
    }
    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Some one write new message for you"

            val notificationManager = context.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)

        }
    }

}