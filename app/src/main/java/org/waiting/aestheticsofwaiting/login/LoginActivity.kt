package org.waiting.aestheticsofwaiting.login

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_login.*
import org.waiting.aestheticsofwaiting.MainActivity
import org.waiting.aestheticsofwaiting.R
import org.waiting.aestheticsofwaiting.nfc.NFCActivity


class LoginActivity : AppCompatActivity(), SignInManager.SignInCallback {
    companion object {
        private val TAG: String = this.javaClass.name
        private fun getConnectivityStatus(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val activeNetwork = cm.activeNetworkInfo
            if (null != activeNetwork) {
                if (activeNetwork.type == ConnectivityManager.TYPE_WIFI)
                    return true

                if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE)
                    return true
            }
            return false
        }
    }

    val mDisplayMetrics: DisplayMetrics by lazy { applicationContext.resources.displayMetrics }
    val mDeviceWidth by lazy { mDisplayMetrics.widthPixels }
    val mDeviceHeight by lazy { mDisplayMetrics.heightPixels }
    val mHandler = Handler()
    val mBeginTime = System.currentTimeMillis()
    //구글 로그인
    val mSignInManager by lazy { SignInManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //hide status bar
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_login)
        FirebaseMessaging.getInstance().subscribeToTopic("system")

        //로그아웃, 디버그용
        FirebaseAuth.getInstance().signOut()

        mSignInManager.setOnLoginListener(this)


        sign_in_button.setOnClickListener {
            mSignInManager.startSignIn()
        }

        if (mSignInManager.isSigned == false) {
            mHandler.postDelayed({

                //여기서 로그인 버튼 보이게
//                logo_image.visibility = View.GONE
                background.setImageResource(R.drawable.loading_login)
                google_login_layout.visibility = View.VISIBLE

            }, 1000)
        }


    }

    override fun onPause() {
        super.onPause()
    }

    override fun onSignInSuccess(user: FirebaseUser) {

        //여기서 로그인버튼 안보이게
        background.setImageResource(R.drawable.loading)
        google_login_layout.visibility = View.INVISIBLE
//        logo_image.visibility = View.VISIBLE


        Log.e(TAG, "uid: " + user.uid)
        Log.e(TAG, "cm_id: " + FirebaseInstanceId.getInstance().token)
        val db = FirebaseDatabase.getInstance()

        db.getReference("user")
                .child(user.uid)
                .setValue(FirebaseInstanceId.getInstance().token)

        //예약 테이블에서 예약했는지 확인
        //했으면 intent 로 reservation 값이 넘어감
        db.getReference("reservation")
                .child(user.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError?) {
                        Log.e(TAG, "cancel")
                        //예외. 프로그램 종료.
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                        //기본적으로 1.3초 이상 대기하게 함
                        val delayTime = 1300 - (System.currentTimeMillis() - mBeginTime)
                        mHandler.postDelayed({
                            val value = dataSnapshot?.getValue(String::class.java)
                            if (value != null) {
                                startActivity(Intent(applicationContext, MainActivity::class.java).let {
                                    if (value != null)
                                        it.putExtra("reservation", value)
                                    it
                                })
                            } else {
                                startActivity(Intent(applicationContext, NFCActivity::class.java))
                            }

                            finish()
                        }, Math.max(delayTime, 10))
                        //Log.e(TAG, "value :" + value)
                    }
                })
    }

    override fun onSignInFail() {
        //로그인 실패, 프로그램 종료

    }

    override fun onStart() {
        super.onStart()
        if (getConnectivityStatus(applicationContext)) {
            mSignInManager.onStart()
        } else {
            AlertDialog.Builder(this)
                    .setMessage("인터넷 연결후 다시 시도")
                    .setCancelable(false)
                    .setPositiveButton("확인", { e, w ->
                        finish()
                    }).show()
        }
    }

    override fun onStop() {
        super.onStop()
        mSignInManager.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mSignInManager.onActivityResult(requestCode, resultCode, data)
    }


}
