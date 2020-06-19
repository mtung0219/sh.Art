package com.qi.shart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {
    private final LinkedList<String> mWordList = new LinkedList<>();
    private final LinkedList<String> mArtistList = new LinkedList<>();
    private final LinkedList<String> mCategoryList = new LinkedList<>();
    //private final LinkedList<String> mDateList = new LinkedList<>();
    private DatabaseReference mDatabase;
    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private RecyclerView mRecyclerView;
    private recyclerAdapter mAdapter;
    private FirebaseFirestore firestoreDB;
    public void goToSubmitChallengePage(View view) {
        Intent intent = new Intent(this, SubmitChallengeActivity.class);
        //EditText editText = (EditText) findViewById(R.id.editText);
        //String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        firestoreDB = FirebaseFirestore.getInstance();


        // Put initial data into the word list.
        for (int i = 0; i < 20; i++) {
            mWordList.addLast("Art Challenge " + i);
            mArtistList.addLast("Artist #" + i);
            mCategoryList.addLast("Category #" + i);
            //mDateList.addLast("Date " + i);
        }

        // Get a handle to the RecyclerView.
        mRecyclerView = findViewById(R.id.recyclerview);
        // Create an adapter and supply the data to be displayed.
        mAdapter = new recyclerAdapter(this, mWordList, mArtistList, mCategoryList);

        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);

        // Give the RecyclerView a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    public void writeNewChallenge(View view) {
        //Log.d("mainactivity", "STARTING DATA UPLOAD?");
        //Challenge challenge = new Challenge("dreamweek","1","test desc","weekly","07/01/2020","7");
        //mDatabase.child("challenges").child("1").setValue(challenge);
    }

}