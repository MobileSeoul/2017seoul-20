package com.missionofseoul.seoul;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CultureDetailActivity extends AppCompatActivity {
    public RequestManager mGlideRequestManager;

    // List<CulturePojo.Row> culturePojoList;
    //CulturePojo.Row culturePojo;
    // int pos;

    //액션바 멤버변수
    TextView abs_title;
    TextView save_text;

    //
    ImageView mainImg;
    TextView title;
    TextView price;
    TextView d_day;
    TextView genre;
    TextView date;
    TextView locationText;
    ImageView locationIcon;
    TextView whoUse;
    TextView host;
    TextView inquiry;

    String mUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            setTheme(R.style.CustumTheme);
        }
        setContentView(R.layout.activity_culture_detail);

        mGlideRequestManager = Glide.with(this);

        //ActionBar 커스텀하기      //emulator api 16에서 custom error
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);

        //액션바 타이틀
        abs_title = (TextView) findViewById(R.id.abs_title);
        abs_title.setText("문화행사정보");
        save_text = (TextView) findViewById(R.id.save_text);

        // culturePojoList = (ArrayList<CulturePojo.Row>) getIntent().getSerializableExtra("CulturePojoList");
        //  pos = getIntent().getIntExtra("pos", 0);

        //   culturePojo = culturePojoList.get(pos);

        String orlink = getIntent().getStringExtra("Orglink");
        String mainImgUrl = getIntent().getStringExtra("MainImg");
        String mTitle = getIntent().getStringExtra("Title");
        String useFree = getIntent().getStringExtra("UseFree");
        String stratDate = getIntent().getStringExtra("StartDate");
        String endDate = getIntent().getStringExtra("EndDate");
        String codeName = getIntent().getStringExtra("CodeName");
        String place = getIntent().getStringExtra("Place");
        String useTrgt = getIntent().getStringExtra("UseTrgt");
        String sponser = getIntent().getStringExtra("Sponsor");
        String mInquiry = getIntent().getStringExtra("Inquiry");

        mainImg = (ImageView) findViewById(R.id.mainImg);
        title = (TextView) findViewById(R.id.title);
        price = (TextView) findViewById(R.id.price);
        d_day = (TextView) findViewById(R.id.d_day);
        genre = (TextView) findViewById(R.id.genre);
        locationText = (TextView) findViewById(R.id.locationText);
        whoUse = (TextView) findViewById(R.id.whoUse);
        host = (TextView) findViewById(R.id.host);
        inquiry = (TextView) findViewById(R.id.inquiry);
        date = (TextView) findViewById(R.id.date);

        mUrl = orlink;

        //mainImage
        if (mainImgUrl != null) {
            String fileName = mainImgUrl;
            int index = fileName.lastIndexOf("/");
            if (index != -1) {
                String imgUrl = fileName.substring(0, index);//주소
                String extension = fileName.substring(index + 1); //이미지 파일부분
                mGlideRequestManager.load(imgUrl.toLowerCase() + "/" + extension).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(mainImg);

            }
        }//end of if

        title.setText(mTitle);
        price.setText(useFree);
        //
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c1 = Calendar.getInstance();
        String strToday = sdf.format(c1.getTime());

        dDay(stratDate, endDate, strToday);

        //d_day.setText();
        genre.setText(codeName);
        locationText.setText(place);
        whoUse.setText(useTrgt);
        if (sponser.length() == 0) {
            host.setText("-");
        } else {
            host.setText(sponser);
        }
        inquiry.setText(mInquiry);
        date.setText(stratDate + " ~ " + endDate);

    }//end of onCreate()

    private void dDay(String start, String end, String today) {

        // SimpleDateFormat의 형식을 선언한다.
        SimpleDateFormat original_format = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat new_format = new SimpleDateFormat("yyyyMMdd");

        // 날짜 형식 변환시 파싱 오류를 try.. catch..로 체크한다.
        try {
            Date dStrat = original_format.parse(start);
            Date dEnd = original_format.parse(end);
            Date dToay = original_format.parse(today);

            String strStrat = new_format.format(dStrat);
            String strEnd = new_format.format(dEnd);
            String strToday = new_format.format(dToay);


            // 문자열 타입을 날짜 타입으로 변환한다.
            Date begin = new_format.parse(strStrat);
            Date todayDate = new_format.parse(strToday);
            long diff = todayDate.getTime() - begin.getTime();
            long diffDays = diff / (24 * 60 * 60 * 1000);

            if (diffDays < 0) {
                d_day.setText("D - " + (-diffDays));
            } else if (diffDays == 0) {
                d_day.setText("진행중");
            } else {
                Date lastDay = new_format.parse(strEnd);

                long diff01 = todayDate.getTime() - lastDay.getTime();
                long diffDays01 = diff01 / (24 * 60 * 60 * 1000);

                if (diffDays01 > 0) {
                    d_day.setText("종료");
                } else {
                    d_day.setText("진행중");
                }

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }//end of dDay()


    //행사정보 홈페이 이동
    public void onClickSite(View v) {

        if (mUrl != null) {
            Intent intent = new Intent(this, CultureWebViewActivity.class);
            intent.putExtra("Url", mUrl);
            startActivity(intent);

        } else {
            Toast.makeText(this, "링크가 없습니다!", Toast.LENGTH_SHORT).show();
        }

    }//end of onClickSite()


    //액션바의 leftBtn 클릭
    public void backBtn(View v) {
        finish();
    }

}//end of class
