package com.profoundtechs.campus;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by HP on 3/2/2018.
 */

class SectionsPagerAdapter extends FragmentPagerAdapter {
    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                MessagesFragment chatsFragment=new MessagesFragment();
                return chatsFragment;
            case 1:

                ActiveFragment activeFragment=new ActiveFragment();
                return activeFragment;
            case 2:

                RequestsFragment requestsFragment=new RequestsFragment();
                return requestsFragment;
            default:
                 return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position){
        switch (position){
            case 0:
                return "MESSAGES";
            case 1:
                return "ACTIVE";
            case 2:
                return "REQUESTS";
            default:
                return null;
        }
    }
}
