package com.missionofseoul.seoul.intro;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.missionofseoul.seoul.G;
import com.missionofseoul.seoul.MainActivity;
import com.missionofseoul.seoul.R;

import java.util.Random;

public class IntroActivity extends AppCompatActivity {

    public static Activity introctivity;

    RelativeLayout activity_intro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        introctivity = IntroActivity.this;

        //서버에서 새로생긴 notice값을 비교할 쉐어드프리퍼런스 //17/02/22 추가
        SharedPreferences pre = getSharedPreferences("NewNotice", MODE_PRIVATE);
        int newNotice = pre.getInt("NoticeNum", 0);

        if (newNotice == 0) { // 처음 쉐어드프리퍼런스를 시작
            SharedPreferences preferences = getSharedPreferences("NewNotice", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("NoticeNum", 1);
            editor.apply();
        }

        SharedPreferences preferences = getSharedPreferences("AutoLogin", MODE_PRIVATE);
        String mem_id = preferences.getString("Mem_id", "empty");
        if (!mem_id.equals("empty")) { //로그인 했다면
            //글로벌 전역변수에 저장
            G.isLogin = true;
            G.mem_id = mem_id;

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

        setContentView(R.layout.activity_intro);

        activity_intro = (RelativeLayout) findViewById(R.id.activity_intro);
        Random rnd = new Random();

        //인트로백를 생성
        int n = rnd.nextInt(5); //0~4

        switch (n) {
            case 0:
                activity_intro.setBackgroundResource(R.drawable.intro_bg1);
                break;
            case 1:
                activity_intro.setBackgroundResource(R.drawable.intro_bg2);
                break;
            case 2:
                activity_intro.setBackgroundResource(R.drawable.intro_bg3);
                break;
            case 3:
                activity_intro.setBackgroundResource(R.drawable.intro_bg4);
                break;
            case 4:
                activity_intro.setBackgroundResource(R.drawable.intro_bg5);
                break;

        }

    }//end of onCreate()

    //x버튼 영역
    public void closeBtn(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//activity삭제
        startActivity(intent);
        finish();
    }

    //로그인 버튼 눌렀을 때 발동
    public void loginBtn(View v) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);//애니메이션 위에서 아래로

    }//end of loginBtn()

    //회원가입 버튼을 눌렀을 때 발동
    public void joinBtn(View v) {
        Intent intent = new Intent(this, MemberJoinActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//메모리 관리를 위해 백스텍에 빠진 Activity를 화면위로 올린다.
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);

    }//end of joinBtn()

    private long backKeyPressedTime = 0;
    private Toast toast;

    public void myOnBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
            toast.cancel();
        }
    }

    public void showGuide() {
        toast = Toast.makeText(this,
                "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
        toast.show();
    }

    //뒤로가기버튼
    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        myOnBackPressed();

    }//end of onBackPressed()


}
