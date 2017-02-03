package org.waiting.aestheticsofwaiting.login;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.waiting.aestheticsofwaiting.R;

public class SignInManager implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = SignInManager.class.getSimpleName();
    private static final int RC_SIGN_IN = 9001;

    public interface SignInCallback {
        void  onSignInSuccess(@NonNull FirebaseUser user);
        void onSignInFail();
    }

    private final FragmentActivity mFragmentActivity;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    private SignInCallback mLoginListener = new SignInCallback() {
        @Override
        public void onSignInSuccess(@NonNull FirebaseUser user) {
        }

        @Override
        public void onSignInFail() {
        }
    };

    private final FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                mLoginListener.onSignInSuccess(user);
            }
        }
    };

    public SignInManager(FragmentActivity activity) {
        this.mFragmentActivity = activity;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(mFragmentActivity)
                .enableAutoManage(mFragmentActivity, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();
    }

    public boolean isSigned(){
        return (FirebaseAuth.getInstance().getCurrentUser() != null);
    }

    public void startSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        mFragmentActivity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.e("succ","dd"+result.isSuccess());
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
            else{
                mLoginListener.onSignInFail();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(mFragmentActivity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            mLoginListener.onSignInFail();
                        } else {
                            mLoginListener.onSignInSuccess(task.getResult().getUser());
                        }
                    }
                });
    }

    public void onStart() {
        mAuth.addAuthStateListener(mAuthListener);
    }

    public void onStop() {
        mAuth.removeAuthStateListener(mAuthListener);
    }

    public void setOnLoginListener(SignInCallback listener) {
        this.mLoginListener = listener;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

}