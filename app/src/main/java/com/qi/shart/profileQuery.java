package com.qi.shart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;

public class profileQuery {
    private Context ctx;
    private String uuidValue;

    public profileQuery(Context ctx, String uuidValue) {
        this.ctx = ctx;
        this.uuidValue = uuidValue;
    }


    public void routeAfterAuth() {
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        CollectionReference sortRef = firestoreDB.collection("profiles");
        DocumentReference docRef = firestoreDB.collection("profiles").document(uuidValue);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        //existing profile, go to main
                        Profile pf = task.getResult().toObject(Profile.class);
                        goToMainAfterLogIn();
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

    public void goToMainAfterLogIn() {
        Intent intent = new Intent(ctx, MainActivity.class);
        //EditText editText = (EditText) findViewById(R.id.editText);
        //String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        ctx.startActivity(intent);
    }
    public void goToCreateProfile() {
        Intent intent = new Intent(ctx, createProfileActivity.class);
        //EditText editText = (EditText) findViewById(R.id.editText);
        //String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        ctx.startActivity(intent);
    }
}
