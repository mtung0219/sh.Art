package com.qi.shart;

import android.app.PictureInPictureParams;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;

// Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
public class ViewPageAdapterProfile extends FragmentStatePagerAdapter {
    private Fragment[] childFragments;

    public ViewPageAdapterProfile(@NonNull FragmentManager fm, Context ctx, profileParticipation PP,
                                  ArrayList<challengeSlotDetail> slotDetails, String posterID, String posterName) {
        super(fm);
        childFragments = new Fragment[]{
                profileMySubmissions_fragment.newInstance(PP, slotDetails, posterID, posterName),
                profileMyChallenges_fragment.newInstance(PP, slotDetails)
                //new profileMyBio_fragment()
        };
    }

    @NonNull
    @Override
    public Fragment getItem(int i) {
        return childFragments[i];
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Submissions";
            case 1:
                return "Challenge Progress";
        }
        return null;
    }
}