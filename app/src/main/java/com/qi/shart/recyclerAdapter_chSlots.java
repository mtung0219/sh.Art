package com.qi.shart;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class recyclerAdapter_chSlots extends RecyclerView.Adapter<recyclerAdapter_chSlots.ViewHolder> {
    private FirebaseStorage firestoreStorage = FirebaseStorage.getInstance();
    private LayoutInflater mInflater;
    private ImageView imageView;
    private Context cxt;
    private int picCount, numEntries;
    private String chName, comingFrom, challengeID;
    private Challenge ch;
    private boolean isLoggedIn;
    private int numEntriesIndicator;

    //private RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();

    public recyclerAdapter_chSlots(Context context, Challenge ch, String docID, int numEntriesIndicator,
                                   String comingFrom) {
        mInflater = LayoutInflater.from(context);
        this.cxt = context;
        this.ch = ch;
        this.picCount = ch.getnumEntries();
        this.challengeID = docID;
        this.chName = ch.getTitle();
        this.numEntries = ch.getnumEntries();
        this.comingFrom = comingFrom;
        this.numEntriesIndicator = numEntriesIndicator;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imgView;
        private TextView viewSubmissions;
        private Button submit;
        final TextView titleItemView;
        final recyclerAdapter_chSlots mAdapter;
        final RecyclerView mRecyclerViewIndividual;
        recyclerAdapter_slotDetail mAdapterIndividual;
        private final Context context;
        private ViewHolder(@NonNull View itemView, recyclerAdapter_chSlots adapter) {
            super(itemView);
            titleItemView = itemView.findViewById(R.id.title_ch_slots);
            viewSubmissions = itemView.findViewById(R.id.viewAll_ch_slots);
            if (numEntriesIndicator == 1) {

                viewSubmissions.setVisibility(View.INVISIBLE);
                viewSubmissions.setClickable(false);
            } else {
                viewSubmissions.setOnClickListener(this);
            }
            //submit = itemView.findViewById(R.id.submit_ch_slots);
            //imgView = itemView.findViewById(R.id.testingPic_ch_slots);
            this.mAdapter = adapter;
            context = itemView.getContext();
            mRecyclerViewIndividual = itemView.findViewById(R.id.recyclerView_view_slots);
            //imgView.setOnClickListener(this);
            //submit.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.viewAll_ch_slots:
                    goToViewSlotPage();
                    break;
                /*case R.id.submit_ch_slots:
                    if (isLoggedIn) {
                        goToChSubmitPage();
                    } else {
                        goToSignInPage();
                    }
                    break;*/
                default:
                    break;
            }
        }

        public void goToChSubmitPage() {
            final Intent intent;
            intent = new Intent(context, SubmitSlotActivity.class);
            intent.putExtra("numPos",getLayoutPosition());
            intent.putExtra("docID",challengeID);
            intent.putExtra("chName",chName);
            intent.putExtra("numEntries",numEntries);
            context.startActivity(intent);
        }

        public void goToViewSlotPage() { //VIEW ALL
            /*final Intent intentview;
            intentview = new Intent(context, slotDetailActivity.class);
            intentview.putExtra("numPos",getLayoutPosition());
            intentview.putExtra("docID",challengeID);
            intentview.putExtra("chName",chName);
            intentview.putExtra("numEntries",numEntries);
            context.startActivity(intentview);*/

            Log.d("TAG","TRYING TO GO TO CHALLENGE PAGE..");
            Fragment nextFrag = new slotDetailFragment();
            Bundle mBundle = new Bundle();
            mBundle.putInt("numPos", getLayoutPosition());
            mBundle.putString("docID", challengeID);
            mBundle.putString("chName", chName);
            mBundle.putInt("numEntries", numEntries);
            nextFrag.setArguments(mBundle);
            MainActivity mainActivity = (MainActivity) cxt;

            CustomViewPager mpager = mainActivity.findViewById(R.id.mainpager);
            mpager.setPagingEnabled(false);

            if (comingFrom.equals("mainFragment")) {
                mainActivity.switchContent(R.id.mainFragment_layout, nextFrag);
            } else {
                mainActivity.switchContent(R.id.profileFragment_layout, nextFrag);
            }
        }

        public void goToSignInPage() {
            final Intent intentSignIn;
            intentSignIn = new Intent(context, SignInActivity.class);
            context.startActivity(intentSignIn);
        }
    }

    @NonNull
    @Override
    public recyclerAdapter_chSlots.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.recycler_item_ch_slots, parent, false);
        //this.imageView = mItemView.findViewById(R.id.testingPic_ch_slots);
        return new recyclerAdapter_chSlots.ViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull final recyclerAdapter_chSlots.ViewHolder holder, final int position) {
        displayTextInfo(holder, position);
        populateCards(holder, position);

    }

    private void cacheImages(Bitmap bmp, recyclerAdapter_chSlots.ViewHolder holder) {
        Glide.with(cxt)
                .load(bmp)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imgView);
    }

    private void dlFromFirebase(StorageReference imageRef, Bitmap bmp, final recyclerAdapter_chSlots.ViewHolder holder) {
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
                holder.imgView.setImageResource(R.drawable.testpic);
                Log.d("TAG", "IMG PULL FAILED");
            }
        });
    }

    /*private void retrieveFromFirebase(final recyclerAdapter_chSlots.ViewHolder holder, int position) {
        final String mCurrent = mChIDs.get(position);
        final boolean mHasPicPosition = mHasPic.get(position);
        String imagePath = "images/" + mChIDs.get(position);
        Log.d("TAG",imagePath);
        StorageReference storageRef = firestoreStorage.getReference();
        StorageReference imageRef = storageRef.child(imagePath);
        //StorageReference imageRefTest = firestoreStorage.getReferenceFromUrl("gs://shart-74bb2.appspot.com/images/ef21a7b4-5c9c-4072-8589-27692eda6a37");
        Log.d("TAGIMAGEGET", mCurrent);
        if (!mHasPicPosition) {
            holder.imgView.setImageResource(R.drawable.testpic);
        } else {
            try {
                Glide.with(cxt).load(imageRef).into(holder.imgView);
            } catch (Exception e) {
                holder.imgView.setImageResource(R.drawable.testpic);
                Log.e("TAG", "IMG PULL FAILED", e);
            }
        }

    }*/

    private void displayTextInfo(recyclerAdapter_chSlots.ViewHolder holder, int position) {
        if (numEntriesIndicator > 1) {holder.titleItemView.setText("Day " + String.valueOf(position) + " Submissions:");}
        else {holder.titleItemView.setText("All Submissions:");}


        isLoggedIn = PreferenceData.getUserLoggedInStatus(holder.context);

        /*if (isLoggedIn) {
            holder.submit.setText("Submit An Entry");
        } else {
            holder.submit.setText("Sign in to submit.");
        }*/
    }

    private void populateCards(@NonNull final recyclerAdapter_chSlots.ViewHolder holder, int position) {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        final FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        final ArrayList<challengeSlotDetail> mCSD = new ArrayList<>();
        final int posCopy = position;

        CollectionReference sortRef = firestoreDB.collection("challengeSlotURIs").document(challengeID)
                .collection(String.valueOf(position));
        Query query = sortRef.orderBy("timeStamp", Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        challengeSlotDetail CSD = document.toObject(challengeSlotDetail.class);
                        mCSD.add(CSD);
                    }
                    Log.d("TAG","populate cards running " + String.valueOf(mCSD.size()));
                    holder.mAdapterIndividual = new recyclerAdapter_slotDetail(holder.context, mCSD, challengeID,
                            numEntries, chName, posCopy, numEntriesIndicator);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(holder.context, LinearLayoutManager.HORIZONTAL,false);
                    LinearLayoutManager layoutManagerSingle = new GridLayoutManager(holder.context, 3);
                    //layoutManager.setInitialPrefetchItemCount(mCSD.size());
                    //mRecyclerViewIndividual.setLayoutManager(new LinearLayoutManager(cxt, LinearLayoutManager.HORIZONTAL,false));
                    holder.mRecyclerViewIndividual.setAdapter(holder.mAdapterIndividual);

                    if (numEntriesIndicator > 1) {
                        holder.mRecyclerViewIndividual.setLayoutManager(layoutManager);
                    } else {
                        holder.mRecyclerViewIndividual.setLayoutManager(layoutManagerSingle);
                    }


                    //mRecyclerViewIndividual.setRecycledViewPool(viewPool);

                } else {
                    Log.d("TAG", "ERROR GETTING INDIV SUBMISSIONS: ", task.getException());
                }
            }
        });
    }
    @Override
    public int getItemCount() {
        return picCount;
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }
}