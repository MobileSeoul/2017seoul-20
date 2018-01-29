package com.missionofseoul.seoul;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.missionofseoul.seoul.fragment.DetailPageAdapter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import java.net.URL;
import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {

    ViewFlipper flipper;

    TabLayout tabLayout;
    ViewPager viewPager;
    public DetailPageAdapter detailPageAdapter;

    AppBarLayout appBarLayout;
    CollapsingToolbarLayout collaps;

    //커스텀액션바 지도맵
    TextView map_text;
    Button back_btn;

    ImgThread imgThread;

    String id; //sharedPreference에서 id를 가져와 저정 할 변수
    public int tableNum; //getLayoutPosition에서 위치값을 가져온 것(미션테이블의 번호)
    int column; //review컬럼에 붙을 숫자

    //서버에있는 xml에서 탭 타이틀 가져오기
    ArrayList<String> titles = new ArrayList<>();
    String name;

    //서버에있는 location_load.xml에서 위치정보가져오기
    ArrayList<String> locations = new ArrayList<>();
    int no; //fragmentpage 위치확인(미션버튼이미지)

    //Glide API 코딩수정부분
    public RequestManager mGlideRequestManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            setTheme(R.style.NoActionCustumTheme);
        }

        setContentView(R.layout.activity_detail);

        mGlideRequestManager = Glide.with(this);

        //로그인 회원인지 알아내기
        if (!G.mem_id.equals("empty")) {
            id = G.mem_id;
        } else {
            id = "admin"; //비회원이라면 아이디 default값으로 이미지 가져오기!!!
        }

        Intent intent = getIntent();
        tableNum = intent.getIntExtra("MissionPosition", 0);

        flipper = (ViewFlipper) findViewById(R.id.flipper);
        tabLayout = (TabLayout) findViewById(R.id.layout_detailtab);
        viewPager = (ViewPager) findViewById(R.id.viewpager);


        appBarLayout = (AppBarLayout) findViewById(R.id.layout_appbar);
        collaps = (CollapsingToolbarLayout) findViewById(R.id.layout_collaps);

        //툴바를 액션바로 사용하기
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);

        //액션바 커스텀하기
        View mCustomView = LayoutInflater.from(this).inflate(R.layout.detail_abs_layout, null);
        actionBar.setCustomView(mCustomView);
        //액션바에 있는 뒤로가기버튼 지도글씨 객체생성
        map_text = (TextView) mCustomView.findViewById(R.id.map_text);
        back_btn = (Button) mCustomView.findViewById(R.id.back_btn);

        //detailpage header이미지를 가져오기 위한 쓰레드 실행
        imgThread = new ImgThread();
        imgThread.start();

        //서버에서 해당번째 미션의 상세페이지에 들어갈 탭이름을 가져온다.!!(경복궁, 경희궁,..)
        // xml 파싱을 해서 사용
        new Thread() {
            @Override
            public void run() {
                String address = G.domain + "xml/title_load.xml";
                String title = "mission" + tableNum;
                String tmp;

                //InputStream을 열어주는 객체 생성
                URL url = null;
                try {
                    url = new URL(address);
                    InputStream is = url.openStream();
                    InputStreamReader isr = new InputStreamReader(is, "utf-8");

                    //InputStreamReader가 읽어드리는 byte값을 xml문서로 읽어들이는 객체 생성
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    XmlPullParser xpp = factory.newPullParser();
                    xpp.setInput(isr);

                    xpp.next();
                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        switch (eventType) {
                            case XmlPullParser.START_DOCUMENT:
                                break;
                            case XmlPullParser.START_TAG:
                                name = xpp.getName();
                                if (name.equals(title)) {
                                    xpp.next();
                                    tmp = xpp.getText();
                                    String[] tmpArr = tmp.split(";");
                                    for (int i = 0; i < tmpArr.length; i++) {
                                        titles.add(tmpArr[i]);
                                    }
                                }
                                break;
                            case XmlPullParser.TEXT:
                                break;
                            case XmlPullParser.END_TAG:
                                break;
                            case XmlPullParser.END_DOCUMENT:
                                break;
                        }//end of switch;
                        eventType = xpp.next();
                    }//end of while

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            detailPageAdapter = new DetailPageAdapter(getSupportFragmentManager(),
                                    titles.get(0).trim(), titles.get(1).trim(), titles.get(2).trim(), titles.get(3).trim(), titles.get(4).trim());
                            viewPager.setAdapter(detailPageAdapter);
                            //뷰페이져와 탭레이아웃을 같이 작업하도록 설정
                            tabLayout.setupWithViewPager(viewPager);

                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.start();

        //appbar가 뿌셔졌을 때 설정
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                if (collaps.getHeight() + verticalOffset < 2 * ViewCompat.getMinimumHeight(collaps)) {
                    back_btn.setBackgroundResource(R.drawable.back_btn333);
                    map_text.setTextColor(0xff18FFFF); //지도글씨색 포인트색

                } else {
                    back_btn.setBackgroundResource(R.drawable.back_btn_white);
                    map_text.setTextColor(0xffffffff);//지도글씨색 하얀색
                }
            }
        });


        //ActionBar에 있는 지도글씨 참조
        map_text.setOnClickListener(new View.OnClickListener() { //지도글씨 클릭하면 서버에서 위치가져오기
            @Override
            public void onClick(View v) {

                no = viewPager.getCurrentItem(); //현재보여지는 fragment의 값을 알아낸다.

                //서버에서 xml파일을 가져와 파싱을해서 미션의 위도경도 가져오기
                new Thread() {
                    @Override
                    public void run() {
                        String address = G.domain + "xml/location_load.xml";
                        final String mission = "mission" + tableNum;//미션 리스트 중 몇 번째 미션인지 확인
                        String missionTag = "";//파싱하면서 태그이름 저장할 변수
                        String tmp = ""; //임시저장변수

                        //InputStream을 열어주는 객체 생성
                        URL url = null;
                        try {
                            url = new URL(address);
                            InputStream is = url.openStream();
                            InputStreamReader isr = new InputStreamReader(is, "utf-8");

                            //InputStreamReader가 읽어드리는 byte값을 xml문서로 읽어들이는 객체 생성
                            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                            XmlPullParser xpp = factory.newPullParser();
                            xpp.setInput(isr);

                            xpp.next();
                            int eventType = xpp.getEventType();
                            while (eventType != XmlPullParser.END_DOCUMENT) {
                                switch (eventType) {
                                    case XmlPullParser.START_DOCUMENT:
                                        break;
                                    case XmlPullParser.START_TAG:
                                        missionTag = xpp.getName();
                                        if (missionTag.equals(mission)) {
                                            xpp.next();
                                            tmp = xpp.getText();
                                            String[] tmpArr = tmp.split(";");
                                            for (int i = 0; i < tmpArr.length; i++) {
                                                locations.add(tmpArr[i]);
                                            }
                                        }
                                        break;
                                    case XmlPullParser.TEXT:
                                        break;
                                    case XmlPullParser.END_TAG:
                                        break;
                                    case XmlPullParser.END_DOCUMENT:
                                        break;
                                }//end of switch;
                                eventType = xpp.next();
                            }//end of while

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String label = "미션위치";
                                    String uriBegin = "geo:" + locations.get(no).trim();
                                    String query = locations.get(no).trim() + "(" + label + ")";
                                    String encodedQuery = Uri.encode(query);
                                    String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
                                    Uri uri = Uri.parse(uriString);
                                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                                    intent.setPackage("com.google.android.apps.maps");
                                    if (intent.resolveActivity(getPackageManager()) != null) {
                                        startActivity(intent);
                                    }

                                }
                            });

                        } catch (Exception e) {

                        }
                    }

                }.start();
            }
        });

        //ViewPager가 처음에 미리 로드할수 있는 페이지개수 정하기 0부터시작이라서 4면 5개이다.
        viewPager.setOffscreenPageLimit(4);

    }// ****************************   end of onCreate(); ***************************///


    //17/03/07 fragment 새로고침
    public void loadFragment() {
        detailPageAdapter.notifyDataSetChanged();
    }

    //actionbar에 있는 뒤로가기 버튼
    public void backBtn(View v) {
        finish();
    }

    class ImgThread extends Thread {
        @Override
        public void run() {

            //getLayoutPosition값에 +1을 더해서 전역번수에 대입한다.(미션테이블번호는1번부터)
            String serverUrl = G.domain + "php/detail_header_img.php";

            try {
                URL url = new URL(serverUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setUseCaches(false);

                String data = "id=" + id + "&tableNum=" + tableNum;
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

                //읽어온 데이터 문자열에서 db의 row별로 배열로 구분해서 저장
                final String[] rows = buffer.toString().split("&");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (String row : rows) {
                            ImageView img = new ImageView(DetailActivity.this);
                            img.setScaleType(ImageView.ScaleType.CENTER_CROP);//ViewFlipper에 들어갈 이미지 사이즈 조절
                            mGlideRequestManager.load(G.domain + row).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(img);
                            flipper.addView(img);
                        }
                        flipper.setFlipInterval(1500);
                        flipper.startFlipping();
                    }
                });
                is.close();
            } catch (Exception e) {

            }
        }//end of run()
    }//end of ImgThread


}//end of DetailClass
