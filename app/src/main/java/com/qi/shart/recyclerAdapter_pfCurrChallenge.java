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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class recyclerAdapter_pfCurrChallenge extends RecyclerView.Adapter<recyclerAdapter_pfCurrChallenge.ViewHolder> {
    private final ArrayList<String> mChIDs;
    private final ArrayList<String> mTitles;
    private final ArrayList<Boolean> mHasPic;
    private FirebaseStorage firestoreStorage = FirebaseStorage.getInstance();
    private LayoutInflater mInflater;
    private ImageView imageView, doneGraphic;
    private TextView titleItemView, progressFractionView;
    private Context cxt;
    private ProgressBar pBar;
    private final HashMap<String,Integer> chProgress, chTemp;

    public recyclerAdapter_pfCurrChallenge(Context context, ArrayList<String> mTitles,ArrayList<String> mChIDs,ArrayList<Boolean> mHasPic,
            HashMap<String,Integer> chProgress, HashMap<String,Integer> chTemp) {

        this.mTitles = mTitles;
        mInflater = LayoutInflater.from(context);
        this.mChIDs = mChIDs;
        this.cxt = context;
        this.mHasPic = mHasPic;
        this.chProgress = chProgress;
        this.chTemp = chTemp;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imgView;
        final recyclerAdapter_pfCurrChallenge mAdapter;
        private final Context context;
        private ViewHolder(@NonNull View itemView, recyclerAdapter_pfCurrChallenge adapter) {
            super(itemView);
            titleItemView = itemView.findViewById(R.id.title_pfcurrchallenge);
            pBar = itemView.findViewById(R.id.pb_default);
            progressFractionView = itemView.findViewById(R.id.fractionProgress_pfcurrchallenge);
            doneGraphic = itemView.findViewById(R.id.doneTemp_pfcurrchallenge);
            imgView = itemView.findViewById(R.id.testingPic_pfcurrchallenge);
            this.mAdapter = adapter;
            context = itemView.getContext();
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            // Get the position of the item that was clicked.
            int mPosition = getLayoutPosition();
            // Use that to access the affected item in mWordList.
            String element = mChIDs.get(mPosition);
            goToChProfilePage(view, element);
            // Change the word in the mWordList.
            //mChIDs.set(mPosition, "Clicked " + element);
            // Notify the adapter, that the data has changed so it can update the RecyclerView to display the data.
            //mAdapter.notifyDataSetChanged();
        }

        public void goToChProfilePage(View v, String docID) {

            Log.d("TAG","TRYING TO GO TO CHALLENGE PAGE..");
            Fragment nextFrag = new challengeprofilefragment();
            Bundle mBundle = new Bundle();
            mBundle.putString("docID", docID);
            mBundle.putString("comingFrom","profileFragment");
            nextFrag.setArguments(mBundle);
            MainActivity mainActivity = (MainActivity) cxt;

            CustomViewPager mpager = mainActivity.findViewById(R.id.mainpager);
            mpager.setPagingEnabled(false);

            mainActivity.switchContent(R.id.profileFragment_layout, nextFrag);

            /*final Intent intent;
            intent = new Intent(context, ChallengeProfile.class);
            intent.putExtra("docID",docID);
            //EditText editText = (EditText) findViewById(R.id.editText);
            //String message = editText.getText().toString();
            //intent.putExtra(EXTRA_MESSAGE, message);
            context.startActivity(intent);*/
        }
    }

    @NonNull
    @Override
    public recyclerAdapter_pfCurrChallenge.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.recycler_item_pf_currchallenge, parent, false);
        this.imageView = mItemView.findViewById(R.id.testingPic_pfcurrchallenge);
        return new recyclerAdapter_pfCurrChallenge.ViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull final recyclerAdapter_pfCurrChallenge.ViewHolder holder, final int position) {
        displayTextInfo(holder, position);
        retrieveFromFirebase(holder, position);
    }

    private void cacheImages(Bitmap bmp, recyclerAdapter_pfCurrChallenge.ViewHolder holder) {
        Glide.with(cxt)
                .load(bmp)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imgView);
    }

    private void dlFromFirebase(StorageReference imageRef, Bitmap bmp, final recyclerAdapter_pfCurrChallenge.ViewHolder holder) {
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

    private void retrieveFromFirebase(final recyclerAdapter_pfCurrChallenge.ViewHolder holder, int position) {
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

    }

    private void displayTextInfo(recyclerAdapter_pfCurrChallenge.ViewHolder holder, int position) {

        String idGet = mChIDs.get(position);
        Log.d("TAG",idGet);
        int numerator = 0;
        int denominator = 0;
        if (chProgress.size() >0 && chTemp.size() >0) {
            Log.d("TAG","CHPROGRESS SIZE IS " + chProgress.size());
            numerator = chProgress.get(idGet);
            denominator = chTemp.get(idGet);
        }
        String titleString = mTitles.get(position);
        titleItemView.setText(titleString);
        progressFractionView.setText(String.valueOf(numerator) + "/" + String.valueOf(denominator));

        if (numerator == denominator) {
            adjustCompletedChallenge(denominator);
        } else {
            pBar.setProgress((int) (((double) numerator / denominator) * 100.0));
        }
    }

    private void adjustCompletedChallenge(int denom) {
        pBar.setVisibility(View.INVISIBLE);
        if (denom == 0) { //not started yet
            progressFractionView.setText("Not started yet.");
            doneGraphic.setVisibility(View.INVISIBLE);
        }//done
        else {doneGraphic.setVisibility(View.VISIBLE);}

    }

    @Override
    public int getItemCount() {
        return mChIDs.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

}