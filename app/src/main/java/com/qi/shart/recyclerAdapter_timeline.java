package com.qi.shart;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

public class recyclerAdapter_timeline extends RecyclerView.Adapter<recyclerAdapter_timeline.ViewHolder> {
    private FirebaseStorage firestoreStorage = FirebaseStorage.getInstance();
    private LayoutInflater mInflater;
    private ImageView imageView;
    private TextView catItemView, titleItemView, usernameItemView;
    private Context cxt;
    private int widthUse, num;
    private TextView testRotate, testRotateAll;
    private int selectedItem = 0;
    private int lastSelected = 0;
    private int selectedPos = RecyclerView.NO_POSITION;
    private ArrayList<Date> dateArray;
    private ArrayList<Challenge> mSeriesChallenges;
    private Format dayFormat, monthFormat,yearFormat;
    private Date lastDate;
    private ArrayList<ArrayList<Challenge>> challengeListDateSpread;
    private ArrayList<ArrayList<String>> challengeListDateSpreadName;

    public recyclerAdapter_timeline(Context context, int num, ArrayList<Date> dateArray, ArrayList<Challenge> mSeriesChallenges,
        ArrayList<ArrayList<Challenge>> challengeListDateSpread, ArrayList<ArrayList<String>> challengeListDateSpreadName) {
        mInflater = LayoutInflater.from(context);
        this.cxt = context;
        this.num = num;
        this.dateArray = dateArray;
        this.mSeriesChallenges = mSeriesChallenges;
        this.dayFormat = new SimpleDateFormat("dd");
        this.monthFormat = new SimpleDateFormat("MMMM");
        this.yearFormat = new SimpleDateFormat("yyyy");
        this.lastDate = dateArray.get(dateArray.size()-1);
        this.challengeListDateSpread = challengeListDateSpread;
        this.challengeListDateSpreadName = challengeListDateSpreadName;

    }

    public void fillChallengeDateSpread() {
        challengeListDateSpread = new ArrayList<>(dateArray.size());
        challengeListDateSpreadName = new ArrayList<>(dateArray.size());

        //initialize challengeListDateSpread;
        for (int i = 0;i < dateArray.size();i++) {
            challengeListDateSpread.add(new ArrayList<Challenge>());
            challengeListDateSpreadName.add(new ArrayList<String>());
        }

        Calendar c = Calendar.getInstance();
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c.setTime(new Date());
        for (int i = 0; i < dateArray.size(); i++) {
            Date a = dateArray.get(i);
            c1.setTime(a);
            for (int j = 0; j < mSeriesChallenges.size(); j++) {
                Challenge ch = mSeriesChallenges.get(j);
                c2.setTime(ch.getStartDate());
                boolean sameDay = c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR) &&
                        c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR);
                if (sameDay) {
                    int numEntries = ch.getnumEntries();
                    int counter = 0;
                    for (int iCopy = i; iCopy < Math.min(i + numEntries, dateArray.size()); iCopy++) {
                        challengeListDateSpread.get(iCopy).add(ch);
                        String dayLabel = " Day " + counter;
                        challengeListDateSpreadName.get(iCopy).add(dayLabel);
                        //adding day1 day2 .. day 30 etc.
                        counter+=1;
                    }
                    //challengeListDateSpread.get(i).add(ch);
                }
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imgView;
        final recyclerAdapter_timeline mAdapter;
        private TextView dateNumber, monthName,yearNumber;

        private final Context context;
        private ViewHolder(@NonNull View itemView, recyclerAdapter_timeline adapter) {
            super(itemView);
            this.mAdapter = adapter;
            testRotate = itemView.findViewById(R.id.testRotate);
            testRotateAll = itemView.findViewById(R.id.testRotateAll);
            context = itemView.getContext();
            dateNumber = itemView.findViewById(R.id.dateNumber);
            monthName = itemView.findViewById(R.id.monthName);
            yearNumber = itemView.findViewById(R.id.yearTemp);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //notifyItemChanged(selectedPos);
            //selectedPos = getLayoutPosition();
            //notifyItemChanged(selectedPos);
        }
    }

    public String getDayNumber(int position) {
        Date pos = dateArray.get(position);
        return dayFormat.format(pos);
    }
    public String getMonthName(int position) {
        Date pos = dateArray.get(position);
        return monthFormat.format(pos);
    }
    public String getYearNumber(int position) {
        Date pos = dateArray.get(position);
        return yearFormat.format(pos);
    }


    @NonNull
    @Override
    public recyclerAdapter_timeline.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("TIMELINE","inflating viewholder");
        View mItemView = mInflater.inflate(R.layout.recycler_item_timeline, parent, false);
        return new ViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull final recyclerAdapter_timeline.ViewHolder holder, final int position) {
        //holder.setIsRecyclable(false);
        TextView animationTarget = testRotate;

        Date pos = dateArray.get(position);
        holder.dateNumber.setText(dayFormat.format(pos));
        holder.monthName.setText(monthFormat.format(pos));
        holder.yearNumber.setText(yearFormat.format(pos));


        ArrayList<Challenge> a = challengeListDateSpread.get(position);
        if (!a.isEmpty()) {
            ArrayList<String> a1 = challengeListDateSpreadName.get(position);
            String toSetText = "";
            String toSetTextShort = "";
            if (a.size() == 1) {
                toSetTextShort = toSetTextShort + a.get(0).getTitle() + a1.get(0);
            } else {
                toSetTextShort = toSetTextShort + a.get(0).getTitle() + a1.get(0) + " and " + (a.size() - 1) + " others";
            }
            for (int i = 0; i < a.size(); i++) {
                Challenge ch = a.get(i);
                String dayNum = a1.get(i);
                toSetText = toSetText + ch.getTitle() + dayNum + "\n";
            }

            testRotate.setText(toSetTextShort);
            testRotateAll.setText(toSetText);
        } else {
            testRotate.setText("");
            testRotateAll.setText("Nothing for today!");
        }

    }

    @Override
    public void onViewAttachedToWindow(recyclerAdapter_timeline.ViewHolder holder) {
    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return 101;
    }
}
