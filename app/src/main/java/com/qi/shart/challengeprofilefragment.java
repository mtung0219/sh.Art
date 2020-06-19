package com.qi.shart;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class challengeprofilefragment extends Fragment implements View.OnClickListener{

    private String docID;
    private FirebaseFirestore firestoreDB;
    private ImageView imageView;
    private FirebaseStorage firestoreStorage = FirebaseStorage.getInstance();
    private boolean hasPic = false;
    private TextView joinButton;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String UID;
    private boolean isLoggedIn;
    private boolean amIDoingThisChallenge = false;
    private RecyclerView mRecyclerView;
    private TextView amiinIndicator;
    private recyclerAdapter_chSlots mAdapter;
    private int challengePicCount = 0;
    private String chName, comingFrom;
    private Challenge ch;
    private Context ctx;
    private View rootView;

    public challengeprofilefragment() {
        // Required empty public constructor
    }

    public static challengeprofilefragment newInstance(String docID, String comingFrom) {
        challengeprofilefragment fragment = new challengeprofilefragment();
        Bundle args = new Bundle();
        args.putString("docID", docID);
        args.putString("comingFrom",comingFrom);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            docID = getArguments().getString("docID");
            comingFrom = getArguments().getString("comingFrom");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ctx = getContext();
        rootView = inflater.inflate(R.layout.challengeprofile, container, false);
        imageView = rootView.findViewById(R.id.challengeprofilePic);
        mRecyclerView = rootView.findViewById(R.id.recyclerview_chSlots);

        //button adds me to challenge if signed in, otherwise goes to create account
        joinButton = rootView.findViewById(R.id.doThisChallengeButton);
        amiinIndicator = rootView.findViewById(R.id.chpf_ImInIndicator);
        joinButton.setOnClickListener(this);
        isLoggedIn = PreferenceData.getUserLoggedInStatus(ctx);
        if (isLoggedIn) UID = user.getUid();

        pullProfile();
        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.doThisChallengeButton:

                if (isLoggedIn && !amIDoingThisChallenge) {
                    addMeToChallenge(view);
                } else if (!isLoggedIn && !amIDoingThisChallenge) {
                    startActivity(new Intent(ctx, SignInActivity.class));
                } else { removeMeFromChallenge(view); }
                break;
            default:
                break;
        }

    }

    private void setJoinButtonText() {
        if (amIDoingThisChallenge && isLoggedIn) { //i am in this challenge
            joinButton.setText(R.string.leavechallenge);
            amiinIndicator.setVisibility(View.VISIBLE);
        } else if (isLoggedIn) { //i am not in this challenge
            UID = user.getUid();
            joinButton.setText(R.string.joinchallenge);
            amiinIndicator.setVisibility(View.INVISIBLE);
        } else { //i am not logged in
            joinButton.setText(R.string.signinforchallenge);
            amiinIndicator.setVisibility(View.INVISIBLE);
        }
    }

    private void pullPic() {
        StorageReference storageRef = firestoreStorage.getReference();
        String imagePath = "images/" + docID;
        StorageReference imageRef = storageRef.child(imagePath);
        if (!hasPic) {
            //imageView.setImageResource(R.drawable.testpic);
        } else {
            try {
                Glide.with(this)
                        .load(imageRef)
                        /*.error(
                                Glide.with(this)
                                .load(R.drawable.testpic)
                        )*/
                        .into(imageView);
            } catch (Exception e) {
                //imageView.setImageResource(R.drawable.testpic);
                Log.e("TAG", "IMG PULL FAILED", e);
            }
        }
    }

    private void pullProfile() {
        //pulls challenge profile
        firestoreDB = FirebaseFirestore.getInstance();
        DocumentReference docpath = firestoreDB.collection("challenges").document(docID);
        Log.d("TAG",docID);
        docpath.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d("TAG","task successful...");
                    if (task.getResult()==null) Log.d("TAG","no results??");
                    ch = task.getResult().toObject(Challenge.class);
                    Log.d("TAG","toObject successful..");
                    TextView author = rootView.findViewById(R.id.chpf_author);
                    TextView hashtag = rootView.findViewById(R.id.chpf_hashtag);
                    TextView numEntries = rootView.findViewById(R.id.chpf_numDays);
                    TextView platform = rootView.findViewById(R.id.chpf_platform);
                    TextView desc = rootView.findViewById(R.id.chpf_desc);
                    TextView dateRange = rootView.findViewById(R.id.chpf_startdate);
                    TextView title = rootView.findViewById(R.id.chpf_title);
                    if (ch!=null) {
                        Date sD = ch.getStartDate();
                        Date eD = ch.getEndDate();
                        final DateFormat dateformatter = new SimpleDateFormat("MM/dd/yyyy");
                        String dateText = dateformatter.format(sD) + " - " + dateformatter.format(eD);

                        author.setText(ch.getAuthorId());
                        hashtag.setText(ch.getHashtag());
                        numEntries.setText(String.valueOf(ch.getnumEntries()));
                        platform.setText(ch.getPlatform());
                        desc.setText(ch.getDesc());
                        dateRange.setText(dateText);
                        title.setText(ch.getTitle());
                        hasPic = ch.getHasPic();
                        chName = ch.getTitle();
                    }

                    if (ch.getnumEntries() != 0)  challengePicCount = ch.getnumEntries();
                    pullParticipants();
                } else {
                    Log.d("TAG","ERROR GETTING DOCUMENTS: ", task.getException());
                }
            }
        });
    }

    private void pullParticipants() {
        firestoreDB = FirebaseFirestore.getInstance();
        DocumentReference docpath = firestoreDB.collection("challengeParticipation").document(docID);
        docpath.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    challengeParticipation chP = task.getResult().toObject(challengeParticipation.class);
                    Log.d("TAG","toObject successful..");
                    TextView participantNum = rootView.findViewById(R.id.chpf_participantNum);;
                    if (chP!=null) {
                        participantNum.setText(String.valueOf(chP.getCurrParticipants()));
                        if (isLoggedIn) {
                            if (chP.getActiveParticipants().contains(UID)) {
                                int uidIndex = chP.getActiveParticipants().indexOf(UID);
                                amIDoingThisChallenge = chP.getParticipantActive().get(uidIndex);
                            } else { amIDoingThisChallenge = false; }
                        }
                    }
                    setJoinButtonText();
                    getChSlots();
                    pullPic();

                } else {
                    Log.d("TAG","ERROR GETTING PARTICIPANTS: ", task.getException());
                }
            }
        });
    }

    public void removeMeFromChallenge(View v) {
        //profile list from challenge point of view
        final DocumentReference docpath = firestoreDB.collection("challengeParticipation").document(docID);
        //challenge list from profile point of view
        final DocumentReference pDocpath = firestoreDB.collection("profileParticipation").document(UID);
        //active participants in challenges
        final DocumentReference cDocpath = firestoreDB.collection("challenges").document(docID);

        firestoreDB.runTransaction(new Transaction.Function<Boolean>() {

            @Nullable
            @Override
            public Boolean apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                DocumentSnapshot ds = transaction.get(docpath);
                DocumentSnapshot profileDS = transaction.get(pDocpath);
                //DocumentSnapshot cDS = transaction.get(cDocpath);

                double currParticipants = ds.getDouble("currParticipants") - 1;

                ArrayList<String> newActiveParticipants = (ArrayList<String>) ds.get("activeParticipants");
                ArrayList<Boolean> newParticipantsActive = (ArrayList<Boolean>) ds.get("participantActive");

                ArrayList<String> challengesImDoing = (ArrayList<String>) profileDS.get("challengesParticipating");
                ArrayList<Boolean> amIActive = (ArrayList<Boolean>) profileDS.get("isActive");

                transaction.update(docpath,"currParticipants",currParticipants);
                transaction.update(cDocpath, "activeParticipants",currParticipants);
                //get the current user ID
                int CPindexPos = newActiveParticipants.indexOf(UID);
                newParticipantsActive.set(CPindexPos, false);

                transaction.update(docpath,"activeParticipants",newActiveParticipants);
                transaction.update(docpath,"participantActive",newParticipantsActive);

                if (challengesImDoing.contains(docID)) {
                    int indexPos = challengesImDoing.indexOf(docID);
                    amIActive.set(indexPos, false);
                    transaction.update(pDocpath, "isActive", amIActive);
                }
                return true;
            }
        }).addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean itWorked) {
                if (itWorked) {
                    Log.d("TAG", "remove me from Challenge success!");
                    pullParticipants();
                } else {
                    Log.d("TAG","transaction on success but not re-pulling participants?");
                }
            }
        }) .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("TAG", "Couldn't remove me from challenge!.", e);
            }
        });
    }

    public void addMeToChallenge(View v) {
        //profile list from challenge point of view
        final DocumentReference docpath = firestoreDB.collection("challengeParticipation").document(docID);
        //challenge list from profile point of view
        final DocumentReference pDocpath = firestoreDB.collection("profileParticipation").document(UID);
        //challenge docPath
        final DocumentReference challengeDocPath = firestoreDB.collection("challenges").document(docID);

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
                if (!newActiveParticipants.contains(UID)) {
                    newActiveParticipants.add(UID);
                    newParticipantsActive.add(true);
                } else {
                    int properIndex = newActiveParticipants.indexOf(UID);
                    newParticipantsActive.set(properIndex, true);
                }


                transaction.update(docpath,"activeParticipants",newActiveParticipants);
                transaction.update(docpath,"participantActive",newParticipantsActive);
                if (!challengesImDoing.contains(docID)) {
                    challengesImDoing.add(docID);
                    challengesHasPic.add(hasPic);
                    challengeTitle.add(title);
                    isActive.add(true);
                    challengeActive.add(true);
                    isDone.add(false);
                } else {
                    int properIndex = challengesImDoing.indexOf(docID);
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
                    Log.d("TAG", "add me to Challenge success!");
                    pullParticipants();
                } else {
                    Log.d("TAG","transaction on success but not re-pulling participants?");
                }
            }
        }) .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("TAG", "Couldn't add me to challenge!.", e);
            }
        });
    }

    private void getChSlots() {
        if (challengePicCount==0) return;
        firestoreDB = FirebaseFirestore.getInstance();

        if (ch.getnumEntries() > 1) {
            mAdapter = new recyclerAdapter_chSlots(ctx, ch, docID,2, comingFrom);
            RecyclerView.LayoutManager layoutManager = new CustomLinearLayoutManager(ctx);
            //mAdapter = new recyclerAdapter_chSlots(this, challengePicCount, docID, chName);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        } else {
            mAdapter = new recyclerAdapter_chSlots(ctx, ch, docID,1, comingFrom);
            RecyclerView.LayoutManager layoutManager = new CustomLinearLayoutManager(ctx);
            //mAdapter = new recyclerAdapter_chSlots(this, challengePicCount, docID, chName);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(ctx));
            //mRecyclerView.setLayoutManager(new GridLayoutManager(ctx, 3));
        }

    }
}