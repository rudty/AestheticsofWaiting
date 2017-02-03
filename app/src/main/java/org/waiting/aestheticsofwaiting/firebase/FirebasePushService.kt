package org.waiting.aestheticsofwaiting.firebase

import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.waiting.aestheticsofwaiting.R

/**
 * Created by d on 2017-01-29.
 */
class FirebasePushService : FirebaseMessagingService(){
    companion object val TAG : String = this.javaClass.name

    private val notificationManager by lazy{ getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager}

    override fun onMessageReceived(msg: RemoteMessage) {
        val nofi = msg.data

        val title = ""+nofi["title"]
        val msg = ""+nofi["text"]

        notificationManager.notify(0,
        NotificationCompat.Builder(applicationContext)
                .setContentTitle(title)
                .setContentText(msg)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0L, 1000L))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.mipmap.ic_launcher)
                .build()
        )

    }
}