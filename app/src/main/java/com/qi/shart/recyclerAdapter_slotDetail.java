package com.qi.shart;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
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
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

public class recyclerAdapter_slotDetail extends RecyclerView.Adapter<recyclerAdapter_slotDetail.ViewHolder> {
    private FirebaseStorage firestoreStorage = FirebaseStorage.getInstance();
    private LayoutInflater mInflater;
    private ImageView imageView;
    private Context cxt;
    private FirebaseFirestore firestoreDB;
    private String uuidValue;
    private int numPos, likeNum;
    private TextView descView, dateView, posterID, likeView;
    private ImageButton likeButton;
    private ImageView imgView;
    private String imagePath;
    private ArrayList<challengeSlotDetail> mCSD;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private Boolean likedAlready, isLoggedIn;
    private Button deleteButton;
    private SimpleDateFormat sfd = new SimpleDateFormat("MM-dd-yyyy");
    private int numEntries;
    private int dayNum;
    private String chName;
    private int numEntriesIndicator;

    public recyclerAdapter_slotDetail(Context context, ArrayList<challengeSlotDetail> mCSD, String challengeID, int numEntries,
                                      String chName, int dayNum, int numEntriesIndicator) {
        mInflater = LayoutInflater.from(context);
        this.cxt = context;
        this.mCSD = mCSD;
        this.uuidValue = challengeID;
        this.numEntries = numEntries;
        this.chName = chName;
        this.dayNum=dayNum;
        this.numEntriesIndicator = numEntriesIndicator;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final recyclerAdapter_slotDetail mAdapter;
        private final Context context;
        private ViewHolder(@NonNull View itemView, recyclerAdapter_slotDetail adapter) {
            super(itemView);
            Log.d("TAG","INNER RECYCLER STARTING UP");
            //titleItemView = itemView.findViewById(R.id.title_ch_slots);
            //descView = itemView.findViewById(R.id.desc_view_slots);
            //dateView = itemView.findViewById(R.id.TS_view_slots);
            //posterID = itemView.findViewById(R.id.authorID_view_slots);
            imgView = itemView.findViewById(R.id.testingPic_view_slots);
            //likeView = itemView.findViewById(R.id.likes_view_slots);
            //likeButton = itemView.findViewById(R.id.likeButton_view_slots);

            deleteButton = itemView.findViewById(R.id.deleteTest);

            this.mAdapter = adapter;
            context = itemView.getContext();
            isLoggedIn = PreferenceData.getUserLoggedInStatus(context);
            itemView.setOnClickListener(this);
            imgView.setOnClickListener(this);
            deleteButton.setOnClickListener(this);
            //likeButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.testingPic_view_slots:
                    if (getAdapterPosition()==0) {
                        if (isLoggedIn) {
                            goToChSubmitPage(getAdapterPosition());
                        } else {
                            goToSignInPage();
                        }
                    } else {
                        //fullScreenImageOnClick();
                        goToViewSlotPage();
                    }
                    break;
                /*case R.id.likeButton_view_slots:

                    likeHandler lH = new likeHandler(cxt, mCSD, getAdapterPosition(), likeButton, likeView);
                    lH.updateLikes();
                    break;*/
                case R.id.deleteTest:
                    deleteHandler dH = new deleteHandler(cxt, mCSD, getAdapterPosition());
                    dH.createAlertDialogForDelete();
                    break;
                default:
                    break;
            }
        }


