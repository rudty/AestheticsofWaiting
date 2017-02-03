package org.waiting.aestheticsofwaiting.firebase

import android.app.ProgressDialog
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import java.util.*

/**
 * Created by d on 2017-01-27.
 */
class ShopDatabase {

    interface PhoneNumberCallback {
        fun onPhoneNumberDataReceive(phoneNumber: String?)
    }

    interface LocationCallback {
        fun onLocationReceive(lat: Double, lng: Double)
    }

    interface WaitingCallback {
        fun onWaitingReceive(time: Int)
    }

    interface WaitingPeopleCallback {
        fun onWaitingPeoples(people: Int)
    }

    interface ShopNameCallback {
        fun onShopNameReceive(name: String)
    }

    interface ShopMenuCallback {
        fun onMenuReceive(menu: java.util.Map<java.lang.String, Any>)
    }

    interface ReservationCancelCallback {
        fun onReservationCancelSuccess(b: Boolean)
    }

    companion object {
        private val TAG = this.javaClass.name
        private val LOCATION_PATH = "location/"
        private val MENU_PATH = "menu/"
        private val SHOP_PATH = "shop/"
        private val SHOP_NAME_PATH = "shopname/"
        private val CODE_PATH = "code/"
        private val RESERVATION_PATH = "reservation/"
    }

    private val mDatabase by lazy { FirebaseDatabase.getInstance() }

    /**
     * 가게 전화번호를 가져오는 함수
     */
    fun getShopPhoneNumber(code: String, readCallback: PhoneNumberCallback?) {
        mDatabase.getReference(CODE_PATH + code).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                val shopId = dataSnapshot?.getValue(String::class.java)
                readCallback?.onPhoneNumberDataReceive(shopId)
            }
        })
    }

    /**
     * code : 인식한 결과,
     * id : 고유번호(폰번호, 토큰값 등)
     * 비동기로 실행 즉시 반환
     */
    fun reservation(shopId: String, id: String): String {
        val reservationTime = System.currentTimeMillis().toString()
        mDatabase.getReference(SHOP_PATH + shopId)
                .child(reservationTime)
                .setValue(id)
        mDatabase.getReference(RESERVATION_PATH + id)
                .setValue(shopId)
        return reservationTime
    }

    fun reservationPostpone(shop_id: String, id: String, callback: ReservationCancelCallback) {
        calcWaitingPeople(shop_id, id, object : WaitingPeopleCallback {
            override fun onWaitingPeoples(people: Int) {
                if (people == 1) {
                    callback.onReservationCancelSuccess(false)
                } else {
                    callback.onReservationCancelSuccess(true)
                    removeReservation(shop_id, id, Runnable {
                        val reservationTime = System.currentTimeMillis().toString()
                        mDatabase.getReference(SHOP_PATH + shop_id)
                                .child(reservationTime)
                                .setValue(id)
                    })
                }
            }
        })
    }

    fun reservationCancel(shop_id: String, id: String) {
        removeReservation(shop_id, id, null)
        mDatabase.getReference(RESERVATION_PATH + id).removeValue()
    }


    private fun removeReservation(shop_id: String, id: String, finishListener: Runnable?) {
        mDatabase.getReference(SHOP_PATH + shop_id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                try {
                    val dataMap = dataSnapshot.value as Map<String, Any>
                    for ((key, value) in dataMap) {
                        if (value == id) {
                            mDatabase.getReference(SHOP_PATH + shop_id)
                                    .child(key).removeValue()
                            break
                        }
                    }
                    finishListener?.run()
                } catch (e: Exception) {

                }
            }

        })
    }

    fun getShopLocation(shop_id: String, callback: LocationCallback) {
        mDatabase.getReference(LOCATION_PATH + shop_id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                val data = dataSnapshot?.getValue(String::class.java)
                if (data != null) {
                    val location = data.split("/")
                    callback.onLocationReceive(location[0].toDouble(), location[1].toDouble())
                } else {
                    //fail
                    callback.onLocationReceive(-1024.0, -1024.0)
                }

            }

        })
    }

    private fun calcWaitingPeople(shop_id: String, id: String, callback: WaitingPeopleCallback) {
        mDatabase.getReference(SHOP_PATH + shop_id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dataMap = dataSnapshot.value as Map<String, Any>
                callback.onWaitingPeoples(dataMap.size)
            }

        })
    }

    fun startWatchingMyWaitingTime(shop_id: String, id: String, callback: WaitingCallback) {

        mDatabase.getReference(SHOP_PATH + shop_id).addValueEventListener(object : ValueEventListener, Comparator<Map.Entry<String, Any>> {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //map 을 arrayList 로 바꿔놓고 정렬함.
                try {
                    val dataMap = dataSnapshot.value as Map<String, Any>
                    val arrayList = ArrayList<Map.Entry<String, Any>>(dataMap.entries)
                    Collections.sort(arrayList, this)

                    //대기순서
                    var count = 0
                    for (e in arrayList) {
                        count += 1
                        if (e.value == id) {
                            return callback.onWaitingReceive(count)
                        }
                    }
                } catch(e: Exception) {
                    mDatabase.getReference(SHOP_PATH + shop_id).removeEventListener(this)
                }
                return callback.onWaitingReceive(0)
            }

            override fun onCancelled(err: DatabaseError?) {
            }

            override fun compare(lhs: Map.Entry<String, Any>, rhs: Map.Entry<String, Any>): Int {
                return lhs.key.toLong().compareTo(rhs.key.toLong())
            }

        })
