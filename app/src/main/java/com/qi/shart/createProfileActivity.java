package com.qi.shart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class createProfileActivity extends AppCompatActivity {
    private String UUIDValue;
    private DatabaseReference mDatabase;
    private FirebaseFirestore firestoreDB;
    private EditText usernameText;
    private Context ctx;
    private TextView usernameOK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);
        ctx = this;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        firestoreDB = FirebaseFirestore.getInstance();

        usernameOK = findViewById(R.id.usernameAvailable);
        usernameText = (EditTextCustom) findViewById(R.id.createprofile_usernametext);
        final EditTextCustom etc = new EditTextCustom(ctx);

        usernameText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) { //username check if focus change, as opposed to keyboard exit
                    String usernameTyped = usernameText.getText().toString();
                    if (!usernameTyped.matches("")) {
                        etc.checkUsernameAvailable(usernameTyped);
                    }
                }
            }
        });

    }
    public boolean isUsernameOK() {
        String mUsername = usernameText.getText().toString();
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
    public void writeProfile(View view) {
        Log.d("mainactivity", "STARTING DATA UPLOAD?");
        int numEntriesInt =0;
        int authorId=1; //to do later

        if (!isUsernameOK()) {
            Toast.makeText(ctx, "Please choose another username!",Toast.LENGTH_LONG).show();
            return;
        }

        String username = ( (TextView) findViewById(R.id.createprofile_usernametext)).getText().toString();
        String desc=( (TextView) findViewById(R.id.createprofile_desctext)).getText().toString();

        Date ts = new Date();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String provID = user.getProviderId();
        try {
            UUIDValue = user.getUid();
        } catch (NullPointerException e) {
            Log.d("TAG","current user is null when it shouldn't be!");
            UUIDValue = "1";
        }

        Profile profile = new Profile(username, desc, ts, provID,"","","",0L,"",username.toLowerCase());
        mDatabase.child("profiles").child("1").setValue(profile);

        //mDatabase.child("size").setValue(1);
        //incrementSize();
        ArrayList<String> emptyList = new ArrayList<>();
        emptyList.add("placeholder");
        ArrayList<Boolean> emptyListBool = new ArrayList<>();
        emptyListBool.add(false);
        profileParticipation PP = new profileParticipation(emptyList,emptyListBool,emptyListBool,emptyList, emptyListBool,emptyListBool);
        //UUIDValue = UUID.randomUUID().toString();
        //-------------------------------------------------firestore---------------------------------------------
        firestoreDB.collection("profiles").document(UUIDValue).set(profile);
        firestoreDB.collection("profileParticipation").document(UUIDValue).set(PP);
        goToMainAfterLogIn();
        //uploadImage(view);

        //update challenges size
        //firestoreDB.collection("challenges").document("meta")
        //.update("size",FieldValue.increment(1));
        //update next ID to assign
        //firestoreDB.collection("challenges").document("meta")
        //.update("nextID",FieldValue.increment(1));
    }

    public void goToMainAfterLogIn() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}