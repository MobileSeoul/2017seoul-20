package com.missionofseoul.seoul;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.missionofseoul.seoul.fragment.PageAdapter;
import com.missionofseoul.seoul.intro.IntroActivity;
import com.missionofseoul.seoul.intro.LoginActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity {

    TabLayout topTabLayout;

    ViewPager viewPager;
    PageAdapter pageAdapter;

    ImageView home_icon;
    ImageView search_icon;
    ImageView user_icon;
    ImageView thebogi_icon;

    ProgressDialog dialog;

    public static final int REQUEST_CAMERA = 1;

    private BackPressCloseHandler backPressCloseHandler;


    public RequestManager mGlideRequestManager;

    public static MainActivity mainActivity;

    String name;
    String tmp;

    //SwipeRefreshLayout mSwipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            setTheme(R.style.NoActionCustumTheme);
        }
        setContentView(R.layout.activity_main);

        mainActivity = MainActivity.this;

        mGlideRequestManager = Glide.with(this);

        IntroActivity intro = (IntroActivity) IntroActivity.introctivity;
        intro.finish();


        topTabLayout = (TabLayout) findViewById(R.id.layout_toptab);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        pageAdapter = new PageAdapter(getSupportFragmentManager());

        viewPager.setAdapter(pageAdapter);


        topTabLayout.setupWithViewPager(viewPager);

        home_icon = (ImageView) findViewById(R.id.home_icon);
        search_icon = (ImageView) findViewById(R.id.search_icon);
        user_icon = (ImageView) findViewById(R.id.user_icon);
        thebogi_icon = (ImageView) findViewById(R.id.thebogi_icon);


        backPressCloseHandler = new BackPressCloseHandler(this);

        home_icon.setImageResource(R.drawable.home_icon_over);



        if (G.isLogin == true && G.isConfirm == false) {
            new Thread() {
                @Override
                public void run() {
                    String serverUrl = G.domain + "php/mail/load_confirm.php";

                    try {
                        URL url = new URL(serverUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setDoOutput(true);
                        connection.setDoInput(true);
                        connection.setUseCaches(false);

                        String data = "id=" + G.mem_id;
                        OutputStream os = connection.getOutputStream();
                        os.write(data.getBytes());

                        os.flush();
                        os.close();

                        //서버로부터 오는 echo를 읽어오기
                        InputStream is = connection.getInputStream();
                        InputStreamReader isr = new InputStreamReader(is);
                        BufferedReader reader = new BufferedReader(isr);

                        StringBuffer buffer = new StringBuffer();

                        String line = reader.readLine();

                        while (true) {
                            buffer.append(line);
                            line = reader.readLine();
                            if (line == null) break;
                            buffer.append("\n");
                        }
                        final String result = buffer.toString().trim();
                        if (result.equals("1")) {
                            G.isConfirm = true;
                        } else {
                            G.isConfirm = false;
                        }
                        is.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }//end of run()
            }.start();
        }//end of if





        SharedPreferences preferences = getSharedPreferences("MissionRange", MODE_PRIVATE);
        G.range = preferences.getInt("Range", 0);

    }///******************************************* end of onCreate() *****************************************///

    public void loadFragment() {
        pageAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onResume() {
        super.onResume();

    }


    //bottomnavigation click 발동메서드
    public void bottomBtn(View v) {
        switch (v.getId()) {

            case R.id.layout_home:
                break;
            case R.id.layout_search:
                Intent intent = new Intent(this, MapsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                break;
            case R.id.layout_user:
                if (G.isLogin == true) {
                    Intent intent1 = new Intent(this, UserActivity.class);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent1);
                    overridePendingTransition(0, 0);
                    // finish();
                } else {
                    new AlertDialog.Builder(this).setMessage("로그인이 필요합니다. 로그인 하시겠습니까?").setPositiveButton("로그인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(intent);
                            overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.anim_transparent);
                            //  finish();
                        }
                    }).setNegativeButton("다음에", null).create().show();

                }
                break;
            case R.id.layou_thebogi:

                Intent intent2 = new Intent(this, ThebogiActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent2);
                overridePendingTransition(0, 0);
                break;

        }
    }//end of bttomBtn

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();

    }//end of onBackPressed()


}//end of class
