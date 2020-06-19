package com.qi.shart;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class profileMyChallenges_fragment extends Fragment {
    private Context ctx;
    private View rootView;
    private TextView noEntriesCh;
    private RecyclerView mRecyclerView;
    private FirebaseFirestore firestoreDB;
    private recyclerAdapter_pfCurrChallenge mAdapter;
    private String docID;
    private profileParticipation PP;
    private ArrayList<challengeSlotDetail> mCSD;
    private HashMap<String,Integer> mChProgress,mChTemp;

    public profileMyChallenges_fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            PP = getArguments().getParcelable("PPobject");
            mCSD = getArguments().getParcelableArrayList("CSDobject");
        }
    }
    public static profileMyChallenges_fragment newInstance(profileParticipation PP,
                                                           ArrayList<challengeSlotDetail> mCSD) {
        profileMyChallenges_fragment fragment = new profileMyChallenges_fragment();
        Bundle args = new Bundle();
        args.putParcelable("PPobject", PP);
        args.putParcelableArrayList("CSDobject", mCSD);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("FRAGMENT","creating view");
        ctx = getContext();
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_profile_my_challenges_fragment, container, false);
        mRecyclerView = rootView.findViewById(R.id.recyclerview_pf_currchallenge);
        noEntriesCh = rootView.findViewById(R.id.pf_currchallenge_noEntries);
        if (getArguments() != null) {
            Log.d("FRAGMENT","PP POPULATED FROM BUNDLE challenge");
            getChallengesDoingCards(PP);
        }
        return rootView;
    }

    @Override
    public void onResume() {
        //pullChallengesDoing();
        super.onResume();
    }

    private void buildChallengeProgressMap() {
        //mChProgress maps challenge IDs with # completed
        ArrayList<String> mChallengeList = PP.getChallengesParticipating();
        ArrayList<Boolean> isActive = PP.getIsActive();
        mChProgress = new HashMap<>();
        mChTemp = new HashMap<>();
        for (int i = 1; i < mChallengeList.size(); i++) {
            //initialize the hashMaps
            if (isActive.get(i)) {
                mChProgress.put(mChallengeList.get(i), 0);
                mChTemp.put(mChallengeList.get(i),0);
            }
        }

        for (int i=0; i< mCSD.size();i++) {
            String imgKey = mCSD.get(i).getimageURI();
            if (mChTemp.containsKey(imgKey)) {
                mChTemp.put(imgKey, mCSD.get(i).getNumEntries());

                //chProgress should already contain key if active. this means you have to join the challenge to post to it
                int progress = mChProgress.get(imgKey) + 1;
                mChProgress.put(imgKey, progress);
            }

        }
    }

    private void getChallengesDoingCards(profileParticipation PP) {
        buildChallengeProgressMap();

        ArrayList<String> challenges = PP.getChallengesParticipating();
        ArrayList<String> titles = PP.getTitle();
        ArrayList<Boolean> hasPic = PP.getHasPic();
        ArrayList<Boolean> isActive = PP.getIsActive();
        if (challenges.size()==1) return;
        challenges.remove(0);
        titles.remove(0);
        hasPic.remove(0);
        isActive.remove(0);
        firestoreDB = FirebaseFirestore.getInstance();
        ArrayList<String> mTitlePass, mChIDPass;
        ArrayList<Boolean> mHasPicPass;
        mTitlePass = new ArrayList<>();
        mChIDPass = new ArrayList<>();
        mHasPicPass = new ArrayList<>();
        for (int i = 0; i < isActive.size(); i++) {
            if (isActive.get(i)) {
                mTitlePass.add(titles.get(i));
                mChIDPass.add(challenges.get(i));
                mHasPicPass.add(hasPic.get(i));
            }
        }
        if (mTitlePass.isEmpty()) {
            noEntriesCh.setText("No Challenges In Progress!");
        } else {
            mAdapter = new recyclerAdapter_pfCurrChallenge(getContext(), mTitlePass,mChIDPass,mHasPicPass, mChProgress, mChTemp);

            mRecyclerView.setAdapter(mAdapter);

            mRecyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        }
    }
}