//        mDatabase.getReference(SHOP_PATH + shop_id).orderByValue().equalTo(id).addListenerForSingleValueEvent(object : ValueEventListener{
//            override fun onCancelled(p0: DatabaseError?) {
//            }
//
//            override fun onDataChange(dataSnapshot: DataSnapshot?) {
//                //아직도 대기중이면 not null
//                //대기가 끝났으면 null
//                val isWaiting = dataSnapshot?.value
//                if(isWaiting != null){
//                    //대기중이면 계산하게 한다.
//                    calcWaitingTime(shop_id, id, callback)
//                }
//                else{
//                    //그렇지 않을때는 0을 보냄 (자신의 차례인데 끄지 않았거나.. 했을때 )
//                    callback.onWaitingReceive(0)
//                }
//            }
//
//        })

    }

    private fun calcWaitingTime(shop_id: String, id: String, callback: WaitingCallback) {
        var count = 1
        mDatabase.getReference(SHOP_PATH + shop_id)
                .orderByKey()
                .addChildEventListener(object : ChildEventListener {
                    override fun onCancelled(dataSnapshot: DatabaseError?) {
                    }

                    override fun onChildRemoved(dataSnapshot: DataSnapshot?) {
                    }

                    override fun onChildAdded(dataSnapshot: DataSnapshot?, s: String?) {
                        dataSnapshot?.getValue(Object::class.java)?.let {
                            if (it.equals(id) == false) {
                                count += 1
                            } else {
                                mDatabase.getReference(SHOP_PATH + shop_id).removeEventListener(this)
                                callback.onWaitingReceive(count)
                            }
                            //Log.e(TAG, it)
                        }
                    }

                    override fun onChildChanged(dataSnapshot: DataSnapshot?, s: String?) {
                    }

                    override fun onChildMoved(dataSnapshot: DataSnapshot?, s: String?) {
                    }
                })
    }

    fun getShopMenu(shop_id: String, callback: ShopMenuCallback) {
        mDatabase.getReference(MENU_PATH + shop_id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.value?.let {
                    callback.onMenuReceive(it as java.util.Map<java.lang.String, Any>)
                }
            }

        })
    }

    fun getShopName(shop_id: String, callback: ShopNameCallback) {
        mDatabase.getReference(SHOP_NAME_PATH + shop_id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.getValue(String::class.java)?.let {
                    callback.onShopNameReceive(it)
                }
            }

        })
    }
}