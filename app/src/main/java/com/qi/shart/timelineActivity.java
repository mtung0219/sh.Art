package com.qi.shart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.type.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;

public class timelineActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
    private FirebaseStorage firestoreStorage = FirebaseStorage.getInstance();
    private ArrayList<Challenge> mChallenges;
    private ArrayList<Challenge> mFullChallengesList;
    private ArrayList<Challenge> mFullListTempDTIYS;
    private recyclerAdapter_timeline mAdapterTimeline;
    private recyclerAdapter_timelineDTIYS mAdapterTimelineDTIYS;
    private Context ctx;
    private RecyclerView mRecyclerView, mRecyclerViewDTIYS;
    private int lastSelected = 0;
    private int currentSelected = 0;
    private int prevCenterPos, prevCenterPosDTIYS;
    private TextView dateNumSelected,monthNameSelected, timeTitle, DTIYStoggle, seriesList;
    private ArrayList<Date> dateArray;
    private ArrayList<String> mChallengeIDs;
    private boolean seriesViewVisible = true;
    private int direction;
    private RecyclerView moveOut, moveIn;
    private ImageView timelineImage;
    private final int bufferEitherSide = 4;
    private profileParticipation PP;
    private int currPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        this.ctx = this;
        mRecyclerView = findViewById(R.id.recyclerview_timeline);
        mRecyclerViewDTIYS = findViewById(R.id.recyclerview_timelineDTIYS);
        dateNumSelected = findViewById(R.id.dateNumSelected);
        monthNameSelected = findViewById(R.id.monthNameSelected);
        DTIYStoggle = findViewById(R.id.timelineDTIYStoggle);
        timelineImage = findViewById(R.id.timelineImage);
        DTIYStoggle.setOnClickListener(this);
        timeTitle = findViewById(R.id.monthYearTitle);
        seriesList = findViewById(R.id.seriesList);
        //this.PP = getIntent().getParcelableExtra("profileParticipation");
        this.mFullChallengesList = getIntent().getParcelableArrayListExtra("completeChallenges");

        Toolbar myToolbar = findViewById(R.id.maintoolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        timelineImage.setOnClickListener(this);

        profileParticipationPull();
    }

    /*@Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast.makeText(this, "menu item pressed", Toast.LENGTH_SHORT).show();
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_EditProfile:
                //d1.openDrawer(GravityCompat.START);
                return true;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        Log.d("TAG","clicked?");
        switch (view.getId()) {
            case R.id.timelineDTIYStoggle:
                toggleList(view);
                break;
            case R.id.timelineImage:
                if (mFullListTempDTIYS.get(currPosition).getHasPic() && !seriesViewVisible) {
                    goToDTIYSpage(currPosition);
                }
                break;
            default:
                break;
        }
    }

    public void goToDTIYSpage(int currPosition) {
        Intent intent = new Intent (this, MainActivity.class);
        intent.putExtra("GOTOCHALLENGE", "goToChallenge");
        String docID = mFullListTempDTIYS.get(currPosition).getChID();
        intent.putExtra("docID", docID);
        startActivity(intent);
    }

    public void switchContent(int id, Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(id, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void profileParticipationPull() {
        String myUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference docpath = firestoreDB.collection("profileParticipation").document(myUserID);
        docpath.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    PP = task.getResult().toObject(profileParticipation.class);

                    setUpDates();
                    pullSubmissions();
                    pullSubmissionsDTIYS();

                } else {
                    Log.d("TAG","ERROR GETTING PARTICIPANTS: ", task.getException());
                }
            }
        });
    }

    public int getWidthUse() {
        Display display = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int widthWM = size.x;
        return (widthWM / 2);
    }
    public void setUpDates() {
        dateArray = new ArrayList<Date>();
        Calendar c = Calendar.getInstance();
        Date today = new Date();

        c.setTime(today);
        dateArray.add(today);
        for (int i=0; i < 100; i++) {
            c.add(Calendar.DATE,1);
            dateArray.add(c.getTime());
        }

    }

    public void toggleList(View v) {
        Log.d("TAG","toggle list?");
        if (seriesViewVisible) {
            direction = 1;
            moveOut = mRecyclerView;
            moveIn = mRecyclerViewDTIYS;
            timeTitle.setVisibility(View.INVISIBLE);
            timelineImage.setVisibility(View.VISIBLE);
        } else {
            direction = -1;
            moveOut = mRecyclerViewDTIYS;
            moveIn = mRecyclerView;
            timeTitle.setVisibility(View.VISIBLE);
            timelineImage.setVisibility(View.INVISIBLE);
        }
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeoutanim);
        final Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fadeinanim);
        fadeOut.reset();fadeIn.reset();
        moveOut.setClickable(false); moveOut.setFocusable(false);
        moveOut.startAnimation(fadeOut);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
                moveOut.setVisibility(View.GONE);
                moveIn.setClickable(true);
                moveIn.setFocusable(true);
                moveIn.setVisibility(View.VISIBLE);
                moveIn.startAnimation(fadeIn);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        seriesViewVisible = !seriesViewVisible;
    }

    private void pullSubmissionsDTIYS() { //for DTIYS user is in

        ArrayList<String> ppChallenges = PP.getChallengesParticipating();
        mFullListTempDTIYS = mFullChallengesList;
        Log.d("TAG","PPCHALLENGES SIZE IS " + ppChallenges.size());
        for (Iterator<Challenge> iter = mFullListTempDTIYS.iterator(); iter.hasNext();) {
            Challenge ch = iter.next();
            switch (ch.getChType()) {
                case "DTIYS":
                    int ppIndex = ppChallenges.indexOf(ch.getChID());
                    if (ppIndex < 0) {iter.remove();continue;}
                    if (!PP.getIsActive().get(ppIndex) || !PP.getChallengeActive().get(ppIndex)) {
                        iter.remove();continue;}
                    break;
                case "Series":
                    iter.remove();
                    break;
            }
        } //at this point mFullListTempDTIYS contains only active DTIYS we are doing.

        mAdapterTimelineDTIYS = new recyclerAdapter_timelineDTIYS(ctx, mFullListTempDTIYS.size() + bufferEitherSide * 2,
                mFullListTempDTIYS, bufferEitherSide);
        mRecyclerViewDTIYS.setAdapter(mAdapterTimelineDTIYS);
        final CustomLinearLayoutManagerSlow CLLM = new CustomLinearLayoutManagerSlow(ctx);
        final LinearLayoutManager LLM = new LinearLayoutManager(ctx);
        mRecyclerViewDTIYS.setHorizontalFadingEdgeEnabled(true);
        mRecyclerViewDTIYS.setFadingEdgeLength(60);
        mRecyclerViewDTIYS.setLayoutManager(CLLM);

        final int backgroundColor= R.color.colorGreen;

        mRecyclerViewDTIYS.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int center =  mRecyclerViewDTIYS.getHeight() / 2;
                View centerView = mRecyclerViewDTIYS.findChildViewUnder(mRecyclerViewDTIYS.getLeft(), center);
                int centerPos = mRecyclerViewDTIYS.getChildAdapterPosition(centerView);
                currPosition = centerPos - bufferEitherSide;


                if (prevCenterPosDTIYS != centerPos) {
                    // dehighlight the previously highlighted view
                    View prevView = mRecyclerViewDTIYS.getLayoutManager().findViewByPosition(prevCenterPosDTIYS);
                    if (prevView != null) {
                        prevView.findViewById(R.id.dateNumber).setBackgroundResource(0);
                        prevView.findViewById(R.id.testRotate).setBackgroundResource(0);
                                }

                    // highlight view in the middle
                    if (centerView != null) {
                        TextView dateNumTest = centerView.findViewById(R.id.dateNumber);
                        TextView monthNameTest = centerView.findViewById(R.id.monthName);
                        TextView yearNumTest = centerView.findViewById(R.id.yearTemp);

                        dateNumTest.setBackgroundResource(backgroundColor);
                        centerView.findViewById(R.id.testRotate).setBackgroundResource(backgroundColor);

                        String num = dateNumTest.getText().toString();
                        String monthName = monthNameTest.getText().toString();
                        String yearNum = yearNumTest.getText().toString();

                        dateNumSelected.setText(num);
                        monthNameSelected.setText(monthName);
                        timeTitle.setText(monthName + " " + yearNum);
                    }

                    prevCenterPosDTIYS = centerPos;
                }

                populateimageDTIYS(centerPos);
            }
        });
    }

    private void populateimageDTIYS(int pos) {
        //int position = pos - bufferEitherSide;
        if (pos < bufferEitherSide || pos >= (bufferEitherSide + mFullListTempDTIYS.size())) return;
        int position = pos - bufferEitherSide;
        String challengeID = mFullListTempDTIYS.get(position).getChID();
        final boolean mHasPicPosition = mFullListTempDTIYS.get(position).getHasPic();

        String imagePath = "images/" + challengeID;
        Log.d("TAG",imagePath);
        StorageReference storageRef = firestoreStorage.getReference();
        StorageReference imageRef = storageRef.child(imagePath);
        Log.d("TAGIMAGEGET", challengeID);
        if (!mHasPicPosition) {
            timelineImage.setImageResource(R.drawable.testpic);
            //timelineImage.setBackgroundResource(0);
            //timelineImage.getLayoutParams().height= ViewGroup.LayoutParams.WRAP_CONTENT;

            //holder.imgView.setImageResource(R.drawable.testpic);
        } else {
            try {
                //timelineImage.setBackgroundResource(R.drawable.imageinputborder);
                Glide.with(this)
                        .load(imageRef)
                        .error(
                                Glide.with(this)
                                .load(R.drawable.testpic)
                        )
                        .into(timelineImage);
            } catch (Exception e) {
                timelineImage.setImageResource(R.drawable.testpic);
                Log.e("TAG", "IMG PULL FAILED", e);
            }
        }
    }


    private void pullSubmissions() { //for series
        ArrayList<String> ppChallenges = PP.getChallengesParticipating();
        ArrayList<Challenge> mFullListTemp = mFullChallengesList;
        Log.d("TAG","PPCHALLENGES SIZE IS " + ppChallenges.size());
        for (Iterator<Challenge> iter = mFullListTemp.iterator(); iter.hasNext();) {
            Challenge ch = iter.next();
            switch (ch.getChType()) {
                case "Series":
                    int ppIndex = ppChallenges.indexOf(ch.getChID());
                    if (ppIndex < 0) {iter.remove();continue;}
                    Log.d("TAG","PPCHALLENGES GETISACTIVE SIZE IS " + PP.getIsActive().size());
                    if (!PP.getIsActive().get(ppIndex) || !PP.getChallengeActive().get(ppIndex)) {
                        iter.remove();continue;}
                    break;
                case "DTIYS":
                    iter.remove();
                    break;
            }
        } //at this point mFullListTemp contains only active series we are doing.


        mAdapterTimeline = new recyclerAdapter_timeline(ctx, 10, dateArray, mFullListTemp);
        mRecyclerView.setAdapter(mAdapterTimeline);
        final CustomLinearLayoutManagerSlow CLLM = new CustomLinearLayoutManagerSlow(ctx);
        final LinearLayoutManager LLM = new LinearLayoutManager(ctx);
        mRecyclerView.setHorizontalFadingEdgeEnabled(true);
        mRecyclerView.setFadingEdgeLength(60);
        mRecyclerView.setLayoutManager(CLLM);

        final int backgroundColor= R.color.colorGreen;

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int center =  mRecyclerView.getHeight() / 2;
                View centerView = mRecyclerView.findChildViewUnder(mRecyclerView.getLeft(), center);
                int centerPos = mRecyclerView.getChildAdapterPosition(centerView);

                if (prevCenterPos != centerPos) {
                    // dehighlight the previously highlighted view
                    View prevView = mRecyclerView.getLayoutManager().findViewByPosition(prevCenterPos);
                    if (prevView != null) {
                        prevView.findViewById(R.id.dateNumber).setBackgroundResource(0);
                        prevView.findViewById(R.id.testRotate).setBackgroundResource(0);
                    }

                    // highlight view in the middle
                    if (centerView != null) {
                        TextView dateNumTest = centerView.findViewById(R.id.dateNumber);
                        TextView monthNameTest = centerView.findViewById(R.id.monthName);
                        TextView yearNumTest = centerView.findViewById(R.id.yearTemp);
                        TextView seriesListTest = centerView.findViewById(R.id.testRotate);

                        dateNumTest.setBackgroundResource(backgroundColor);
                        centerView.findViewById(R.id.testRotate).setBackgroundResource(backgroundColor);

                        String seriesListString = seriesListTest.getText().toString();
                        String num = dateNumTest.getText().toString();
                        String monthName = monthNameTest.getText().toString();
                        String yearNum = yearNumTest.getText().toString();

                        //String num = mAdapterTimeline.getDayNumber(centerPos);
                        //String monthName =mAdapterTimeline.getMonthName(centerPos);
                        //String yearNum = mAdapterTimeline.getYearNumber(centerPos);


                        dateNumSelected.setText(num);
                        monthNameSelected.setText(monthName);
                        timeTitle.setText(monthName + " " + num + " " + yearNum);
                        seriesList.setText(seriesListString);
                    }

                    prevCenterPos = centerPos;
                }

                /*int firstVisibleItemPosition = CLLM.findFirstVisibleItemPosition();
                           int visibleItemCount = CLLM.getChildCount();
                            final int lastItem = firstVisibleItemPosition + visibleItemCount;
                            currentSelected = (firstVisibleItemPosition + lastItem)  / 2;

                            RecyclerView.ViewHolder vH = recyclerView.findViewHolderForAdapterPosition(lastSelected);
                            RecyclerView.ViewHolder vhCurrent = recyclerView.findViewHolderForAdapterPosition(currentSelected);

                            if (vH != null && vhCurrent != null) {
                                vH.itemView.findViewById(R.id.dateNumber).setBackgroundResource(0);
                                vhCurrent.itemView.findViewById(R.id.dateNumber)
                                        .setBackgroundColor(ContextCompat.getColor(ctx, backgroundColor));
                                lastSelected = currentSelected;
                            }*/
                        }
                    });

    }

    public static class NegativeTopItemDecorator extends RecyclerView.ItemDecoration {
        private final int mTopMargin;

        public NegativeTopItemDecorator(int topMargin) {
            this.mTopMargin = topMargin;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            if (position != 0) {
                outRect.left = mTopMargin;
            }
        }
    }


}