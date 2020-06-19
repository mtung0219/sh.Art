package com.qi.shart;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

public class imageFullScreenView extends AppCompatActivity implements View.OnClickListener {


    private String imagePath, myID, challengeID, challengeNum, posterID;
    private ImageView imgView, likeButton;
    private Long cacheKey;
    private int likeNum;
    private challengeSlotDetail CSD;
    private TextView title, chName, likeView, date, desc;
    private Boolean likedAlready, amILoggedIn;
    private DocumentReference docpath,pDocpath;
    private FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
    private Context ctx;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_full_screen_view);
        ctx = this;
        imgView = findViewById(R.id.fullscreen_ImageView);
        imagePath = getIntent().getStringExtra("imagePath");
        cacheKey = getIntent().getLongExtra("cacheKey",0);
        CSD = getIntent().getParcelableExtra("CSD");

        title = findViewById(R.id.fullscreen_title);
        chName = findViewById(R.id.fullscreen_ch);
        likeView = findViewById(R.id.fullscreen_likeNum);
        date = findViewById(R.id.fullscreen_postDate);
        desc = findViewById(R.id.fullscreen_desc);
        likeButton = findViewById(R.id.fullscreen_likeImg);

        title.setText(CSD.getUsername());
        chName.setText(CSD.getChName() + " day " + (CSD.getNumPos()+1));
        likeView.setText(String.valueOf(CSD.getLikeNum()));
        date.setText(String.valueOf(CSD.getTS()));
        desc.setText(CSD.getdesc());
        myID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likeButton.setOnClickListener(this);

        challengeID = CSD.getimageURI();
        challengeNum = String.valueOf(CSD.getNumPos());
        posterID = CSD.getposterID();
        docpath = firestoreDB.collection("challengeSlotURIs")
                .document(challengeID).collection(challengeNum)
                .document(challengeID + "_" + challengeNum + "_" + posterID);


        //poster docpath
        pDocpath = firestoreDB.collection(posterID)
                .document(challengeID + "_" + challengeNum);

        title.setOnClickListener(this);
        amILoggedIn = PreferenceData.getUserLoggedInStatus(ctx);
        showImage();
        likeRefresh();
    }

    private void showImage() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(imagePath);
        try {
            Glide.with(this).load(imageRef)
                    .signature(new ObjectKey(cacheKey))
                    .error(
                            Glide.with(this)
                            .load(R.drawable.testpic)
                    )
                    .into(imgView);
            Log.d("TAG", "PULL OK");
        } catch (Exception e) {
            imgView.setImageResource(R.drawable.testpic);
            Log.e("TAG", "IMG PULL FAILED", e);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fullscreen_likeImg:
                if (amILoggedIn) updateLikeHelper();
                break;
            case R.id.fullscreen_title:
                goToProfilePage();
            default:
                break;
        }
    }

    public void goToProfilePage() {
        Log.d("TAG","TRYING TO GO TO A PROFILE PAGE..");
        String posterIDuse = posterID;
        String myUID = user.getUid();
        MainActivity mainActivity = (MainActivity) ctx;
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

            mainActivity.switchContent(R.id.mainFragment_layout, nextFrag);
        }

    }

    public void likeRefresh() {
        String challengeNum = String.valueOf(CSD.getNumPos());
        String posterID = CSD.getposterID();
        String challengeID = CSD.getimageURI();
        DocumentReference docpath = FirebaseFirestore.getInstance().collection("challengeSlotURIs")
                .document(CSD.getimageURI()).collection(challengeNum)
                .document(challengeID + "_" + challengeNum + "_" + posterID);

        //re-pulling like statistics
        docpath.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Log.d("TAG","CSD UPDATED");
                CSD = task.getResult().toObject(challengeSlotDetail.class);

                boolean likedAlready = CSD.getLikes().contains(myID);
                int likeNumber = CSD.getLikeNum();
                likeView.setText(String.valueOf(likeNumber));
                if (likedAlready) { likeButton.setImageResource(R.drawable.likedtemp); }
                else { likeButton.setImageResource(R.drawable.liketemp); }

                likeButton.setClickable(true);
            }
        });
    }


    public void updateLikeHelper() {
        //re-pulling like statistics after a change

        //get the UI updater first when you press button
        likeButton.setClickable(false);
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

        updateLikes();
        /*docpath.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Log.d("TAG","CSD UPDATED");
                CSD = task.getResult().toObject(challengeSlotDetail.class);
                Log.d("TAG","likedalready after CSD updated is " + CSD.getLikes().contains(myID));

                likedAlready = CSD.getLikes().contains(myID);
                likeNum = CSD.getLikeNum();

                updateLikes();
            }
        });*/
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

                likeRefresh();

            }
        }) .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("TAG", "Couldn't add like!", e);
                if (likedAlready) { //reverse things on failure
                    likeNum+=1;
                    likeButton.setImageResource(R.drawable.likedtemp);
                } else {
                    likeNum-=1;
                    likeButton.setImageResource(R.drawable.liketemp);
                }
                likeView.setText(String.valueOf(likeNum));

                Toast.makeText(ctx, "Couldn't change Like at this time. Please try again later.",Toast.LENGTH_LONG).show();
                likeButton.setClickable(true);
            }
        });
    }
}