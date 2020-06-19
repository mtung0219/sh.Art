package com.qi.shart;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

public class mainfragment extends Fragment implements View.OnClickListener {

    private LinkedList<String> mTitles, challengeIDs;
    private LinkedList<Challenge> mChallenges;
    private ArrayList<Challenge> mFullChallengesList;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private LinkedList<Boolean> hasPic;
    private DatabaseReference mDatabase;
    private RecyclerView mRecyclerView;
    private recyclerAdapter mAdapter;
    private FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
    private TextView logInText, chTypeSeries, chTypeDTIYS, chTypeOther, sortButton,filterButton;
    private ListView chTypeButton;
    private String sortItems[], filterItems[], chTypeItems[];
    private ArrayAdapter<String> sortAdapter, filterAdapter, chTypeAdapter;
    private Context ctx;
    private String sortValue, filterValue, sortValueBackup, filterValueBackup;
    private int widthUse;
    private String myUserID;
    private boolean isUserLoggedIn;
    private profileParticipation PP;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.mainfragment, container, false);
        //Button buttonInFragment1 = rootView.findViewById(R.id.button_1);
        mRecyclerView = rootView.findViewById(R.id.recyclerview);
        logInText = rootView.findViewById(R.id.whoIsLoggedIn);

        ctx = rootView.getContext();

        chTypeSeries = rootView.findViewById(R.id.mainFragment_seriesButton);
        chTypeDTIYS = rootView.findViewById(R.id.mainFragment_dtiysButton);
        chTypeOther = rootView.findViewById(R.id.mainFragment_otherButton);
        chTypeSeries.setSelected(true);chTypeDTIYS.setSelected(true);chTypeOther.setSelected(true);
        chTypeSeries.setOnClickListener(this);chTypeDTIYS.setOnClickListener(this);chTypeOther.setOnClickListener(this);

        sortButton = rootView.findViewById(R.id.mainFragment_orderBy);
        sortButton.setOnClickListener(this);
        sortItems = new String[]{"Most Recent","Most Submissions","Most Participants"};
        sortAdapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_spinner_dropdown_item, sortItems);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        filterButton = rootView.findViewById(R.id.mainFragment_filterBy);
        filterButton.setOnClickListener(this);
        filterItems = new String[]{"Active Challenges","Past Challenges","All Challenges"};
        filterAdapter = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_dropdown_item, filterItems);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        /*chTypeButton = rootView.findViewById(R.id.chTypeList);
        chTypeItems = new String[]{"Series","DTIYS","Other"};
        chTypeAdapter = new ArrayAdapter<>(ctx, android.R.layout.simple_list_item_multiple_choice, chTypeItems);
        chTypeButton.setAdapter(chTypeAdapter);*/

        TextView timelineButton = rootView.findViewById(R.id.mainFragment_tempTimeline);
        timelineButton.setOnClickListener(this);

        setHasOptionsMenu(true);
        checklogin();

        sortValue = "timeStamp";
        filterValue = "allChallenges";
        setButtonText();
        if (isUserLoggedIn) {
            profileParticipationPull();
        } else {
            initialCardPull();
        }
        //initialCardPull();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void checklogin() {
        isUserLoggedIn = PreferenceData.getUserLoggedInStatus(getContext());
        if (!isUserLoggedIn) {
            logInText.setText("Not logged in.");
            myUserID="notLoggedIn";
            //goToLoginPage();
        } else {
            //signInAnonymously();
            String logInString = "Currently logged in: " + PreferenceData.getLoggedInUsername(getContext());
            myUserID = user.getUid();
            logInText.setText(logInString);
        }
    }

    public void sortButtonClick() {
        new AlertDialog.Builder(ctx).setTitle("Order By:")
                .setAdapter(sortAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        sortValueBackup = sortValue;
                        if (i==0) { // most recent
                            sortValue = "timeStamp";
                            initialCardPull();
                        } else if (i==1) { //most submissions
                            sortValue = "numSubmissions";
                            initialCardPull();
                        } else if (i==2) { //most participants
                            sortValue = "activeParticipants";
                            initialCardPull();
                        }
                    }
                }).create().show();
    }

    public void filterButtonClick() {
        new AlertDialog.Builder(ctx).setTitle("Filter By:")
                .setAdapter(filterAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        filterValueBackup = filterValue;
                        if (i==0) { // active challenges
                            filterValue = "activeOnly";
                            initialCardPull();
                        } else if (i==1) { //past challenges
                            filterValue = "pastOnly";
                            initialCardPull();
                        } else if (i==2) { //all challenges
                            filterValue = "allChallenges";
                            initialCardPull();
                        }
                    }
                }).create().show();
    }

    private void setButtonText() {
        switch (sortValue) {
            case "timeStamp":
                sortButton.setText("Most Recent ▼");break;
            case "numSubmissions":
                sortButton.setText("Most Submissions ▼");break;
            case "activeParticipants":
                sortButton.setText("Most Participants ▼");break;
        }

        switch (filterValue) {
            case "activeOnly":
                filterButton.setText("Active Only ▼");break;
            case "pastOnly":
                filterButton.setText("Past Only ▼");break;
            case "allChallenges":
                filterButton.setText("Past + Present ▼");break;
        }
    }

    private Query setQuery() {
        Query queryStore = FirebaseFirestore.getInstance().collection("challenges");
        queryStore = queryStore.orderBy(sortValue, Query.Direction.DESCENDING);
        return queryStore;
    }

    /*public void refreshCards() {

        mDatabase = FirebaseDatabase.getInstance().getReference();
        firestoreDB = FirebaseFirestore.getInstance();

        Query query = setQuery();
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    clientSideFilter(task);

                    setButtonText();

                    mAdapter = new recyclerAdapter(getContext(), challengeIDs, mTitles, hasPic);
                    mRecyclerView.setAdapter(mAdapter);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                } else {
                    Log.d("TAG", "ERROR GETTING DOCUMENTS: ", task.getException());
                    //sortValue = sortValueBackup; //in case sort didn't go through
                }
            }
        });
    }*/

    public void initialCardPull() {
        mChallenges = new LinkedList<>();
        mFullChallengesList = new ArrayList<>();
        Query query = setQuery(); //set order
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Challenge ch = document.toObject(Challenge.class);
                        ch.setChID(document.getId());
                        mChallenges.add(ch);
                        mFullChallengesList.add(ch);
                    }
                    //mFullChallengesList = mChallenges;
                    clientSideFilter2();
                } else {
                    Log.d("TAG", "ERROR GETTING INITIAL CARD PULL: ", task.getException());
                }
            }
        });
    }

    public void profileParticipationPull() {
        DocumentReference docpath = firestoreDB.collection("profileParticipation").document(myUserID);
        docpath.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    PP = task.getResult().toObject(profileParticipation.class);
                    initialCardPull();
                    Log.d("TAG","toObject successful..");
                } else {
                    Log.d("TAG","ERROR GETTING PARTICIPANTS: ", task.getException());
                }
            }
        });
    }

    private void clientSideFilter2() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long todayLong = cal.getTimeInMillis();
        final Date today = new Date(todayLong);

        for (Iterator<Challenge> iter = mChallenges.iterator(); iter.hasNext();) {
            Challenge ch = iter.next();
            switch (filterValue) {
                case "activeOnly":
                    if (ch.getEndDate().before(today)) {
                        iter.remove();continue;
                    } break;
                case "pastOnly":
                    if (!ch.getEndDate().before(today)) {
                        iter.remove();continue;
                    } break;
            }
            if (!chTypeSeries.isSelected() && ch.getChType().equals("Series")) {
                iter.remove();continue;
            }
            if (!chTypeDTIYS.isSelected() && ch.getChType().equals("DTIYS")) {
                iter.remove();continue;
            }
            if (!chTypeOther.isSelected() && ch.getChType().equals("Other")) {
                iter.remove();continue;
            }
        }
        setButtonText();
        setWidth();
        mAdapter = new recyclerAdapter(ctx, mChallenges, widthUse, PP);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    }

    public void setWidth() {
        Configuration configuration = getActivity().getResources().getConfiguration();
        Display display = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int widthWM = size.x;
        widthUse = (widthWM*8/8);
    }

    private void clientSideFilter(Task<QuerySnapshot> task) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long todayLong = cal.getTimeInMillis();
        final Date today = new Date(todayLong);

        switch (filterValue) {
            case "activeOnly":
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (!document.getTimestamp("endDate").toDate().before(today)) {
                        challengeIDs.addLast(document.getId());
                        Challenge ch = document.toObject(Challenge.class);
                        mTitles.addLast(ch.getTitle());
                        hasPic.addLast(ch.getHasPic());
                    }
                } break;
            case "pastOnly":
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (document.getTimestamp("endDate").toDate().before(today)) {
                        challengeIDs.addLast(document.getId());
                        Challenge ch = document.toObject(Challenge.class);
                        mTitles.addLast(ch.getTitle());
                        hasPic.addLast(ch.getHasPic());
                    }
                } break;
            default:
                for (QueryDocumentSnapshot document : task.getResult()) {
                    challengeIDs.addLast(document.getId());
                    Challenge ch = document.toObject(Challenge.class);
                    mTitles.addLast(ch.getTitle());
                    hasPic.addLast(ch.getHasPic());
                } break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mainFragment_orderBy:
                sortButtonClick();break;
            case R.id.mainFragment_filterBy:
                filterButtonClick();break;
            case R.id.mainFragment_seriesButton:
                if (chTypeSeries.isSelected()) {
                    chTypeSeries.setSelected(false);
                    clientSideFilter2();
                } else {
                    chTypeSeries.setSelected(true);
                    initialCardPull();
                }
                break;
            case R.id.mainFragment_dtiysButton:
                if (chTypeDTIYS.isSelected()) {
                    chTypeDTIYS.setSelected(false);
                    clientSideFilter2();
                } else {
                    chTypeDTIYS.setSelected(true);
                    initialCardPull();
                }
                break;
            case R.id.mainFragment_otherButton:
                if (chTypeOther.isSelected()) {
                    chTypeOther.setSelected(false);
                    clientSideFilter2();
                } else {
                    chTypeOther.setSelected(true);
                    initialCardPull();
                }
                break;
            case R.id.mainFragment_tempTimeline:
                goToTimeline();
            default: break;
        }
    }

    public void goToTimeline() {
        /*Intent intent = new Intent(ctx, timelineActivity.class);
        //EditText editText = (EditText) findViewById(R.id.editText);
        //String message = editText.getText().toString();
        intent.putExtra("profileParticipation",PP);
        intent.putParcelableArrayListExtra("completeChallenges",mFullChallengesList);
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);*/
        Fragment nextFrag = new timelineFragment();
        Bundle mBundle = new Bundle();
        mBundle.putParcelableArrayList("completeChallenges", mFullChallengesList);
        nextFrag.setArguments(mBundle);
        MainActivity mainActivity = (MainActivity) ctx;
        CustomViewPager mpager = mainActivity.findViewById(R.id.mainpager);
        mpager.setPagingEnabled(false);

        mainActivity.switchContent(R.id.mainFragment_layout, nextFrag);

    }
}