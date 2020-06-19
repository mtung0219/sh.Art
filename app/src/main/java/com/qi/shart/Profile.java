package com.qi.shart;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Profile implements Parcelable {

    private String username;
    private String desc;
    private String provID;
    private @ServerTimestamp Date timeStamp;
    private String instagram;
    private String deviantArt;
    private String twitter;
    private Long profPicCache;
    private String realName;
    private String usernameLower;

    public Profile(String username, String desc, Date ts, String provID, String instagram, String deviantArt, String twitter,
                   long profPicCache, String realName, String usernameLower) {
        this.username = username;
        this.desc = desc;
        this.timeStamp = ts;
        this.provID = provID;
        this.instagram = instagram;
        this.deviantArt = deviantArt;
        this.twitter = twitter;
        this.profPicCache = profPicCache;
        this.realName = realName;
        this.usernameLower = usernameLower;
    }

    public Profile() {
    }

    protected Profile(Parcel in) {
        username = in.readString();
        desc = in.readString();
        provID = in.readString();
        instagram = in.readString();
        deviantArt = in.readString();
        twitter = in.readString();
        if (in.readByte() == 0) {
            profPicCache = null;
        } else {
            profPicCache = in.readLong();
        }
    }

    public static final Creator<Profile> CREATOR = new Creator<Profile>() {
        @Override
        public Profile createFromParcel(Parcel in) {
            return new Profile(in);
        }

        @Override
        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };

    public String getUsername() {
        return username;
    }
    public String getDesc() {
        return desc;
    }
    public Date getTimeStamp() {
        return timeStamp;
    }
    public String getProvID() {
        return provID;
    }
    public String getInstagram() {
        return instagram;
    }
    public String getDeviantArt() {
        return deviantArt;
    }
    public String getTwitter() {
        return twitter;
    }
    public String getRealName() {
        return realName;
    }
    public long getProfPicCache() {
        return profPicCache;
    }
    private String getUsernameLower() {return usernameLower;}

    public void updateProfPicCache() {
        profPicCache += 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(username);
        parcel.writeString(desc);
        parcel.writeString(provID);
        parcel.writeString(instagram);
        parcel.writeString(deviantArt);
        parcel.writeString(twitter);
        if (profPicCache == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(profPicCache);
        }
    }
}
