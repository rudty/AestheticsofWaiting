package org.waiting.aestheticsofwaiting.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

/**
 * Created by d on 2017-01-29.
 */
class FirebaseTokenReceivingService : FirebaseInstanceIdService(){

    override fun onTokenRefresh() {

        //refreshedToken : fcm 보낼때 씀
        //myId : db 저장용
        val refreshedToken = FirebaseInstanceId.getInstance().token
        val myId = FirebaseAuth.getInstance().currentUser?.uid

        if(refreshedToken != null && myId != null){
            val mDatabase = FirebaseDatabase.getInstance().getReference("user")
            mDatabase.child(myId)
                    .setValue(refreshedToken)

        }

    }
}