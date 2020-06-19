package com.qi.shart;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SubmitChallengeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private DatabaseReference mDatabase;
    private FirebaseFirestore firestoreDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submitchallenge);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        firestoreDB = FirebaseFirestore.getInstance();
        DatabaseReference sizeRef = mDatabase.child("meta").child("size");

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        sizeRef.addValueEventListener(eventListener); //listener for the Size variable

        //setting up spinners------------------------------------------------------------
        Spinner platformspinner = findViewById(R.id.platform_spinner);
        Spinner freqspinner = findViewById(R.id.frequency_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.lengthoptions, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> platformAdapter = ArrayAdapter.createFromResource(this,
                R.array.platformOptions, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> freqAdapter = ArrayAdapter.createFromResource(this,
                R.array.freqoptions, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        platformAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        platformspinner.setAdapter(platformAdapter);
        platformspinner.setOnItemSelectedListener(this);
        freqspinner.setAdapter(freqAdapter);
        freqspinner.setOnItemSelectedListener(this);
        //spinner end----------------------------------------------------------------------
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        Toast.makeText(adapterView.getContext(),adapterView.getSelectedItem().toString(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void writeChallenge(View view) {
        Log.d("mainactivity", "STARTING DATA UPLOAD?");
        int numEntriesInt =0;
        int authorId=1; //to do later
        String title= ( (TextView) findViewById(R.id.challengename_text)).getText().toString();
        String platform=( (TextView) findViewById(R.id.platform_label)).getText().toString();
        String hashtag=( (TextView) findViewById(R.id.hashtag_text)).getText().toString();
        String numEntries=( (TextView) findViewById(R.id.numEntries_text)).getText().toString();
        if (!numEntries.equals("")) {
            numEntriesInt = Integer.parseInt(numEntries);
        }
        String postFreq=( (TextView) findViewById(R.id.frequency_label)).getText().toString();
        String startDate=( (TextView) findViewById(R.id.startdate_show)).getText().toString();
        String desc=( (TextView) findViewById(R.id.desc_text)).getText().toString();

        Date ts = new Date();
        Challenge challenge = new Challenge(authorId,title,platform,hashtag,numEntriesInt,postFreq,startDate,desc, ts);
        mDatabase.child("challenges").child("1").setValue(challenge);

        //mDatabase.child("size").setValue(1);
        incrementSize();

        //-------------------------------------------------firestore---------------------------------------------
        firestoreDB.collection("challenges").add(challenge);

        //update challenges size
        firestoreDB.collection("challenges").document("meta")
                .update("size",FieldValue.increment(1));
        //update next ID to assign
        firestoreDB.collection("challenges").document("meta")
                .update("nextID",FieldValue.increment(1));


    }

    public static void incrementSize() { //transaction to add size by 1
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference sizeRef = rootRef.child("meta").child("size");

        sizeRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Integer score = mutableData.getValue(Integer.class);
                if (score == null) {
                    return Transaction.success(mutableData);
                }
                mutableData.setValue(score + 1);
                return Transaction.success(mutableData);
            }
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

            }
        });
    }
}