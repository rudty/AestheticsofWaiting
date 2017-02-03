package org.waiting.aestheticsofwaiting.nfc;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.tsengvn.typekit.TypekitContextWrapper;

import org.waiting.aestheticsofwaiting.MainActivity;
import org.waiting.aestheticsofwaiting.R;
import org.waiting.aestheticsofwaiting.firebase.ShopDatabase;

import java.io.UnsupportedEncodingException;


public class NFCActivity extends AppCompatActivity {

    private static final String TAG = NFCActivity.class.getName();

    private NfcAdapter mNfcAdapter;
    private PendingIntent mNfcPendingIntent;
    private Handler mHandler = new Handler();
    private TextView mTextView;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        mTextView = (TextView) findViewById(R.id.textView);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());

        if (mNfcAdapter.isEnabled() == false) {
            //NFC 장치를 켜주세요 ^^
            Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
            startActivity(intent);
        }

        Intent intent = new Intent(getApplicationContext(), NFCActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mNfcPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, null, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        // 앱이 종료될때 NFC 어댑터를 비활성화 한다
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);

    }

    /**
     * NFC Process
     * NFC
     *
     * @param intent
     */
    @Override
    public void onNewIntent(Intent intent) {
        String readMessage = readFromIntent(intent);
        if (readMessage != null) {
//            mTextView.setText(readMessage);
            reservation(readMessage);
        }
    }

    /**
     * 예약 실행하고 MainActivity 실행
     *
     * @param data
     */
    public void reservation(final String data) {
        //예약 실행
        //처리중입니다 알림창을 띄웁니다.
        final ShopDatabase db = new ShopDatabase();
        db.getShopPhoneNumber(data, new ShopDatabase.PhoneNumberCallback() {
            @Override
            public void onPhoneNumberDataReceive(@org.jetbrains.annotations.Nullable String phoneNumber) {
                String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                db.reservation(phoneNumber, myId);

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("reservation", phoneNumber);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
                finish();
            }
        });
    }

    /******************************************************************************
     * *********************************Read From NFC Tag***************************
     ******************************************************************************/
    private String readFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            return buildTagViews(msgs);
        }
        return null;
    }

    private String buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) return null;
        String text = "";
//        String tagId = new String(msgs[0].getRecords()[0].getType());
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
        int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"
        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");

        try {
            // Get the Text
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
        }

        return text;
    }

}


/******************************************************************************
 * *********************************Write to NFC Tag****************************
 ******************************************************************************/
//      myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//    private void write(String text, Tag tag) throws IOException, FormatException {
//        NdefRecord[] records = { createRecord(text) };
//        NdefMessage message = new NdefMessage(records);
//        // Get an instance of Ndef for the tag.
//        Ndef ndef = Ndef.get(tag);
//        // Enable I/O
//        ndef.connect();
//        // Write the message
//        ndef.writeNdefMessage(message);
//        // Close the connection
//        ndef.close();
//    }
//    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
//        String lang       = "en";
//        byte[] textBytes  = text.getBytes();
//        byte[] langBytes  = lang.getBytes("US-ASCII");
//        int    langLength = langBytes.length;
//        int    textLength = textBytes.length;
//        byte[] payload    = new byte[1 + langLength + textLength];
//
//        // set status byte (see NDEF spec for actual bits)
//        payload[0] = (byte) langLength;
//
//        // copy langbytes and textbytes into payload
//        System.arraycopy(langBytes, 0, payload, 1,              langLength);
//        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);
//
//        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  new byte[0], payload);
//
//        return recordNFC;
//    }
//