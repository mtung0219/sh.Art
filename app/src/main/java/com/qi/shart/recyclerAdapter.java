package com.qi.shart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.LinkedList;

public class recyclerAdapter extends RecyclerView.Adapter<recyclerAdapter.WordViewHolder> {
    private final LinkedList<String> mWordList;
    private final LinkedList<String> mArtistList;
    private final LinkedList<String> mCategoryList;
    //private final LinkedList<String> mDateList;
    private LayoutInflater mInflater;

    public recyclerAdapter(Context context, LinkedList<String> wordList, LinkedList<String> artistList,LinkedList<String> catList) {
        mInflater = LayoutInflater.from(context);
        this.mWordList = wordList;
        this.mArtistList = artistList;
        this.mCategoryList = catList;
        //this.mDateList = dateList;
    }

    class WordViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView wordItemView;
        public final TextView artistItemView;
        public final TextView catItemView;
        //public final TextView dateItemView;
        final recyclerAdapter mAdapter;
        public WordViewHolder(@NonNull View itemView, recyclerAdapter adapter) {
            super(itemView);
            wordItemView = itemView.findViewById(R.id.textView);
            artistItemView = itemView.findViewById(R.id.textView1);
            catItemView = itemView.findViewById(R.id.textView2);
            //dateItemView = itemView.findViewById(R.id.textView3);
            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Get the position of the item that was clicked.
            int mPosition = getLayoutPosition();
            // Use that to access the affected item in mWordList.
            String element = mWordList.get(mPosition);
            // Change the word in the mWordList.
            mWordList.set(mPosition, "Clicked " + element);
            // Notify the adapter, that the data has changed so it can update the RecyclerView to display the data.
            mAdapter.notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public recyclerAdapter.WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.recyler_item, parent, false);
        return new WordViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull recyclerAdapter.WordViewHolder holder, int position) {
        String mCurrent = mWordList.get(position);
        String mCurrentArtist = mArtistList.get(position);
        String mCurrentCat = mCategoryList.get(position);
        //String mCurrentDate = mDateList.get(position);

        holder.wordItemView.setText(mCurrent);
        holder.artistItemView.setText(mCurrentArtist);
        holder.catItemView.setText(mCurrentCat);
        //holder.dateItemView.setText(mCurrentDate);

    }

    @Override
    public int getItemCount() {
        return mWordList.size();
    }
}
