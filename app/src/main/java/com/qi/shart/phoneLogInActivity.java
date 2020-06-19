package com.qi.shart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class phoneLogInActivity extends AppCompatActivity implements View.OnClickListener{
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    private String mVerificationId;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private Context ctx;
    private EditText enterVCode;
    private Button submitVCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        this.ctx = this;
        enterVCode = findViewById(R.id.enterVCode);
        submitVCode = findViewById(R.id.submitVCode);
        submitVCode.setOnClickListener(this);
        phoneSendCode();
    }

    public void passInCredential(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void phoneSendCode() {
        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Log.d("TAG", "onVerificationCompleted:" + phoneAuthCredential);
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }
            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.w("TAG", "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }
            }
            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("TAG", "onCodeSent:" + verificationId);
                Toast.makeText(ctx, "Code sent via SMS.",Toast.LENGTH_LONG).show();
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                enterVCode.setVisibility(View.VISIBLE);
                submitVCode.setVisibility(View.VISIBLE);

                // ...
            }

        };
        String phoneNumber = "+15039263983";
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallBacks);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            Toast.makeText(ctx, "Phone Login Successful!",Toast.LENGTH_LONG).show();
                            PreferenceData.setUserLoggedInStatus(ctx, true);
                            String uuidValue = user.getUid();
                            routeAfterAuth(uuidValue);
                            goToMainAfterLogin();
                        } else {
                            Toast.makeText(ctx, "Phone Login Failed!",Toast.LENGTH_SHORT).show();
                            // Sign in failed, display a message and update the UI
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    public void routeAfterAuth(String uuidValue) {
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        DocumentReference docRef = firestoreDB.collection("profiles").document(uuidValue);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Profile pf = doc.toObject(Profile.class);
                        PreferenceData.setLoggedInUsername(getApplicationContext(),pf.getUsername());
                        //existing profile, go to main
                        goToMainAfterLogin();
                    } else {
                        //profile doesn't exist, send to finish making profile
                        goToCreateProfile();
                    }
                } else {
                    Log.d("TAG", "ERROR GETTING DOCUMENTS: ", task.getException());
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submitVCode:
                passInCredential(enterVCode.getText().toString());
                break;
            default:
                break;
        }
    }

    private void goToMainAfterLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        //EditText editText = (EditText) findViewById(R.id.editText);
        //String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
    public void goToCreateProfile() {
        Intent intent = new Intent(this, createProfileActivity.class);
        startActivity(intent);
    }
}