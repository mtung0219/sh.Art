package com.qi.shart;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

// Since this is an object collection, use a FragmentStatePagerAdapter,
// and NOT a FragmentPagerAdapter.
public class ViewPageAdapter extends FragmentStatePagerAdapter {
    private Fragment[] childFragments;
    private boolean isUserLoggedIn;
    private int tabPosition;
    private FragmentManager fm;
    private Profile pf;
    private TabLayout tL;


    public ViewPageAdapter(@NonNull FragmentManager fm, Context ctx, TabLayout tL, String posterID,
                           profileParticipation PP, ArrayList<challengeSlotDetail> slotDetails) {
        super(fm);
        this.fm = fm;
        this.tL = tL;
        isUserLoggedIn = PreferenceData.getUserLoggedInStatus(ctx);

        if (isUserLoggedIn) {
            childFragments = new Fragment[] {
                    new mainfragment(),
                    profileMyChallenges_fragment.newInstance(PP, slotDetails),
                    profilefragment.getInstance(posterID)
            };
        } else {
            childFragments = new Fragment[] {
                    new mainfragment(),
                    new logInFragment(),
                    new logInFragment()
            };
        }

    }

    @NonNull
    @Override
    public Fragment getItem(int i) {
        /*tabPosition = tL.getSelectedTabPosition();

        int fragComingFrom;
        Fragment FragmentToReturn;
        switch (tabPosition) {
            case 1:
                fragComingFrom = R.id.discoverFragment_layout;
                break;
            case 2:
                fragComingFrom = R.id.profileFragment_layout;
                break;
            default: //case 0
                fragComingFrom = R.id.mainFragment_layout;
                break;
        }
        switch (i) {
            case 1:
                FragmentToReturn = new discoverfragment();
                break;
            case 2:
                FragmentToReturn = profilefragment.getInstance(pf);
                break;
            default: //case 0
                FragmentToReturn = new mainfragment();
                break;
        }

        /*fm.beginTransaction()
                //.replace(fragComingFrom, FragmentToReturn)
                .addToBackStack(null)
                .commit();*/

        return childFragments[i];
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        /*switch (position) {
            case 0:
                return "Challenges";
            case 1:
                return "Discover";
            case 2:
                return "My Profile";
        }*/
        return null;
    }
}