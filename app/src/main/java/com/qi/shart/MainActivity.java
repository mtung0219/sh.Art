package com.qi.shart;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends BaseActivity {
    private DatabaseReference mDatabase;
    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseStorage firestoreStorage = FirebaseStorage.getInstance();
    private FirebaseFirestore firestoreDB;
    private SharedPreferences sharedPrefs;
    private boolean isUserLoggedIn;
    private int currentlyLoggedInUser;
    private NavigationView nv;
    private DrawerLayout d1;
    private String docID;
    private Profile pf = null;
    private int tabPosition = 0;

    public void goToSubmitChallengePage(View view) {
        Intent intent = new Intent(this, SubmitChallengeActivity.class);
        //EditText editText = (EditText) findViewById(R.id.editText);
        //String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void goToLoginPage() {
        Intent signinintent = new Intent(this, SignInActivity.class);
        signinintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(signinintent);
        finish();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.topbar, menu);
        return true;
    }*/

    public void switchContent(int id, Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
        .replace(id, fragment)
        .addToBackStack(null)
        .commit();
    }

    /*private void pullProfile() {
        firestoreDB = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        docID = user.getUid();
        DocumentReference docpath = firestoreDB.collection("profiles").document(docID);
        Log.d("TAG",docID);
        docpath.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d("TAG","profile get successful...");
                    if (task.getResult()==null) Log.d("TAG","no results??");
                    pf = task.getResult().toObject(Profile.class);
                    Log.d("TAG","profile toObject successful..");

                    TextView nvUsername = nv.findViewById(R.id.navDrawer_username);
                    nvUsername.setText(pf.getUsername());
                    retrieveFromFirebase();
                    tabSetup();
                    //retrieveFromFirebase();
                } else {
                    Log.d("TAG","ERROR GETTING PROFILE: ", task.getException());
                }
            }
        });
    }*/

    //pulling profile picture
    private void retrieveFromFirebase() {
        long cacheKey = pf.getProfPicCache();
        CircleImageView profpic = nv.findViewById(R.id.navDrawer_profPic);
        String imagePath = "profilePictures/" + docID;
        StorageReference storageRef = firestoreStorage.getReference();
        StorageReference imageRef = storageRef.child(imagePath);
        try {
            Glide.with(this)
                    .load(imageRef)
                    .signature(new ObjectKey(String.valueOf(cacheKey)))
                    .into(profpic);
        } catch (Exception e) {
            profpic.setImageResource(R.mipmap.ic_launcher_round);
            Log.e("TAG", "IMG PULL FAILED", e);
        }
    }

    private void signInAnonymously() {
        mFirebaseAuth.signInAnonymously().addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Log.d("TAG","logged in anoymously successfully");

            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("TAG", "signFailed****** ", exception);
            }
        });
    }
    @Override
    public void onRestart() {
        super.onRestart();
    }

}