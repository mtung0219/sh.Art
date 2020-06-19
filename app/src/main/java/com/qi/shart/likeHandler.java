package com.qi.shart;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.Date;
import java.util.LinkedList;

public class likeHandler {
    private Context ctx;
    private FirebaseFirestore firestoreDB;
    private challengeSlotDetail CSD;
    private int numPos, likeNum;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private ImageView likeButton;
    private TextView likeView;
    private Boolean likedAlready;
    private DocumentReference docpath,pDocpath;
    private String challengeID, challengeNum, posterID, myID;

    public likeHandler(){}






    /*public likeHandler(Context ctx, challengeSlotDetail CSD,
                       ImageView likeButton, TextView likeView){
        this.CSD = CSD;
        this.likeButton = likeButton;
        this.likeView = likeView;
        firestoreDB = FirebaseFirestore.getInstance();

        challengeID = CSD.getimageURI();
        challengeNum = String.valueOf(CSD.getNumPos());
        posterID = CSD.getposterID();
        myID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //challenge Slot URI path - need to update the likes in three locations
        docpath = firestoreDB.collection("challengeSlotURIs")
                .document(challengeID).collection(challengeNum)
                .document(challengeID + "_" + challengeNum + "_" + posterID);


        //poster docpath
        pDocpath = firestoreDB.collection(posterID)
                .document(challengeID + "_" + challengeNum);

        //likeRefresh();
    }

    public void updateLikeHelper() {
        //re-pulling like statistics after a change

        //get the UI updater first when you press button
        likedAlready = CSD.getLikes().contains(myID);
        likeNum = CSD.getLikeNum();
        if (likedAlready) {
            likeNum-=1;
            likeButton.setImageResource(R.drawable.liketemp);
        } else {
            likeNum+=1;
            likeButton.setImageResource(R.drawable.likedtemp);
        }
        likeView.setText(String.valueOf(likeNum));

        docpath.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Log.d("TAG","CSD UPDATED");
                CSD = task.getResult().toObject(challengeSlotDetail.class);
                Log.d("TAG","likedalready after CSD updated is " + CSD.getLikes().contains(user.getUid()));

                likedAlready = CSD.getLikes().contains(myID);
                likeNum = CSD.getLikeNum();

                updateLikes();
            }
        });
    }


    public void updateLikes() {
        //allSubmissions docpath (For discover page)
        //final DocumentReference allSubDocPath = firestoreDB.collection("allSubmissions").document(
        // challengeID + "_" + challengeNum + "_" + posterID);

        firestoreDB.runTransaction(new Transaction.Function<Boolean>() {
            @Nullable
            @Override
            public Boolean apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                DocumentSnapshot ds = transaction.get(docpath);
                DocumentSnapshot profileDS = transaction.get(pDocpath);
                challengeSlotDetail CSD = ds.toObject(challengeSlotDetail.class);
                if (likedAlready) {
                    CSD.removeLikes(myID);
                    CSD.decrementLikeCounter();
                } else {
                    CSD.addLikes(myID);
                    CSD.incrementLikeCounter();
                }

                transaction.set(docpath, CSD);
                transaction.set(pDocpath, CSD);
                return true;
            }
        }).addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean itWorked) {
                if (itWorked) {
                    Log.d("TAG", "updating Likes a success!" + likedAlready);

                    likedAlready = !likedAlready;
                    //likeRefresh();
                    Log.d("TAG", "LikedAlready is now " + likedAlready);
                } else {
                    Log.d("TAG","failed to add a like");
                }

            }
        }) .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("TAG", "Couldn't add like!", e);
            }
        });
    }

    public void likeRefresh() {
        //re-pulling like statistics after a change
        docpath.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Log.d("TAG","CSD UPDATED");
                CSD = task.getResult().toObject(challengeSlotDetail.class);
                Log.d("TAG","likedalready after CSD updated is " + CSD.getLikes().contains(user.getUid()));

                likedAlready = CSD.getLikes().contains(myID);
                likeNum = CSD.getLikeNum();

                likeView.setText(String.valueOf(likeNum));
                if (likedAlready) { likeButton.setImageResource(R.drawable.likedtemp); }
                else { likeButton.setImageResource(R.drawable.liketemp); }
            }
        });
    }*/


}
