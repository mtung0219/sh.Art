package com.qi.shart;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Challenge implements Parcelable {

    private String title;
    private String authorId;
    private String platform;
    private String hashtag;
    private int numEntries;
    //private String postFreq;
    private String desc;
    private @ServerTimestamp Date startDate, endDate;
    private String UID;
    private boolean hasPic;
    public @ServerTimestamp Date timeStamp;
    private long activeParticipants;
    private long numSubmissions;
    private String chID, chType;
    private long startDateLong, endDateLong;

    public Challenge() {
    }

    public Challenge(String authorId, String title, String platform, String hashtag, int numEntries,
                     Date startDate,Date endDate, String desc, Date ts, boolean hasPic, String UID, Long activeParticipants,
                     Long numSubmissions, String chID, String chType) {
        this.title = title;
        this.authorId = authorId;
        this.desc = desc;
        this.startDate = startDate;
        this.endDate = endDate;
        this.hashtag = hashtag;
        //this.postFreq = postFreq;
        this.platform = platform;
        this.numEntries = numEntries;
        this.timeStamp = ts;
        this.hasPic = hasPic;
        this.UID = UID;
        this.chID = chID;
        this.activeParticipants = activeParticipants;
        this.numSubmissions=numSubmissions;
        this.chID = chID;
        this.chType = chType;
        //setStartDateLong();
    }

    protected Challenge(Parcel in) {
        title = in.readString();
        authorId = in.readString();
        platform = in.readString();
        hashtag = in.readString();
        numEntries = in.readInt();
        desc = in.readString();
        UID = in.readString();
        hasPic = in.readByte() != 0;
        activeParticipants = in.readLong();
        numSubmissions = in.readLong();
        chID = in.readString();
        chType = in.readString();
        startDate = new Date(in.readLong());
    }

    public static final Creator<Challenge> CREATOR = new Creator<Challenge>() {
        @Override
        public Challenge createFromParcel(Parcel in) {
            return new Challenge(in);
        }

        @Override
        public Challenge[] newArray(int size) {
            return new Challenge[size];
        }
    };

    public void setStartDateLong() {
        startDateLong = startDate.getTime();
    }
    public boolean getHasPic() {
        return hasPic;
    }
    public String getTitle() {
        return title;
    }
    public String getPlatform() {
        return platform;
    }
    public String getHashtag() {
        return hashtag;
    }
    /*public String getPostFreq() {
        return postFreq;
    }*/
    public Date getStartDate() {
        return startDate;
    }
    public Date getEndDate() {
        return endDate;
    }
    public String getAuthorId() {
        return authorId;
    }
    public int getnumEntries() {
        return numEntries;
    }
    public String getDesc() {
        return desc;
    }
    public String getChID() {
        return chID;
    }
    public String getChType() {
        return chType;
    }
    public String getUID() { return UID; }
    public Date getTS() {
        return timeStamp;
    }
    public Long getActiveParticipants() { return activeParticipants; }
    public Long getNumSubmissions() { return numSubmissions; }
    public Date getStartDateFromLong() { return new Date(startDateLong);} //for parcelable

    public void setChID(String chID) {
        this.chID = chID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(authorId);
        parcel.writeString(platform);
        parcel.writeString(hashtag);
        parcel.writeInt(numEntries);
        parcel.writeString(desc);
        parcel.writeString(UID);
        parcel.writeByte((byte) (hasPic ? 1 : 0));
        parcel.writeLong(activeParticipants);
        parcel.writeLong(numSubmissions);
        parcel.writeString(chID);
        parcel.writeString(chType);
        parcel.writeLong(startDate.getTime());
    }
}
