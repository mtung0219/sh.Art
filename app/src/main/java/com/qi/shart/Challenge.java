package com.qi.shart;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Challenge {

    private DatabaseReference mDatabase;
    public String title;
    public int authorId;
    public String platform;
    public String hashtag;
    public int numEntries;
    public String postFreq;
    public String desc;
    public String challengeType;
    public String startDate;
    public String numDays;
    public @ServerTimestamp Date timeStamp;

    public Challenge() {
    }

    public Challenge(int authorId, String title, String platform, String hashtag, int numEntries, String postFreq, String startDate, String desc, Date ts) {
        this.title = title;
        this.authorId = authorId;
        this.desc = desc;
        this.startDate = startDate;
        this.hashtag = hashtag;
        this.postFreq = postFreq;
        this.platform = platform;
        this.numEntries = numEntries;
        this.timeStamp = ts;
    }

    //mDatabase = FirebaseDatabase.getInstance().getReference();
}
