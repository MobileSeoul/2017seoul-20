package com.missionofseoul.seoul.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by hyunho on 2016-10-25.
 */

public class PageAdapter extends FragmentPagerAdapter {

    Fragment[] fragments = new Fragment[3];
    String[] pageTitles = {"서울의미션", "서울문화정보", "서울여행톡"};//5

    public PageAdapter(FragmentManager fm) {
        super(fm);

        fragments[0] = new MissionPageFrag1();
        fragments[1] = new MissionPageFrag2();
      // fragments[2] = new MissionPageFrag3();
        fragments[2] = new MissionPageFrag4();
      //  fragments[4] = new MissionPageFrag5();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

    //뷰페이져와 연결된 탭레이아웃의 탭제목을 설정
    @Override
    public CharSequence getPageTitle(int position) {
        return pageTitles[position];
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
