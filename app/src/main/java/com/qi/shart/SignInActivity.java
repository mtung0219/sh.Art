package com.qi.shart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    SignInButton signInButton;
    Button signOut;
    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;
    private final int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private SignInButton signInGoogleButton;
    private Button signInPhoneButton;
    //private Button signInFacebookButton;
    private Button signOutButton;

    //*****************FB**********************
    private static final String EMAIL = "email";
    private CallbackManager mCallbackManager;
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        signInGoogleButton = findViewById(R.id.usingGoogle);
        signInPhoneButton = findViewById(R.id.usingPhone);
        //signInFacebookButton = findViewById(R.id.usingFB);
        signOutButton = findViewById(R.id.sign_out_button);
        signInGoogleButton.setOnClickListener(this);
        signInPhoneButton.setOnClickListener(this);
        //signInFacebookButton.setOnClickListener(this);
        signOutButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.usingGoogle:
                Log.w("TAG123", "starting google sign in");
                goToGoogleSignInActivity();
                break;
            case R.id.usingPhone:
                Log.w("TAG123", "starting phone sign in");
                goToPhoneSignInActivity();
                break;
            /*case R.id.usingFB:
                Log.w("TAG123", "starting FB sign in");
                goToFBSignInActivity();
                finish();
                break;*/
            case R.id.sign_out_button:
                Log.w("TAG123", "starting sign out");
                goToSignOut();
                break;
            default:
                break;
        }
    }


    private void goToGoogleSignInActivity() {
        Intent intent = new Intent(this, googleLogInActivity.class);
        startActivity(intent);
    }
    private void goToPhoneSignInActivity() {
        Intent intent = new Intent(this, phoneLogInActivity.class);
        startActivity(intent);
    }
    private void goToFBSignInActivity() {
        Intent intent = new Intent(this, fbLogInActivity.class);
        startActivity(intent);
    }
    private void goToSignOut() {
        FirebaseAuth.getInstance().signOut();
        PreferenceData.clearLoggedInInfo(this);
        goToMainAfterLogout();
    }
    private void goToMainAfterLogout() {
        Intent intent = new Intent(this, MainActivity.class);
        //EditText editText = (EditText) findViewById(R.id.editText);
        //String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
}
