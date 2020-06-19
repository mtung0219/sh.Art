package com.qi.shart;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class slotDetailFragment extends Fragment {
    private ArrayList<challengeSlotDetail> mCSD;
    private DatabaseReference mDatabase;
    private String uuidValue;

    private RecyclerView mRecyclerView;
    private recyclerAdapter_viewAllDetail mAdapter;
    private FirebaseFirestore firestoreDB;
    private Context ctx;
    private String chName;
    private String username;
    private int numPos, numEntries;
    private View rootView;

    private int likeNum;
    private String myID;
    private DocumentReference docpath, pDocpath, sDocpath;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    public slotDetailFragment() {
        // Required empty public constructor
    }

    public static slotDetailFragment newInstance(int numPos, int numEntries, String docID, String chName) {
        slotDetailFragment fragment = new slotDetailFragment();
        Bundle args = new Bundle();
        args.putInt("numPos", numPos);
        args.putInt("numEntries", numEntries);
        args.putString("docID", docID);
        args.putString("chName", chName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uuidValue = getArguments().getString("docID");
            numPos = getArguments().getInt("numPos");
            chName = getArguments().getString("chName");
            numEntries = getArguments().getInt("numEntries");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ctx = getContext();
        rootView = inflater.inflate(R.layout.viewslot, container, false);
        mRecyclerView = rootView.findViewById(R.id.recyclerView_view_slots);
        TextView viewTitle = rootView.findViewById(R.id.viewslot_title);
        username = PreferenceData.getLoggedInUsername(ctx);
        viewTitle.setText(chName + " day " + numPos + " submissions:");

        populateCards();
        return rootView;
    }

    private void populateCards() {

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
                            new slotDetailFragment.OnLikeClickListener() {
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

    public void updateLikeHelper(final challengeSlotDetail CSD, final ImageView mLikeButton , final TextView mLikeView, final int position) {
        myID = user.getUid();
        //re-pulling like statistics after a change
        String challengeID = CSD.getimageURI();
        String challengeNum = String.valueOf(CSD.getNumPos());
        String posterID = CSD.getposterID();
        docpath = firestoreDB.collection("challengeSlotURIs")
                .document(challengeID).collection(challengeNum)
                .document(challengeID + "_" + challengeNum + "_" + posterID);
        pDocpath = firestoreDB.collection(posterID)
                .document(challengeID + "_" + challengeNum);

        sDocpath = firestoreDB.collection("allSubmissions")
                .document(challengeID + "_" + challengeNum + "_" + posterID);

        //get the UI updater first when you press button
        mLikeButton.setClickable(false);
        boolean likedAlready = CSD.getLikes().contains(myID);
        likeNum = CSD.getLikeNum();
        if (likedAlready) {
            likeNum-=1;
            mLikeButton.setImageResource(R.drawable.liketemp);
        } else {
            likeNum+=1;
            mLikeButton.setImageResource(R.drawable.likedtemp);
        }
        mLikeView.setText(String.valueOf(likeNum));

        firestoreDB.runTransaction(new Transaction.Function<Boolean>() {
            @Nullable
            @Override
            public Boolean apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                DocumentSnapshot ds = transaction.get(docpath);
                //DocumentSnapshot profileDS = transaction.get(pDocpath);
                //DocumentSnapshot submissionDS = transaction.get(sDocpath);
                challengeSlotDetail CSD = ds.toObject(challengeSlotDetail.class);
                if (CSD.getLikes().contains(myID)) { //if likedalready
                    CSD.removeLikes(myID);
                    CSD.decrementLikeCounter();
                } else {
                    CSD.addLikes(myID);
                    CSD.incrementLikeCounter();
                }

                transaction.set(docpath, CSD);
                transaction.set(pDocpath, CSD);
                transaction.set(sDocpath, CSD);
                return true;
            }
        }).addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean itWorked) {
                /*if (itWorked) {
                    Log.d("TAG", "updating Likes a success!" + likedAlready);

                    likedAlready = !likedAlready;
                    //likeRefresh();
                    Log.d("TAG", "LikedAlready is now " + likedAlready);
                } else {
                    Log.d("TAG","failed to add a like");
                }*/

                likeRefresh(position,mLikeView,mLikeButton);

            }
        }) .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("TAG", "Couldn't add like!", e);
                if (CSD.getLikes().contains(myID)) { //reverse things on failure
                    likeNum+=1;
                    mLikeButton.setImageResource(R.drawable.likedtemp);
                } else {
                    likeNum-=1;
                    mLikeButton.setImageResource(R.drawable.liketemp);
                }
                mLikeView.setText(String.valueOf(likeNum));

                Toast.makeText(ctx, "Couldn't change Like at this time. Please try again later.",Toast.LENGTH_LONG).show();
                mLikeButton.setClickable(true);
            }
        });
    }

    public void likeRefresh(final int pos, final TextView mLikeView, final ImageView mLikeButton) {
        //re-pulling like statistics
        docpath.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Log.d("TAG","CSD UPDATED");

                challengeSlotDetail CSDtemp = task.getResult().toObject(challengeSlotDetail.class);
                mCSD.set(pos, CSDtemp);

                boolean likedAlready = CSDtemp.getLikes().contains(myID);
                int likeNumber = CSDtemp.getLikeNum();
                mLikeView.setText(String.valueOf(likeNumber));
                Log.d("LIKE","likedalready value on refresh is now " + likedAlready + " " + CSDtemp.getdesc() + " " + myID);
                if (likedAlready) { mLikeButton.setImageResource(R.drawable.likedtemp); }
                else { mLikeButton.setImageResource(R.drawable.liketemp); }

                mLikeButton.setClickable(true);
            }
        });
    }

}