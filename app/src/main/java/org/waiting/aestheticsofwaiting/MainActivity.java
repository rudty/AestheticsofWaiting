package org.waiting.aestheticsofwaiting;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tsengvn.typekit.TypekitContextWrapper;
import com.viewpagerindicator.CirclePageIndicator;

import org.waiting.aestheticsofwaiting.nfc.NFCActivity;

/**
 * Created by d on 2017-01-27.
 * NFC 인식하고 예약을 진행했을때
 * 진행상황을 알 수 있는 Activity.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private FirebaseUser mUser;
    private ViewPager mViewPager;
    private CirclePageIndicator mIndicator;
    private FragmentAdapter mFragmentAdapter;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mNfcPendingIntent;
    private String mShopId = null;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);

        Intent intent = getIntent();
        //보통 가게 전화번호가 넘어옴
        //shop info fragment
        Log.e(TAG, "" + mShopId);
        mShopId = intent.getStringExtra("reservation");

        //shop info fragment
        Log.e(TAG, "" + mShopId + " " + mFragmentAdapter);

        //로그인 했을때만 접근하므로
        //null 예외 없음
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.e(TAG, mUser.getDisplayName());
        Log.e(TAG, mUser.getEmail());
        Log.e(TAG, mUser.getProviderId());

        mFragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), mShopId, mUser.getUid());
        mViewPager.setAdapter(mFragmentAdapter);
        mIndicator.setViewPager(mViewPager);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
        Intent nfcIntent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mNfcPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, nfcIntent, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, null, null);
    }

    public void onNfcDataParseFail() {
        //다시 시도해주세요. 띄움
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage("인식에 실패했습니다. 다시 해주세요")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getApplicationContext(), NFCActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                        finish();
                    }
                })
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (mNfcAdapter != null)
                mNfcAdapter.disableForegroundDispatch(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }
    //    private void changeFragment(final android.support.v4.app.Fragment f) {
//        getSupportFragmentManager()
//                .beginTransaction()
//                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
//                .replace(R.id.container, f)
//                .commit();
//
//    }
}

//        FirebaseDatabase.getInstance().getReference("shop").child("021231234").orderByKey()
//                .addChildEventListener(new ChildEventListener() {
//                    @Override
//                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                        Log.e(TAG, dataSnapshot.getKey() +" "+ dataSnapshot.getValue());
//                    }
//
//                    @Override
//                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//                    }
//
//                    @Override
//                    public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//                    }
//
//                    @Override
//                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//        .addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
//                for(Map.Entry<String, Object> e : map.entrySet()){
//                    Log.e("DD",e.getKey() +" "+e.getValue());
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.e("Error", databaseError.toString());
//            }
//        });
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                getSupportFragmentManager()
//                        .beginTransaction()
//                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
//                        .replace(R.id.container, nfcFragment2)
//                        .commit();
//            }
//        }, 3000);
