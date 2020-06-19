package com.qi.shart;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.Objects;


public class profileMySubmissions_fragment extends Fragment {
    private Context ctx;
    private TextView noEntries;
    private RecyclerView mRecyclerViewSubmissions;
    private FirebaseFirestore firestoreDB;
    private ArrayList<challengeSlotDetail> mCSD;
    private String docID, posterID, posterName;
    private profileParticipation PP;

    public profileMySubmissions_fragment() {
        // Required empty public constructor
    }

    public static profileMySubmissions_fragment newInstance(profileParticipation PP,
                                                            ArrayList<challengeSlotDetail> mCSD, String posterID,
                                                            String posterName) {
        profileMySubmissions_fragment fragment = new profileMySubmissions_fragment();
        Bundle args = new Bundle();
        args.putParcelable("PPobject", PP);
        args.putParcelableArrayList("CSDobject", mCSD);
        args.putString("posterID", posterID);
        args.putString("posterName", posterName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("FRAGMENT", "creating view");
        // Inflate the layout for this fragment
        ctx = getContext();
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_profile_my_submissions_fragment, container, false);
        mRecyclerViewSubmissions = rootView.findViewById(R.id.recyclerview_pf_submissions);
        noEntries = rootView.findViewById(R.id.pf_submissions_noEntries);
        //TextView txt = (TextView) rootView.findViewById(R.id.aboutMelabel);
        //txt.setText("TESTING IS THIS WORKING");
        if (getArguments() != null) {
            mCSD = getArguments().getParcelableArrayList("CSDobject");
            PP = getArguments().getParcelable("PPobject");
            posterID = getArguments().getString("posterID");
            posterName = getArguments().getString("posterName");
        }
        pullSubmissions();
        return rootView;
    }

    private void pullSubmissions() {
        Configuration configuration = Objects.requireNonNull(getActivity()).getResources().getConfiguration();
        Display display = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int widthWM = size.x;
        float configDensity = configuration.densityDpi;
        int screenWidthDp = configuration.screenWidthDp;
        float factor = Objects.requireNonNull(getContext()).getResources().getDisplayMetrics().density;
        //int varWidth =(int) ( factor * screenWidthDp);
        Log.d("TAG","Screen width dp is " + screenWidthDp + " and density from metrics is "
                + factor + " and from config is " + configDensity + " and widthWM is " + widthWM);

        int useThisWidth = (widthWM - 4) / 3;
        Log.d("TAG","WTF???");
        if (mCSD.size()==0) {
            noEntries.setText("No Submissions so far!");
        } else {
            recyclerAdapter_pfSubmissions mAdapterSubmissions = new recyclerAdapter_pfSubmissions(getContext(), mCSD, useThisWidth, posterID,
                    posterName);
            // Connect the adapter with the RecyclerView.
            mRecyclerViewSubmissions.setAdapter(mAdapterSubmissions);
            // Give the RecyclerView a default layout manager.
            mRecyclerViewSubmissions.setLayoutManager(new GridLayoutManager(ctx, 3));
        }


    }


    /*private void pullSubmissions() {
        firestoreDB = FirebaseFirestore.getInstance();
        slotDetails = new LinkedList<challengeSlotDetail>();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        docID = user.getUid();
        CollectionReference sortRef = firestoreDB.collection(docID);
        Query query = sortRef.orderBy("ts", Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        slotDetails.add(document.toObject(challengeSlotDetail.class));
                    }
                    mAdapterSubmissions = new recyclerAdapter_pfSubmissions(getContext(), slotDetails);
                    // Connect the adapter with the RecyclerView.
                    mRecyclerViewSubmissions.setAdapter(mAdapterSubmissions);

                    // Give the RecyclerView a default layout manager.
                    mRecyclerViewSubmissions.setLayoutManager(new GridLayoutManager(ctx, 3));
                } else {
                    Log.d("TAG", "ERROR GETTING DOCUMENTS: ", task.getException());
                }
            }
        });
    }*/
}
