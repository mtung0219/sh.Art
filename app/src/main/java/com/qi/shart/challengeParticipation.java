package com.qi.shart;

import java.util.ArrayList;

public class challengeParticipation {
    private int currParticipants;
    private ArrayList<String> activeParticipants;
    private ArrayList<Boolean> participantActive;

    public challengeParticipation(int currParticipants, ArrayList<String> activeParticipants,ArrayList<Boolean> participantActive) {
        this.currParticipants = currParticipants;
        this.activeParticipants = activeParticipants;
        this.participantActive = participantActive;
    }

    public challengeParticipation() {
    }

    public int getCurrParticipants() {
        return currParticipants;
    }

    public ArrayList<String> getActiveParticipants() {
        return activeParticipants;
    }
    public ArrayList<Boolean> getParticipantActive() {
        return participantActive;
    }

    public void incrementCurrParticipants() {
        currParticipants += 1;
    }
    public void decrementCurrParticipants() {
        currParticipants -= 1;
    }
    public void setInactive(int index) {
        participantActive.set(index, false);
    }

}
