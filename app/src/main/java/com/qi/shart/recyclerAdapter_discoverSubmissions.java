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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class recyclerAdapter_discoverSubmissions extends RecyclerView.Adapter<recyclerAdapter_discoverSubmissions.ViewHolder> {

    private FirebaseStorage firestoreStorage = FirebaseStorage.getInstance();
    private LayoutInflater mInflater;
    private ImageView imageView;
    private TextView wordItemView;
    private TextView titleItemView;
    private FirebaseFirestore firestoreDB;
    private Context cxt;
    private String imagePath;
    private TextView descView;
    private TextView dateView;
    private TextView posterID;
    private SimpleDateFormat sfd = new SimpleDateFormat("MM-dd-yyyy");
    private int numPos;
    private Button deleteButton;
    private recyclerAdapter_discoverSubmissions.ViewHolder VH;
    private ArrayList<challengeSlotDetail> mCSD;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appBarLayout;
    private int widthUse;
    private Long cacheKey;

    public recyclerAdapter_discoverSubmissions(Context context, ArrayList<challengeSlotDetail> mCSD, int widthUse) {
        mInflater = LayoutInflater.from(context);
        this.mCSD = mCSD;
        this.cxt = context;
        this.widthUse = widthUse;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imgView;

        final recyclerAdapter_discoverSubmissions mAdapter;
        private final Context context;
        private ViewHolder(@NonNull View itemView, recyclerAdapter_discoverSubmissions adapter) {
            super(itemView);
            titleItemView = itemView.findViewById(R.id.title_ch_slots);
            imgView = itemView.findViewById(R.id.testingPic_view_slots);
            View inflatedView = mInflater.inflate(R.layout.profilefragment, null);
            collapsingToolbarLayout = inflatedView.findViewById(R.id.collapsing_layout);
            appBarLayout = inflatedView.findViewById(R.id.appbar_layout);

            //descView = itemView.findViewById(R.id.desc_view_slots);
            //dateView = itemView.findViewById(R.id.TS_view_slots);
            //posterID = itemView.findViewById(R.id.authorID_view_slots);
            imgView = itemView.findViewById(R.id.testingPic_view_slots);
            deleteButton = itemView.findViewById(R.id.deleteTest);

            this.mAdapter = adapter;
            context = itemView.getContext();
            itemView.setOnClickListener(this);
            imgView.setOnClickListener(this);
            deleteButton.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.testingPic_view_slots:
                    //fullScreenImageOnClick(); //OLD ONE
                    viewAllOption(); //clicking individual pic goes to scrolling feed like insta
                    break;
                /*case R.id.likeButton_view_slots:
                    updateLikes();
                    break;*/
                case R.id.deleteTest:
                    deleteHandler dH = new deleteHandler(cxt, mCSD, getAdapterPosition());
                    dH.createAlertDialogForDelete();
                    break;
                default:
                    break;
            }
        }

        public void fullScreenImageOnClick() {
            Intent intent = new Intent(cxt, imageFullScreenView.class);
            //EditText editText = (EditText) findViewById(R.id.editText);
            //String message = editText.getText().toString();

            challengeSlotDetail CSD = mCSD.get(getAdapterPosition());
            String imagePath = CSD.getimageURI() + "/" + CSD.getimageURI() + "_" + CSD.getNumPos() + "_"
                    + CSD.getposterID();
            Long cacheKey = CSD.getCacheKey();
            intent.putExtra("cacheKey",cacheKey);
            intent.putExtra("imagePath", imagePath);
            intent.putExtra("CSD", CSD);
            cxt.startActivity(intent);
        }

        public void viewAllOption() {
            Log.d("TAG","TRYING TO GO TO VIEW ALL PAGE..");
            Fragment nextFrag = new slotDetailSubmissionFragment();
            Bundle mBundle = new Bundle();
            mBundle.putInt("numPos", getLayoutPosition());
            mBundle.putString("posterID",""); //not used
            mBundle.putString("posterName",""); //not used
            mBundle.putString("queryMode","allSubmissions");
            nextFrag.setArguments(mBundle);
            MainActivity mainActivity = (MainActivity) cxt;

            CustomViewPager mpager = mainActivity.findViewById(R.id.mainpager);
            mpager.setPagingEnabled(false);

            mainActivity.switchContent(R.id.discoverFragment_layout, nextFrag);
        }
    }



    @NonNull
    @Override
    public recyclerAdapter_discoverSubmissions.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.recycler_item_slot_detail, parent, false);
        this.imageView = mItemView.findViewById(R.id.testingPic_view_slots);
        if (widthUse!=0) {
            CardView cV = mItemView.findViewById(R.id.recyclerView_view_slots);
            GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams)
                    cV.getLayoutParams();
            layoutParams.height = widthUse;
            layoutParams.width = widthUse;
        }

        imageView = mItemView.findViewById(R.id.testingPic_view_slots);
        VH = new recyclerAdapter_discoverSubmissions.ViewHolder(mItemView, this);
        return VH;
    }

    @Override
    public void onBindViewHolder(@NonNull final recyclerAdapter_discoverSubmissions.ViewHolder holder, final int position) {
        numPos = position;
        displayTextInfo(holder, position);
        retrieveFromFirebase(holder, position);
        //if (position == mCSD.size()-1) {
        //    disableScroll();
        //}
    }

    private void cacheImages(Bitmap bmp, recyclerAdapter_discoverSubmissions.ViewHolder holder) {
        Glide.with(cxt)
                .load(bmp)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imgView);
    }

    private void dlFromFirebase(StorageReference imageRef, Bitmap bmp, final recyclerAdapter_discoverSubmissions.ViewHolder holder) {
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

    private void retrieveFromFirebase(final recyclerAdapter_discoverSubmissions.ViewHolder holder, int position) {
        challengeSlotDetail CSD = mCSD.get(position);
        cacheKey = CSD.getCacheKey();
        imagePath = CSD.getimageURI() + "/" + CSD.getimageURI() + "_" + CSD.getNumPos() + "_"
                + CSD.getposterID();
        StorageReference storageRef = firestoreStorage.getReference();
        StorageReference imageRef = storageRef.child(imagePath);
        try {
            Glide.with(cxt).load(imageRef)
                    .signature(new ObjectKey(String.valueOf(cacheKey)))
                    .into(holder.imgView);
        } catch (Exception e) {
            holder.imgView.setImageResource(R.drawable.testpic);
            Log.e("TAG", "IMG PULL FAILED", e);
        }
    }

    private void displayTextInfo(recyclerAdapter_discoverSubmissions.ViewHolder holder, int position) {
        //descView.setText(mChallenges.get(position).getdesc());
        //dateView.setText(sfd.format(mChallenges.get(position).getTS()));
        //posterID.setText(mChallenges.get(position).getUsername());
    }

    @Override
    public int getItemCount() {
        return mCSD.size();
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void enableScroll() {
        final AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams)
                collapsingToolbarLayout.getLayoutParams();
        params.setScrollFlags(
                AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                        | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
        );
        collapsingToolbarLayout.setLayoutParams(params);
    }

    private void disableScroll() {
        final AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams)
                collapsingToolbarLayout.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL);
        collapsingToolbarLayout.setLayoutParams(params);
        Log.d("delete","SCROLL DISABLED");

        CoordinatorLayout.LayoutParams params12 = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        params12.setBehavior(new AppBarLayout.Behavior());
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params12.getBehavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                return false;
            }
        });
    }
}