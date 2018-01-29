package com.missionofseoul.seoul.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by hyunho on 2016-11-21.
 */

public class DetailPageAdapter extends FragmentPagerAdapter {
    Fragment[] fragments = new Fragment[5];
    String[] pageTitles = new String[5];


    public DetailPageAdapter(FragmentManager fm, String title1, String title2, String title3, String title4, String title5) {
        super(fm);

        fragments[0] = new DetailFrag1();
        fragments[1] = new DetailFrag2();
        fragments[2] = new DetailFrag3();
        fragments[3] = new DetailFrag4();
        fragments[4] = new DetailFrag5();

        pageTitles[0] = title1;
        pageTitles[1] = title2;
        pageTitles[2] = title3;
        pageTitles[3] = title4;
        pageTitles[4] = title5;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return pageTitles[position];
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    ;
}
