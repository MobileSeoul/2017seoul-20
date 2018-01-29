package com.missionofseoul.seoul;

import android.widget.Toast;

/**
 * Created by hyunho on 2016-11-28.
 */

public class BackPressCloseHandler {

    private long backKeyPressedTime = 0;
    private Toast toast;

    private MainActivity mainActivity;

    public BackPressCloseHandler(MainActivity context) {
        this.mainActivity = context;
    }

    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            mainActivity.finish();
            toast.cancel();
        }
    }

    public void showGuide() {
        toast = Toast.makeText(mainActivity,
                "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
        toast.show();
    }
}
