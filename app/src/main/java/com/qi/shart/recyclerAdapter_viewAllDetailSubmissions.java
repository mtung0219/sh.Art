package com.qi.shart;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class recyclerAdapter_viewAllDetailSubmissions extends RecyclerView.Adapter<recyclerAdapter_viewAllDetailSubmissions.ViewHolder> {
    private FirebaseStorage firestoreStorage = FirebaseStorage.getInstance();
    private LayoutInflater mInflater;
    private ImageView imageView;
    private Context cxt;
    private FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
    private String uuidValue;
    private int numPos, likeNum;
    private TextView descView, dateView, posterIDView, usernameView, chView;

    private ImageView imgView;
    private String imagePath, myID;
    private ArrayList<challengeSlotDetail> mCSD;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private Boolean likedAlready, amILoggedIn;
    private Button deleteButton;
    private SimpleDateFormat sfd = new SimpleDateFormat("MM-dd-yyyy");
    private int numEntries;
    private int dayNum;
    private String chName;
    private String challengeNum, posterID, challengeID;
    private DocumentReference docpath, pDocpath;
    private final slotDetailSubmissionFragment.OnLikeClickListener likeClickListener;


    //private ImageView likeButton;
    //private TextView likeView;

    public recyclerAdapter_viewAllDetailSubmissions(Context context, ArrayList<challengeSlotDetail> mCSD,
                                                    slotDetailSubmissionFragment.OnLikeClickListener listener) {
        mInflater = LayoutInflater.from(context);
        this.cxt = context;
        this.mCSD = mCSD;
        this.uuidValue = challengeID;
        myID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.likeClickListener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView likeView;
        private ImageView likeButton;
        private WeakReference<slotDetailSubmissionFragment.OnLikeClickListener> likeClickListenerVH;

        final recyclerAdapter_viewAllDetailSubmissions mAdapter;
        private final Context context;
        private ViewHolder(@NonNull View itemView, recyclerAdapter_viewAllDetailSubmissions adapter,
                           slotDetailSubmissionFragment.OnLikeClickListener listener) {
            super(itemView);
            this.mAdapter = adapter;
            Log.d("TAG","INNER RECYCLER STARTING UP");
            descView = itemView.findViewById(R.id.fullscreen_desc);
            dateView = itemView.findViewById(R.id.fullscreen_postDate);
            //posterIDView = itemView.findViewById(R.id.authorID_view_slots);
            usernameView = itemView.findViewById(R.id.fullscreen_title);
            imgView = itemView.findViewById(R.id.fullscreen_ImageView);
            likeView = itemView.findViewById(R.id.fullscreen_likeNum);
            chView = itemView.findViewById(R.id.fullscreen_ch);
            likeButton = itemView.findViewById(R.id.fullscreen_likeImg);
            likeClickListenerVH = new WeakReference<>(listener);

            likeButton.setOnClickListener(this);

            context = itemView.getContext();
            amILoggedIn = PreferenceData.getUserLoggedInStatus(context);
            itemView.setOnClickListener(this);
            imgView.setOnClickListener(this);
            usernameView.setOnClickListener(this);
            //deleteButton = itemView.findViewById(R.id.deleteTest);
            //deleteButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.fullscreen_likeImg:
                    //updateLikeHelper(getAdapterPosition(), likeView, likeButton);
                    likeClickListenerVH.get().onLikeClick(getAdapterPosition(), likeButton, likeView);
                    break;
                /*case R.id.likeButton_view_slots:

                    likeHandler lH = new likeHandler(cxt, mCSD, getAdapterPosition(), likeButton, likeView);
                    lH.updateLikes();
                    break;
                case R.id.deleteTest:
                    deleteHandler dH = new deleteHandler(cxt, mCSD, getAdapterPosition());
                    dH.createAlertDialogForDelete();
                    break;*/
                case R.id.fullscreen_title:
                    goToProfilePage(getAdapterPosition());
                    break;
                default:
                    break;
            }
        }
        public ImageView getLikeButton() { return likeButton; }
        public TextView getLikeNum() { return likeView; }

        public void goToProfilePage(int pos) {
            Log.d("TAG","TRYING TO GO TO A PROFILE PAGE..");
            String posterIDuse = mCSD.get(pos).getposterID();
            String myUID = user.getUid();
            MainActivity mainActivity = (MainActivity) cxt;
            CustomViewPager mpager = mainActivity.findViewById(R.id.mainpager);
            if (amILoggedIn & posterIDuse.equals(myUID)) {
                mpager.setPagingEnabled(true);
                mainActivity.goToMyProfile();
            } else {
                Fragment nextFrag = new profilefragment();
                Bundle mBundle = new Bundle();
                //mBundle.putInt("numPos", getLayoutPosition());

                mBundle.putString("posterID", posterIDuse);
                nextFrag.setArguments(mBundle);

                mpager.setPagingEnabled(false);

                mainActivity.switchContent(R.id.discoverFragment_layout, nextFrag);
            }

        }
    }


    @NonNull
    @Override
    public recyclerAdapter_viewAllDetailSubmissions.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.activity_image_full_screen_view, parent, false);
        imageView = mItemView.findViewById(R.id.fullscreen_ImageView);
        return new recyclerAdapter_viewAllDetailSubmissions.ViewHolder(mItemView, this, likeClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final recyclerAdapter_viewAllDetailSubmissions.ViewHolder holder, final int position) {
        /*if (clickListener != null) {
            holder.itemView.setOnClickListener(clickListener);
        }
        if (likeClickListener!= null) {
            holder.getLikeButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    likeClickListener.onLikeClick(position, v, holder.getLikeNum());
                }
            });
        }*/
        displayTextInfo(position, holder);
        retrieveFromFirebase(holder, position);
    }

    private void cacheImages(Bitmap bmp, recyclerAdapter_viewAllDetailSubmissions.ViewHolder holder) {
        Glide.with(cxt)
                .load(bmp)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgView);
    }

    private void dlFromFirebase(StorageReference imageRef, Bitmap bmp, final recyclerAdapter_viewAllDetailSubmissions.ViewHolder holder) {
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

    private void retrieveFromFirebase(final recyclerAdapter_viewAllDetailSubmissions.ViewHolder holder, int numPosition) {
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

    private void displayTextInfo(int position,recyclerAdapter_viewAllDetailSubmissions.ViewHolder holder) {
        challengeSlotDetail CSD = mCSD.get(position);
        String posterIDString = mCSD.get(numPos).getposterID();
        String myID = user.getUid();
        String username = CSD.getUsername();
        int dayNum = CSD.getNumPos();
        int likeNum = CSD.getLikeNum();

        Date dt = CSD.getTS();
        descView.setText(CSD.getdesc());
        dateView.setText(sfd.format(dt));
        usernameView.setText(username);
        chView.setText(CSD.getChName() + " - Day " + dayNum);
        holder.likeView.setText(String.valueOf(likeNum));
        boolean likedAlready = CSD.getLikes().contains(myID);
        if (likedAlready) { holder.likeButton.setImageResource(R.drawable.likedtemp); }
        else {holder.likeButton.setImageResource(R.drawable.liketemp); }

        //likeRefresh(position);
    }

    public void updateLikeHelper(int pos, TextView mLikeView, ImageView mLikeButton) {
        challengeSlotDetail CSD = mCSD.get(pos);
        //re-pulling like statistics after a change
        challengeID = CSD.getimageURI();
        challengeNum = String.valueOf(CSD.getNumPos());
        posterID = CSD.getposterID();
        docpath = firestoreDB.collection("challengeSlotURIs")
                .document(challengeID).collection(challengeNum)
                .document(challengeID + "_" + challengeNum + "_" + posterID);
        pDocpath = firestoreDB.collection(posterID)
                .document(challengeID + "_" + challengeNum);

        //get the UI updater first when you press button
        mLikeButton.setClickable(false);
        likedAlready = CSD.getLikes().contains(myID);
        likeNum = CSD.getLikeNum();
        if (likedAlready) {
            likeNum-=1;
            mLikeButton.setImageResource(R.drawable.liketemp);
        } else {
            likeNum+=1;
            mLikeButton.setImageResource(R.drawable.likedtemp);
        }
        mLikeView.setText(String.valueOf(likeNum));

        updateLikes(pos, mLikeView, mLikeButton);
    }

    public void updateLikes(final int pos,final TextView mLikeView, final ImageView mLikeButton) {
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

                likeRefresh(pos,mLikeView,mLikeButton);

            }
        }) .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("TAG", "Couldn't add like!", e);
                if (likedAlready) { //reverse things on failure
                    likeNum+=1;
                    mLikeButton.setImageResource(R.drawable.likedtemp);
                } else {
                    likeNum-=1;
                    mLikeButton.setImageResource(R.drawable.liketemp);
                }
                mLikeView.setText(String.valueOf(likeNum));

                Toast.makeText(cxt, "Couldn't change Like at this time. Please try again later.",Toast.LENGTH_LONG).show();
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
                if (likedAlready) { mLikeButton.setImageResource(R.drawable.likedtemp); }
                else { mLikeButton.setImageResource(R.drawable.liketemp); }

                mLikeButton.setClickable(true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCSD.size();
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
