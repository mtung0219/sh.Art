package com.qi.shart;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class profilefragment extends Fragment implements View.OnClickListener {
    private Context ctx;
    private FirebaseStorage firestoreStorage = FirebaseStorage.getInstance();
    private FirebaseFirestore firestoreDB;
    private String docID, posterID, posterName;
    private View rootView;
    private Profile pf;
    private Uri uriIG, uriDA, uriTwitter;
    private final int PICK_IMAGE_REQUEST = 71;
    private final int EXTERNAL_STORAGE_REQUEST = 70;
    private Uri filePath;
    private Uri destination;

    private RecyclerView mRecyclerView;
    private RecyclerView mRecyclerViewSubmissions;
    private recyclerAdapter_pfCurrChallenge mAdapter;
    private recyclerAdapter_pfSubmissions mAdapterSubmissions;
    private profileParticipation PP;
    private ArrayList<challengeSlotDetail> slotDetails;
    private ViewPager profilePager;
    private ViewPageAdapterProfile viewPageAdapterProfile;
    private Toolbar toolbar_ChgHeight;
    private CircleImageView profpic;
    private Button changeProfilePic;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private StorageReference ref;
    private byte[] BAtest;

    public static profilefragment getInstance(String posterID) {
        profilefragment fragment = new profilefragment();
        Bundle args = new Bundle();
        args.putString("posterID", posterID);
        //args.putParcelable("CSDobject", (Parcelable) mCSD);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getContext();
        boolean isUserLoggedIn = PreferenceData.getUserLoggedInStatus(ctx);
        if (isUserLoggedIn) docID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (getArguments() != null) {
            //pf = getArguments().getParcelable("profile");
            posterID = getArguments().getString("posterID");

            Log.d("TAG","ARGUMENTS WERE NOT NULL AND POSTERID IS " + posterID);
        } else {
            posterID = docID;
            Log.d("TAG","ARGUMENTS WERE NULL AND POSTERID IS " + posterID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.profilefragment, container, false);
        profpic = rootView.findViewById(R.id.imageview_account_profile);
        //changeProfilePic = rootView.findViewById(R.id.changeProfilePicture);

        final LinearLayout LL = rootView.findViewById(R.id.lltest1);
        CoordinatorLayout CL = rootView.findViewById(R.id.coordinator_layout);

        toolbar_ChgHeight = rootView.findViewById(R.id.toolbarChgHeight);

        rootView.findViewById(R.id.igLink).setOnClickListener(this);
        rootView.findViewById(R.id.deviantArtLink).setOnClickListener(this);
        rootView.findViewById(R.id.twitterLink).setOnClickListener(this);
        profpic.setOnClickListener(this);
        //changeProfilePic.setOnClickListener(this);

        mRecyclerView = rootView.findViewById(R.id.recyclerview_pf_currchallenge);
        mRecyclerViewSubmissions = rootView.findViewById(R.id.recyclerview_pf_submissions);
        TextView username = rootView.findViewById(R.id.usernameDisplay);

        //pf = getArguments().getParcelable("profile");
        //if (pf!=null) username.setText(pf.getUsername());
        pullProfile();
        //setSMLinks();
        //pullChallengesDoing();
        //retrieveFromFirebase();

        return rootView;
    }

    @Override
    public void onResume() {
        Log.d("FRAGMENT","PROFILE FRAGMENT RESUMED");
        //setUpTabs();
        super.onResume();
    }

    private void setUpTabs() {
        int challengesCompleted = 0;
        int totalLikes = 0;
        ArrayList<Boolean> isDone = PP.getIsDone();
        for (int i=0;i<isDone.size();i++) {
            if (isDone.get(i)) challengesCompleted+=1;
        }
        for (int i=0;i<slotDetails.size();i++) {
            totalLikes+=slotDetails.get(i).getLikeNum();
        }
        TextView numChCompleted = rootView.findViewById(R.id.numChCompleted);
        TextView numPosts = rootView.findViewById(R.id.numPosts);
        TextView numLikes = rootView.findViewById(R.id.numLikes);

        numChCompleted.setText(String.valueOf(challengesCompleted));
        numPosts.setText(String.valueOf(slotDetails.size()));
        numLikes.setText(String.valueOf(totalLikes));

        profilePager = rootView.findViewById(R.id.profilepager);
        profilePager.setOffscreenPageLimit(0);
        Log.d("TAG","SLOTDETAILS SIZE IS " + String.valueOf(slotDetails.size()));

        viewPageAdapterProfile = new ViewPageAdapterProfile(getFragmentManager(), ctx, PP, slotDetails, posterID, posterName);
        profilePager.setAdapter(viewPageAdapterProfile);

        TabLayout tabLayout = rootView.findViewById(R.id.profiletabs);
        tabLayout.setupWithViewPager(profilePager);
    }

    private void setSMLinks() {
        String instagram = pf.getInstagram();
        String deviantArt = pf.getDeviantArt();
        String twitter = pf.getTwitter();

        TextView igLink = rootView.findViewById(R.id.igLink);
        uriIG = Uri.parse("https://www.instagram.com/" + instagram);
        /*String igLinkString = "&lt;a href=\"https://www.instagram.com/" + instagram + "\">test ig link&lt;/a>";
        Spanned testlinkspanned = Html.fromHtml(igLinkString);
        igLink.setText(testlinkspanned);
        igLink.setClickable(true);
        igLink.setMovementMethod(LinkMovementMethod.getInstance());*/
    }

    private void pullProfile() {
        firestoreDB = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //docID = user.getUid();
        DocumentReference docpath = firestoreDB.collection("profiles").document(posterID);
        docpath.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d("TAG","profile get successful...");
                    if (task.getResult()==null) Log.d("TAG","no results??");
                    pf = task.getResult().toObject(Profile.class);
                    Log.d("TAG","profile toObject successful..");
                    TextView username = rootView.findViewById(R.id.usernameDisplay);
                    TextView realname = rootView.findViewById(R.id.pf_name);
                    TextView bio = rootView.findViewById(R.id.pf_aboutMe);

                    if (pf!=null) {
                        posterName = pf.getUsername();
                        username.setText(posterName);
                        realname.setText(pf.getRealName());
                        bio.setText(pf.getDesc());
                    }
                    setSMLinks();
                    pullChallengesDoing();
                    retrieveFromFirebase();
                } else {
                    Log.d("TAG","ERROR GETTING PROFILE: ", task.getException());
                }
            }
        });
    }

    //TO GET profileParticipation object
    private void pullChallengesDoing() {
        firestoreDB = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //docID = user.getUid();
        DocumentReference docpath = firestoreDB.collection("profileParticipation").document(posterID);
        docpath.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d("TAG","profile get successful...");
                    PP = task.getResult().toObject(profileParticipation.class);
                    pullSubmissions();
                    //setUpTabs();

                    Log.d("TAG","profile toObject successful..");
                } else {
                    Log.d("TAG","ERROR GETTING PROFILE: ", task.getException());
                }
            }
        });
    }

    private void pullSubmissions() {
        slotDetails = new ArrayList<>();
        CollectionReference sortRef = firestoreDB.collection(posterID);
        Query query = sortRef.orderBy("ts", Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        slotDetails.add(document.toObject(challengeSlotDetail.class));
                    }
                    setUpTabs();
                } else {
                    Log.d("TAG", "ERROR GETTING DOCUMENTS: ", task.getException());
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.igLink:
                goToIGPage();
                break;
            case R.id.imageview_account_profile:
                goToChgProfilePic();
            default:
                break;
        }
    }

    private void goToChgProfilePic() {
        final String[] profilePicOptions = new String[]{"Change Profile Pic","Remove Profile Pic"};
        ArrayAdapter<String> platformAdapter = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_dropdown_item, profilePicOptions);

        new AlertDialog.Builder(ctx).setTitle("Edit Profile Pic:")
                .setAdapter(platformAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //platformButton.setText(profilePicOptions[i]);
                        String platform = profilePicOptions[i];
                        dialogInterface.dismiss();

                        if (platform.equals(profilePicOptions[i])) {
                            startImageSelection();
                        }
                    }
                }).create().show();
    }

    public void startImageSelection() {
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //intent.setType("image/*");
        //intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("delete","REQUEST CODE IS " + String.valueOf(requestCode));

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            //RelativeLayout submitLayout = getActivity().findViewById(R.id.submitchallengeLayout);
            //int layoutWidth = submitLayout.getWidth();
            destination = Uri.fromFile(new File(ctx.getCacheDir(),"profPic_" + System.currentTimeMillis()));
            UCrop.Options options = new UCrop.Options();
            options.setCircleDimmedLayer(true);
            UCrop.of(filePath, destination)
                    .withAspectRatio(1,1)
                    .withOptions(options)
                    .withMaxResultSize(500,500)
                    .start(ctx, this, UCrop.REQUEST_CROP);

        } else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Log.d("delete","CROP RESULT STARTING");
            final Uri resultUri = UCrop.getOutput(data);
            //imageView.setImageURI(resultUri);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), destination);
                ByteArrayOutputStream baout = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.WEBP,25,baout);
                BAtest = baout.toByteArray();
                baout.close();

                uploadImage();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void uploadImage() {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        pf.updateProfPicCache(); //updating cache num by 1 upon profile pic change

        if (destination != null) {
            final ProgressDialog progressDialog = new ProgressDialog(ctx);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            ref = storageReference.child("profilePictures/" + posterID);
            UploadTask ulTask = ref.putBytes(BAtest);
            ulTask
                    //ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            retrieveFromFirebase();
                            updateProfilePicCache();
                            Toast.makeText(ctx, "UPLOAD SUCCESSFUL!",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ctx, "FAILED UPLOAD!",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

    private void updateProfilePicCache() {
        firestoreDB.collection("profiles").document(posterID).set(pf);
    }

    //pulling profile picture
    private void retrieveFromFirebase() {
        long cacheKey = pf.getProfPicCache();
        String imagePath = "profilePictures/" + posterID;
        StorageReference storageRef = firestoreStorage.getReference();
        StorageReference imageRef = storageRef.child(imagePath);
        try {
            Glide.with(ctx)
                    .load(imageRef)
                    .signature(new ObjectKey(String.valueOf(cacheKey)))
                    .error(
                            Glide.with(ctx)
                            .load(R.mipmap.ic_launcher_round)
                    )
                    .into(profpic);
        } catch (Exception e) {
            profpic.setImageResource(R.mipmap.ic_launcher_round);
            Log.e("TAG", "IMG PULL FAILED", e);
        }
    }

    private void goToMainAfterLogout() {
        Intent intent = new Intent(ctx, MainActivity.class);
        //EditText editText = (EditText) findViewById(R.id.editText);
        //String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
    private void goToAddLinks() {
        Intent intent = new Intent(ctx, AddSocialMediaActivity.class);
        //EditText editText = (EditText) findViewById(R.id.editText);
        //String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
    private void goToIGPage() {
        Intent intent = new Intent(Intent.ACTION_VIEW, uriIG);
        startActivity(intent);
    }

}