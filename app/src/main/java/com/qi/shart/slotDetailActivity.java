package com.qi.shart;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

public class slotDetailActivity extends AppCompatActivity {
    private ArrayList<challengeSlotDetail> mCSD;
    private DatabaseReference mDatabase;
    private int numPos, numEntries;
    private String uuidValue;

    private RecyclerView mRecyclerView;
    private recyclerAdapter_viewAllDetail mAdapter;
    private FirebaseFirestore firestoreDB;
    private Context ctx;
    private String chName;
    private String username;

    private int likeNum;
    private String myID;
    private DocumentReference docpath, pDocpath, sDocpath;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewslot);
        mRecyclerView = findViewById(R.id.recyclerView_view_slots);

        this.numPos = getIntent().getIntExtra("numPos",0);
        this.uuidValue = getIntent().getStringExtra("docID");
        this.chName = getIntent().getStringExtra("chName");
        this.numEntries = getIntent().getIntExtra("numEntries",666);
        this.ctx = this;

        TextView viewTitle = findViewById(R.id.viewslot_title);
        username = PreferenceData.getLoggedInUsername(this);
        viewTitle.setText(chName + " day " + numPos + " submissions:");

        Log.d("TAGASDF", uuidValue);
        Log.d("TAGASDF", String.valueOf(numPos));

        //populateCards();

    }

    //SHOULD BE UNUSED NOW

    /*private void populateCards() {

        mDatabase = FirebaseDatabase.getInstance().getReference();
        firestoreDB = FirebaseFirestore.getInstance();
        mCSD = new ArrayList<>();

        CollectionReference sortRef = firestoreDB.collection("challengeSlotURIs").document(uuidValue)
                .collection(String.valueOf(numPos));
        Query query = sortRef.orderBy("timeStamp", Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        challengeSlotDetail CSD = document.toObject(challengeSlotDetail.class);
                        mCSD.add(CSD);
                    }
                    mAdapter = new recyclerAdapter_viewAllDetail(ctx, mCSD, uuidValue, numEntries, chName, numPos,
                            new slotDetailActivity.OnLikeClickListener() {
                                @Override
                                public void onLikeClick(int position, View likeView, View likeNum) {
                                    challengeSlotDetail item = mCSD.get(position);
                                    Log.d("CLICKED", "Clicking on item(" + position + ", " + likeView + ", " + likeNum + ")");
                                    ImageView IV = (ImageView) likeView;
                                    TextView TV = (TextView) likeNum;
                                    updateLikeHelper(item, IV, TV, position);

                                }
                            });
                    mRecyclerView.setAdapter(mAdapter);
                    mRecyclerView.setItemViewCacheSize(10);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(ctx));

                } else {
                    Log.d("TAG", "ERROR GETTING DOCUMENTS: ", task.getException());
                }
            }
        });
    }
    public interface OnLikeClickListener {
        void onLikeClick(int position, View view, View view2);
    }
*/


}