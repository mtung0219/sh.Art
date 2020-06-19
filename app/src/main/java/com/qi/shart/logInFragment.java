package com.qi.shart;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class logInFragment extends Fragment implements View.OnClickListener{
    private View rootView;
    private final int RC_SIGN_IN=123;
    GoogleSignInClient mGoogleSignInClient;
    private logInHandler mLogInHandler;
    private Button createAccountButton;
    private Button signInButton;
    GoogleSignInOptions gso;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.loginfragment, container, false);
        createAccountButton = rootView.findViewById(R.id.createAccountButton);
        signInButton = rootView.findViewById(R.id.gotoSignIn);
        createAccountButton.setOnClickListener(this);
        signInButton.setOnClickListener(this);

        mLogInHandler = new logInHandler(rootView, getActivity());
        return rootView;
    }

    public void signIn() {
        Log.d("TAG", "starting sign in..");
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(rootView.getContext().getString(R.string.default_web_client_id))
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(rootView.getContext(), gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();

        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.createAccountButton:
                Log.w("TAG123", "starting create account");
                //mLogInHandler.signOutGoogle(mGoogleSignInClient);
                goToCreateAccount();
                break;
            case R.id.gotoSignIn:
                goToSignIn();
                break;

            default:
                break;
        }
    }

    private void goToCreateAccount() {
        Activity a = getActivity();
        Intent intent = new Intent(a, createProfileActivity.class);
        //EditText editText = (EditText) findViewById(R.id.editText);
        //String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        try {
            assert a != null;
            a.startActivity(intent);
        } catch(Exception e) {
            Log.d("TAG","activity was null for create account button..");
        }
    }
    private void goToSignIn() {
        Activity a = getActivity();
        Intent intent = new Intent(a, SignInActivity.class);
        //EditText editText = (EditText) findViewById(R.id.editText);
        //String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        try {
            assert a != null;
            a.startActivity(intent);
        } catch(Exception e) {
            Log.d("TAG","activity was null for create account button..");

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getIdToken());
                mLogInHandler.firebaseAuthWithGoogle(account.getIdToken(),gso, mGoogleSignInClient);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
                // ...
            }
        }
    }
}