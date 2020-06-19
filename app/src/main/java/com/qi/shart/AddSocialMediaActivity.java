package com.qi.shart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddSocialMediaActivity extends AppCompatActivity {
    private String UUIDValue;
    private FirebaseFirestore firestoreDB;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private Profile pf;
    private Context ctx;
    private EditText name, bio, instagram, deviantart, twitter;
    private EditTextCustom username;
    private TextView usernameOK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_social_media);
        ctx = this;

        username = findViewById(R.id.addSM_username);
        usernameOK = findViewById(R.id.usernameAvailable);
        final EditTextCustom etc = new EditTextCustom(ctx);
        username.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) { //username check if focus change, in addition to keyboard exit
                    String usernameTyped = username.getText().toString();
                    if (!usernameTyped.matches("")) {
                        etc.checkUsernameAvailable(usernameTyped);
                    }
                    //Toast.makeText(ctx, "username typed!",Toast.LENGTH_SHORT).show();
                }
            }
        });
        pullExistingProfile();
    }

    private void populateFieldsExisting() {


        name = findViewById(R.id.addSM_name);
        bio = findViewById(R.id.addSM_bio);

        instagram = findViewById(R.id.addSM_instagram);
        deviantart = findViewById(R.id.addSM_deviantArt);
        twitter = findViewById(R.id.addSM_twitter);

        username.setText(pf.getUsername());
        //name = setText(pf.getName());
        bio.setText(pf.getDesc());
        instagram.setText(pf.getInstagram());
        deviantart.setText(pf.getDeviantArt());
        twitter.setText(pf.getTwitter());
    }

    private void pullExistingProfile() {
        try {
            UUIDValue = user.getUid();
        } catch (NullPointerException e) {
            Log.d("TAG","current user is null when it shouldn't be!");
            return;
        }
        firestoreDB = FirebaseFirestore.getInstance();
        DocumentReference docpath = firestoreDB.collection("profiles").document(UUIDValue);
        Log.d("TAG",UUIDValue);
        docpath.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d("TAG","profile get successful...");
                    if (task.getResult()==null) Log.d("TAG","no results??");
                    pf = task.getResult().toObject(Profile.class);
                    Log.d("TAG","profile toObject successful..");

                    if (pf!=null) {
                        populateFieldsExisting();
                    }
                } else {
                    Log.d("TAG","ERROR GETTING PROFILE: ", task.getException());
                }
            }
        });
    }

    public boolean isUsernameOK() {
        String mUsername = username.getText().toString();
        if (!usernameOK.getText().toString().equals("Username is available.")) return false;
        if (mUsername.length() < 4) return false;
        boolean isDigit, isLetter, isPeriod;
        for (int i = 0;i<mUsername.length();i++) {
            char c = mUsername.charAt(i);
            isDigit = c >= '1' && c <='9';
            isLetter = (c >= 'a' && c <='z') || (c >='A' && c <='Z');
            isPeriod = c == '.';
            if (!(isDigit || isLetter || isPeriod)) {return false;}
        }
        return true;
    }

    public void writeSMtoProfile(View view) {
        if (!isUsernameOK()) {
            Toast.makeText(ctx, "Please choose another username!",Toast.LENGTH_LONG).show();
            return;
        }
        String IGname = ( (TextView) findViewById(R.id.addSM_instagram)).getText().toString();
        String DAname = ( (TextView) findViewById(R.id.addSM_deviantArt)).getText().toString();
        String twitterName = ( (TextView) findViewById(R.id.addSM_twitter)).getText().toString();
        String realName = ( (TextView) findViewById(R.id.addSM_name)).getText().toString();
        String bio = ((TextView) findViewById(R.id.addSM_bio)).getText().toString();
        String username = ((TextView) findViewById(R.id.addSM_username)).getText().toString();
        Map<String,Object> toUpdate = new HashMap<>();
        toUpdate.put("instagram",IGname);
        toUpdate.put("deviantArt",DAname);
        toUpdate.put("twitter",twitterName);
        toUpdate.put("realName",realName);
        toUpdate.put("username",username);
        toUpdate.put("desc",bio);

        firestoreDB = FirebaseFirestore.getInstance();
        firestoreDB.collection("profiles").document(UUIDValue).update(toUpdate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) { Log.d("TAGASDF","LINK SHOULD BE SET");
                        goToMainAfterLogIn();
                        }
                });
    }

    public void goToMainAfterLogIn() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}