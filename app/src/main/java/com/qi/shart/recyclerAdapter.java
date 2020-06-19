package com.qi.shart;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public class recyclerAdapter extends RecyclerView.Adapter<recyclerAdapter.ViewHolder> {
    private final LinkedList<Challenge> mChallenges;
    private FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
    private FirebaseStorage firestoreStorage = FirebaseStorage.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private LayoutInflater mInflater;
    private ImageView imageView;
    private TextView catItemView, titleItemView, usernameItemView;
    private Context cxt;
    private int widthUse;
    private Boolean isUserLoggedIn;
    private String myUserId, challengeId;
    private profileParticipation PP;
    private ArrayList<String> ppChallenges;
    private ArrayList<Boolean> ppChallengesActive;
    private ArrayList<Boolean> ppIsDone;

    public recyclerAdapter(Context context, LinkedList<Challenge> mChallenges, int widthUse, profileParticipation PP) {
        mInflater = LayoutInflater.from(context);
        this.cxt = context;
        this.mChallenges = mChallenges;
        this.widthUse = widthUse;
        this.PP = PP;

        if (PP!=null) {
            ppChallenges = PP.getChallengesParticipating();
            ppChallengesActive = PP.getIsActive();
            ppIsDone = PP.getIsDone();
        }
        isUserLoggedIn = PreferenceData.getUserLoggedInStatus(cxt);
        if (isUserLoggedIn) myUserId = user.getUid();

    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imgView;
        private ImageView addToChallengeButton;
        final recyclerAdapter mAdapter;
        private final Context context;
        private ViewHolder(@NonNull View itemView, recyclerAdapter adapter) {
            super(itemView);
            catItemView = itemView.findViewById(R.id.seriesButton);
            titleItemView = itemView.findViewById(R.id.textView);
            usernameItemView = itemView.findViewById(R.id.artistUsername);
            addToChallengeButton = itemView.findViewById(R.id.addMeToChallenge);

            imgView = itemView.findViewById(R.id.testingPic);
            this.mAdapter = adapter;
            context = itemView.getContext();
            itemView.setOnClickListener(this);
            addToChallengeButton.setOnClickListener(this);
            imgView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            // Get the position of the item that was clicked.
            int mPosition = getLayoutPosition();
            recyclerAdapter.ViewHolder thisViewHolder = this;
            switch (view.getId()) {

                case R.id.testingPic:
                    // Use that to access the affected item in mWordList.
                    String element = mChallenges.get(mPosition).getChID();
                    goToChProfilePage(view, element);
                    // Change the word in the mWordList.
                    //mChIDs.set(mPosition, "Clicked " + element);
                    // Notify the adapter, that the data has changed so it can update the RecyclerView to display the data.
                    //mAdapter.notifyDataSetChanged();
                    break;
                case R.id.addMeToChallenge:
                    addMeToChallenge(thisViewHolder, mPosition);
                    break;
            }

        }

        public void goToChProfilePage(View v, String docID) {
            Log.d("TAG","TRYING TO GO TO CHALLENGE PAGE..");
            Fragment nextFrag = new challengeprofilefragment();
            Bundle mBundle = new Bundle();
            mBundle.putString("docID", docID);
            mBundle.putString("comingFrom","mainFragment");
            nextFrag.setArguments(mBundle);
            MainActivity mainActivity = (MainActivity) cxt;

            CustomViewPager mpager = mainActivity.findViewById(R.id.mainpager);
            mpager.setPagingEnabled(false);

            mainActivity.switchContent(R.id.mainFragment_layout, nextFrag);

        }
    }

    @NonNull
    @Override
    public recyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.recyler_item, parent, false);
        imageView = mItemView.findViewById(R.id.testingPic);
        if (widthUse!=0) {
            ImageView iV = mItemView.findViewById(R.id.testingPic);
            CardView cV = mItemView.findViewById(R.id.recyclerView_cardview);
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams)
                    iV.getLayoutParams();
            layoutParams.height = widthUse;
            layoutParams.width = widthUse;
        }
        return new ViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull final recyclerAdapter.ViewHolder holder, final int position) {
        displayTextInfo(holder, position);
        retrieveFromFirebase(holder, position);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void cacheImages(Bitmap bmp, ViewHolder holder) {
        Glide.with(cxt)
                .load(bmp)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imgView);
    }

    private void dlFromFirebase(StorageReference imageRef, Bitmap bmp, final ViewHolder holder) {
        final long ONE_MEGABYTE = 3072 * 3072;
        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                cacheImages(bmp, holder);
                //holder.imgView.setImageBitmap(bmp);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //Log.e("TAG", "IMG PULL FAILED", exception);
                //holder.imgView.setImageResource(R.drawable.testpic);
                Log.d("TAG", "IMG PULL FAILED");
            }
        });
    }

    private void retrieveFromFirebase(final recyclerAdapter.ViewHolder holder, int position) {
        final String mCurrent = mChallenges.get(position).getChID();
        final boolean mHasPicPosition = mChallenges.get(position).getHasPic();
        String imagePath = "images/" + mCurrent;
        Log.d("TAG",imagePath);
        StorageReference storageRef = firestoreStorage.getReference();
        StorageReference imageRef = storageRef.child(imagePath);
        Log.d("TAGIMAGEGET", mCurrent);
        if (!mHasPicPosition) {
            holder.imgView.setBackgroundResource(0);
            holder.imgView.getLayoutParams().height=ViewGroup.LayoutParams.WRAP_CONTENT;
            //holder.imgView.setImageResource(R.drawable.testpic);
        } else {
            try {
                //holder.imgView.setBackgroundResource(R.drawable.imageinputborder);
                Glide.with(cxt).load(imageRef).into(holder.imgView);
            } catch (Exception e) {
                //holder.imgView.setImageResource(R.drawable.testpic);
                Log.e("TAG", "IMG PULL FAILED", e);
            }
        }
    }

    private void displayTextInfo(recyclerAdapter.ViewHolder holder, int position) {
        Challenge thisCh = mChallenges.get(position);
        final String mCurrent = thisCh.getChType();
        String chID = thisCh.getChID();
        String titleString = thisCh.getTitle();
        catItemView.setText(mCurrent);
        titleItemView.setText(titleString);
        usernameItemView.setText(mChallenges.get(position).getAuthorId());

        if (isUserLoggedIn && PP!=null) {
            int indexPos = ppChallenges.indexOf(chID);
            if (indexPos >= 0 && ppChallengesActive.get(indexPos)) {
                holder.addToChallengeButton.setImageResource(R.drawable.addedpersontemp);
            }
        }
    }

    public void addMeToChallenge(final recyclerAdapter.ViewHolder holder, int pos) {
        challengeId = mChallenges.get(pos).getChID();
        //profile list from challenge point of view
        Log.d("TAG","challenge id is " + challengeId + " and my user ID is " + myUserId);
        final DocumentReference docpath = firestoreDB.collection("challengeParticipation").document(challengeId);
        //challenge list from profile point of view
        final DocumentReference pDocpath = firestoreDB.collection("profileParticipation").document(myUserId);
        //challenge docPath
        final DocumentReference challengeDocPath = firestoreDB.collection("challenges").document(challengeId);

        firestoreDB.runTransaction(new Transaction.Function<Boolean>() {

            @Nullable
            @Override
            public Boolean apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                DocumentSnapshot ds = transaction.get(docpath);
                DocumentSnapshot profileDS = transaction.get(pDocpath);
                DocumentSnapshot challengeDS = transaction.get(challengeDocPath);

                double currParticipants = ds.getDouble("currParticipants") + 1;
                boolean hasPic = challengeDS.getBoolean("hasPic");
                String title = challengeDS.getString("title");

                ArrayList<String> newActiveParticipants = (ArrayList<String>) ds.get("activeParticipants");
                ArrayList<Boolean> newParticipantsActive = (ArrayList<Boolean>) ds.get("participantActive");
                ArrayList<String> challengesImDoing = (ArrayList<String>) profileDS.get("challengesParticipating");
                ArrayList<Boolean> challengesHasPic = (ArrayList<Boolean>) profileDS.get("hasPic");
                ArrayList<String> challengeTitle = (ArrayList<String>) profileDS.get("title");
                ArrayList<Boolean> isActive = (ArrayList<Boolean>) profileDS.get("isActive");
                ArrayList<Boolean> challengeActive = (ArrayList<Boolean>) profileDS.get("challengeActive");
                ArrayList<Boolean> isDone = (ArrayList<Boolean>) profileDS.get("isDone");

                transaction.update(docpath,"currParticipants",currParticipants);
                transaction.update(challengeDocPath, "activeParticipants", currParticipants);
                //get the current user ID
                if (!newActiveParticipants.contains(myUserId)) {
                    newActiveParticipants.add(myUserId);
                    newParticipantsActive.add(true);
                } else {
                    int properIndex = newActiveParticipants.indexOf(myUserId);
                    newParticipantsActive.set(properIndex, true);
                }


                transaction.update(docpath,"activeParticipants",newActiveParticipants);
                transaction.update(docpath,"participantActive",newParticipantsActive);
                if (!challengesImDoing.contains(challengeId)) {
                    challengesImDoing.add(challengeId);
                    challengesHasPic.add(hasPic);
                    challengeTitle.add(title);
                    isActive.add(true);
                    challengeActive.add(true);
                    isDone.add(false);
                } else {
                    int properIndex = challengesImDoing.indexOf(challengeId);
                    isActive.set(properIndex, true);
                }
                transaction.update(pDocpath, "challengesParticipating",challengesImDoing);
                transaction.update(pDocpath, "hasPic",challengesHasPic);
                transaction.update(pDocpath, "title",challengeTitle);
                transaction.update(pDocpath, "isActive", isActive);
                transaction.update(pDocpath, "isDone", isDone);
                transaction.update(pDocpath, "challengeActive", challengeActive);
                return true;
            }
        }).addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean itWorked) {
                if (itWorked) {
                    Toast.makeText(cxt, "Added me to challenge successfully",Toast.LENGTH_LONG).show();
                    holder.addToChallengeButton.setImageResource(R.drawable.addedpersontemp);
                    //pullParticipants();
                }
            }
        }) .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(cxt, "Error adding me to challenge. Please try again later.",Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mChallenges.size();
    }
}
