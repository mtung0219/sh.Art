package com.qi.shart;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class profileParticipation implements Parcelable {
    private ArrayList<String> challengesParticipating;
    private ArrayList<Boolean> hasPic;
    private ArrayList<String> title;
    private ArrayList<Boolean> isDone;
    private ArrayList<Boolean> challengeActive;
    private ArrayList<Boolean> isActive;

    public profileParticipation(ArrayList<String> challengesParticipating,ArrayList<Boolean> hasPic,ArrayList<Boolean> isDone,
    ArrayList<String> title, ArrayList<Boolean> challengeActive, ArrayList<Boolean> isActive) {
        this.challengesParticipating = challengesParticipating;
        this.hasPic = hasPic;
        this.title = title;
        this.isDone = isDone;
        this.challengeActive = challengeActive;
        this.isActive = isActive;
    }
    public profileParticipation() {

    }
    public ArrayList<String> getChallengesParticipating() {
        return challengesParticipating;
    }
    public ArrayList<Boolean> getHasPic() {
        return hasPic;
    }
    public ArrayList<Boolean> getIsDone() {
        return isDone;
    }
    public ArrayList<String> getTitle() {
        return title;
    }
    public ArrayList<Boolean> getChallengeActive() {
        return challengeActive;
    }
    public ArrayList<Boolean> getIsActive() {
        return isActive;
    }
    protected profileParticipation(Parcel in) {
        challengesParticipating = in.createStringArrayList();
        title = in.createStringArrayList();
    }

    public static final Creator<profileParticipation> CREATOR = new Creator<profileParticipation>() {
        @Override
        public profileParticipation createFromParcel(Parcel in) {
            return new profileParticipation(in);
        }

        @Override
        public profileParticipation[] newArray(int size) {
            return new profileParticipation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringList(challengesParticipating);
        parcel.writeStringList(title);
    }
}

