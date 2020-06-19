package com.qi.shart;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class recyclerAdapter_timelineDTIYS extends RecyclerView.Adapter<recyclerAdapter_timelineDTIYS.ViewHolder> {
    private FirebaseStorage firestoreStorage = FirebaseStorage.getInstance();
    private LayoutInflater mInflater;
    private ImageView imageView;
    private TextView catItemView, titleItemView, usernameItemView;
    private Context cxt;
    private int widthUse, num;
    private TextView testRotate;
    private int selectedItem = 0;
    private int lastSelected = 0;
    private int selectedPos = RecyclerView.NO_POSITION;
    private ArrayList<Challenge> mChallenges;
    private ArrayList<String> mChallengeIDs;
    private final int bufferEitherSide;

    public recyclerAdapter_timelineDTIYS(Context context, int num, ArrayList<Challenge> mChallenges,
                                         int bufferEitherSide) {
        mInflater = LayoutInflater.from(context);
        this.cxt = context;
        this.num = num;
        this.mChallenges = mChallenges;
        this.bufferEitherSide = bufferEitherSide;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imgView;
        final recyclerAdapter_timelineDTIYS mAdapter;

        private final Context context;
        private ViewHolder(@NonNull View itemView, recyclerAdapter_timelineDTIYS adapter) {
            super(itemView);
            this.mAdapter = adapter;
            testRotate = itemView.findViewById(R.id.testRotate);
            imgView = itemView.findViewById(R.id.testingPic);
            context = itemView.getContext();
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //notifyItemChanged(selectedPos);
            //selectedPos = getLayoutPosition();
            //notifyItemChanged(selectedPos);
        }
    }

    @NonNull
    @Override
    public recyclerAdapter_timelineDTIYS.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("TIMELINE","inflating viewholder");
        View mItemView = mInflater.inflate(R.layout.recycler_item_timeline, parent, false);
        return new recyclerAdapter_timelineDTIYS.ViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull final recyclerAdapter_timelineDTIYS.ViewHolder holder, final int position) {
        //holder.setIsRecyclable(false);
        TextView animationTarget = testRotate;
        if (position < bufferEitherSide || position >= (bufferEitherSide + mChallenges.size())) {
            testRotate.setText("");
            return;
        }
        int pos = position - bufferEitherSide;
        animationTarget.setText(mChallenges.get(pos).getTitle());

        //holder.itemView.setSelected(selectedPos == position);
        //holder.itemView.setBackgroundColor(selectedPos == position ? Color.GREEN : Color.BLUE);
        /*Animation animation = AnimationUtils.loadAnimation(cxt, R.anim.rotateanim);
        animation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                testRotate.setPivotX(0);
                testRotate.setPivotY(0);
                testRotate.setRotation(-45);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        animationTarget.startAnimation(animation);*/

        //testRotate.setPivotX(0);
        //testRotate.setPivotY(0);
        //testRotate.setRotation(-70);
        //testRotate.bringToFront();
    }

    @Override
    public void onViewAttachedToWindow(recyclerAdapter_timelineDTIYS.ViewHolder holder) {
    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return num;
    }
}
