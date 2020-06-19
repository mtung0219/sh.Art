package com.qi.shart;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;

public class challengeSlotDetail implements Parcelable {
    private String desc;
    private String imageURI;
    public @ServerTimestamp Date timeStamp;
    private String posterID;
    private String username;
    private int numPos;
    private int likeNum;
    private ArrayList<String> likes;
    private String flair;
    private String medium;
    private String chName;
    private Long cacheKey;
    private int numEntries;

    public challengeSlotDetail(String desc, String imageURI, String posterID, Date TS, String username,
                               int numPos, int likeNum, ArrayList<String> likes,
                               String flair, String medium, String chName, long cacheKey, int numEntries) {
        this.desc = desc;
        this.imageURI = imageURI;
        this.posterID = posterID;
        this.timeStamp = TS;
        this.username = username;
        this.numPos = numPos;
        this.likeNum = likeNum;
        this.likes = likes;
        this.flair = flair;
        this.medium = medium;
        this.chName = chName;
        this.cacheKey = cacheKey;
        this.numEntries = numEntries;
    }

    public challengeSlotDetail() {
    }

    protected challengeSlotDetail(Parcel in) {
        desc = in.readString();
        imageURI = in.readString();
        posterID = in.readString();
        username = in.readString();
        numPos = in.readInt();
        likeNum = in.readInt();
        likes = in.createStringArrayList();
        flair = in.readString();
        medium = in.readString();
        chName = in.readString();
        if (in.readByte() == 0) {
            cacheKey = null;
        } else {
            cacheKey = in.readLong();
        }
    }

    public static final Creator<challengeSlotDetail> CREATOR = new Creator<challengeSlotDetail>() {
        @Override
        public challengeSlotDetail createFromParcel(Parcel in) {
            return new challengeSlotDetail(in);
        }

        @Override
        public challengeSlotDetail[] newArray(int size) {
            return new challengeSlotDetail[size];
        }
    };

    public String getdesc() {
        return desc;
    }
    public String getposterID() {
        return posterID;
    }
    public String getimageURI() {
        return imageURI;
    }
    public Date getTS() {
        return timeStamp;
    }
    public String getUsername() {
        return username;
    }
    public int getNumPos() {
        return numPos;
    }
    public int getLikeNum() {
        return likeNum;
    }
    public ArrayList<String> getLikes() {
        return likes;
    }
    public String getFlair() {
        return flair;
    }
    public String getMedium() {
        return medium;
    }
    public String getChName() {
        return chName;
    }
    public int getNumEntries() {
        return numEntries;
    }

    public void addLikes(String posterID) {
        likes.add(posterID);
    }

    public void incrementLikeCounter() {
        likeNum += 1;
    }

    public void removeLikes(String posterID) {
        likes.remove(posterID);
    }
    public void decrementLikeCounter() {
        likeNum -=1;
    }
    public long getCacheKey() {
        return cacheKey;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(desc);
        parcel.writeString(imageURI);
        parcel.writeString(posterID);
        parcel.writeString(username);
        parcel.writeInt(numPos);
        parcel.writeInt(likeNum);
        parcel.writeStringList(likes);
        parcel.writeString(flair);
        parcel.writeString(medium);
        parcel.writeString(chName);
        if (cacheKey == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(cacheKey);
        }
    }
}

