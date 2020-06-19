package com.qi.shart;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.LinkedList;


public class deleteHandler {
    private FirebaseFirestore firestoreDB;
    private int mAdapterPosition;
    private ArrayList<challengeSlotDetail> mCSD;
    private Context ctx;

    public deleteHandler(Context ctx, ArrayList<challengeSlotDetail> mCSD, int mAdapterPosition) {
        this.mCSD = mCSD;
        this.ctx = ctx;
        this.mAdapterPosition = mAdapterPosition;
    }


    public deleteHandler() {}

    public void createAlertDialogForDelete() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case DialogInterface.BUTTON_POSITIVE:
                        deleteSubmission();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:

                        break;
                }
            }
        };
        AlertDialog.Builder areYouSure = new AlertDialog.Builder(ctx);
        areYouSure.setMessage("Are you sure you want to delete your submission?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }


    public void deleteSubmission() {
        int num = mAdapterPosition;
        firestoreDB = FirebaseFirestore.getInstance();
        WriteBatch batch = firestoreDB.batch();
        String challengeID = mCSD.get(num).getimageURI();
        String challengeNum = String.valueOf(mCSD.get(num).getNumPos());
        String posterID = mCSD.get(num).getposterID();

        firestoreDB.collection("challenges").document(challengeID).update("numSubmissions", FieldValue.increment(-1));


        Log.d("delete","num position is " + String.valueOf(num));
        Log.d("delete",challengeID + "_" + challengeNum + "_" + posterID);
        Log.d("delete",challengeID + "_" + challengeNum);

        DocumentReference inChallengeSlotURIs = firestoreDB.collection("challengeSlotURIs")
                .document(challengeID).collection(challengeNum)
                .document(challengeID + "_" + challengeNum + "_" + posterID);

        DocumentReference inProfileCollection = firestoreDB.collection(posterID)
                .document(challengeID + "_" + challengeNum);

        //DocumentReference inAllSubmissions = firestoreDB.collection("allSubmissions")
        //        .document(challengeID + "_" + challengeNum + "_" + posterID);

        batch.delete(inChallengeSlotURIs);
        batch.delete(inProfileCollection);
        //batch.delete(inAllSubmissions);

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(ctx, "Delete submission successful!",Toast.LENGTH_SHORT).show();
                goToMainAfterLogIn();

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
}