        public void goToViewSlotPage() { //VIEW ALL
            /*final Intent intentview;
            intentview = new Intent(context, slotDetailActivity.class);
            intentview.putExtra("numPos",getLayoutPosition());
            intentview.putExtra("docID",challengeID);
            intentview.putExtra("chName",chName);
            intentview.putExtra("numEntries",numEntries);
            context.startActivity(intentview);*/

            Log.d("TAG","TRYING TO GO TO VIEW ALL PAGE..");
            Fragment nextFrag = new slotDetailFragment();
            Bundle mBundle = new Bundle();
            mBundle.putInt("numPos", dayNum);
            mBundle.putString("docID", uuidValue);
            mBundle.putString("chName", chName);
            mBundle.putInt("numEntries", numEntries);
            nextFrag.setArguments(mBundle);
            MainActivity mainActivity = (MainActivity) cxt;

            CustomViewPager mpager = mainActivity.findViewById(R.id.mainpager);
            mpager.setPagingEnabled(false);

            mainActivity.switchContent(R.id.mainFragment_layout, nextFrag);

        }
        public void fullScreenImageOnClick() {
            /*Intent intent = new Intent(cxt, imageFullScreenView.class);
            challengeSlotDetail CSD = mCSD.get(getAdapterPosition()-1);
            String imagePath = CSD.getimageURI() + "/" + CSD.getimageURI() + "_" + CSD.getNumPos() + "_" + CSD.getposterID();
            intent.putExtra("CSD", CSD);
            intent.putExtra("imagePath", imagePath);
            intent.putExtra("cacheKey",CSD.getCacheKey());
            cxt.startActivity(intent);*/
            Log.d("TAG","TRYING TO GO TO INDIV DETAIL PAGE..");
            Fragment nextFrag = new imageFullScreenFragment();
            Bundle mBundle = new Bundle();
            challengeSlotDetail CSD = mCSD.get(getAdapterPosition()-1);
            String imagePath = CSD.getimageURI() + "/" + CSD.getimageURI() + "_" + CSD.getNumPos() + "_" + CSD.getposterID();
            mBundle.putParcelable("CSD", CSD);
            mBundle.putString("imagePath", imagePath);
            mBundle.putLong("cacheKey", CSD.getCacheKey());
            nextFrag.setArguments(mBundle);
            MainActivity mainActivity = (MainActivity) cxt;

            CustomViewPager mpager = mainActivity.findViewById(R.id.mainpager);
            mpager.setPagingEnabled(false);

            mainActivity.switchContent(R.id.mainFragment_layout, nextFrag);

        }
        public void goToChSubmitPage(int adapterPos) {
            Intent intent = new Intent(cxt, SubmitSlotActivity.class);

            intent.putExtra("dayNum",dayNum);

            intent.putExtra("chID",uuidValue);
            Log.d("TAG", "the chID is " + uuidValue);
            intent.putExtra("chName",chName);
            intent.putExtra("numEntries",numEntries);
            cxt.startActivity(intent);


        }
        public void goToSignInPage() {
            final Intent intentSignIn;
            intentSignIn = new Intent(context, SignInActivity.class);
            context.startActivity(intentSignIn);
        }
    }

    public int getWidthUse() {
        Display display = ((WindowManager) cxt.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int widthWM = size.x;
        return (widthWM - 4) / 3;
    }
    @NonNull
    @Override
    public recyclerAdapter_slotDetail.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("TAGASDF","BINDING???");
        View mItemView = mInflater.inflate(R.layout.recycler_item_slot_detail, parent, false);
        imageView = mItemView.findViewById(R.id.testingPic_view_slots);

        if (numEntriesIndicator <=1) {
            int widthUse = getWidthUse();
            CardView cV = mItemView.findViewById(R.id.recyclerView_view_slots);
            GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams)
                    cV.getLayoutParams();
            layoutParams.height = widthUse;
            layoutParams.width = widthUse;
        }

        return new ViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull final recyclerAdapter_slotDetail.ViewHolder holder, final int position) {
        numPos = position - 1;
        if (numPos >= 0) {
            displayTextInfo(numPos);
            retrieveFromFirebase(holder, numPos);
        } else {
            imgView.setImageResource(R.drawable.addtemp);
        }

    }

    private void cacheImages(Bitmap bmp, recyclerAdapter_slotDetail.ViewHolder holder) {
        Glide.with(cxt)
                .load(bmp)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgView);
    }

    private void dlFromFirebase(StorageReference imageRef, Bitmap bmp, final recyclerAdapter_slotDetail.ViewHolder holder) {
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
                imgView.setImageResource(R.drawable.testpic);
                Log.d("TAG", "IMG PULL FAILED");
            }
        });
    }

    private void retrieveFromFirebase(final recyclerAdapter_slotDetail.ViewHolder holder, int numPosition) {
        long cacheKey = mCSD.get(numPosition).getCacheKey();
        uuidValue = mCSD.get(numPosition).getimageURI();
        imagePath = uuidValue + "/" + uuidValue + "_" + mCSD.get(numPosition).getNumPos() + "_" + mCSD.get(numPosition).getposterID();
        Log.d("TAG","imagepath is " + imagePath);
        StorageReference storageRef = firestoreStorage.getReference();
        StorageReference imageRef = storageRef.child(imagePath);
        //StorageReference imageRefTest = firestoreStorage.getReferenceFromUrl("gs://shart-74bb2.appspot.com/images/ef21a7b4-5c9c-4072-8589-27692eda6a37");
        try {
            Glide.with(cxt).load(imageRef)
                    .signature(new ObjectKey(String.valueOf(cacheKey)))
                    .into(imgView);
            Log.d("TAG", "PULL OK");
        } catch (Exception e) {
            imgView.setImageResource(R.drawable.testpic);
            Log.e("TAG", "IMG PULL FAILED", e);
        }
    }

    private void displayTextInfo(int position) {
        /*String posterIDString = mCSD.get(numPos).getposterID();
        likedAlready = mCSD.get(position).getLikes().contains(posterIDString);
        if (likedAlready) {
            likeButton.setImageResource(R.drawable.likedtemp);
        } else {
            likeButton.setImageResource(R.drawable.liketemp);
        }
        likeNum = mCSD.get(position).getLikeNum();
        descView.setText("Description: " + mCSD.get(position).getdesc());
        dateView.setText("Date: " + sfd.format(mCSD.get(position).getTS()));
        //posterID.setText("Submitted by: " + mCSD.get(position).getUsername());
        likeView.setText(String.valueOf(likeNum));*/
    }

    @Override
    public int getItemCount() {
        return mCSD.size() + 1;
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
