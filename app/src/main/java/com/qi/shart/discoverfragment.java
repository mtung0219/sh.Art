package com.qi.shart;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class discoverfragment extends Fragment {

    private FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
    private ArrayList<challengeSlotDetail> mCSD;
    private Context ctx;
    private recyclerAdapter_discoverSubmissions mAdapterSubmissions;
    private RecyclerView mRecyclerViewSubmissions;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.discoverfragment, container, false);
        ctx = getContext();
        mRecyclerViewSubmissions = rootView.findViewById(R.id.recyclerview_discover_submissions);
        pullSubmissions();
        return rootView;
    }

    private void pullSubmissions() {
        Configuration configuration = getActivity().getResources().getConfiguration();
        Display display = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int widthWM = size.x;
        float configDensity = configuration.densityDpi;
        int screenWidthDp = configuration.screenWidthDp;
        float factor = getContext().getResources().getDisplayMetrics().density;
        int varWidth =(int) ( factor * screenWidthDp);
        Log.d("TAG","Screen width dp is " + screenWidthDp + " and density from metrics is "
                + factor + " and from config is " + configDensity + " and widthWM is " + widthWM);

        final int useThisWidth = (widthWM - 4) / 3;
        mCSD = new ArrayList<>();
        CollectionReference sortRef = firestoreDB.collection("allSubmissions");
        Query query = sortRef.orderBy("ts", Query.Direction.DESCENDING);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        mCSD.add(document.toObject(challengeSlotDetail.class));
                    }

                    mAdapterSubmissions = new recyclerAdapter_discoverSubmissions(getContext(), mCSD, useThisWidth);
                    mRecyclerViewSubmissions.setAdapter(mAdapterSubmissions);
                    mRecyclerViewSubmissions.setLayoutManager(new GridLayoutManager(ctx, 3));

                } else {
                    Log.d("TAG", "error getting submissions in discover tab: ", task.getException());
                }
            }
        });
    }
}
