package com.missionofseoul.seoul.thebogi;

import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.missionofseoul.seoul.R;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class TermsActivity extends AppCompatActivity {

    //Actionbartext
    TextView titletext;
    String title;

    String terms;
    TextView termstext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            setTheme(R.style.CustumTheme);
        }
        setContentView(R.layout.activity_terms);

        //더보기에서 이용약관(no1),개인정보처리방침(no2),위치정보이용약관(no3)을 가져옴
        Intent intent = getIntent();
        terms = intent.getStringExtra("terms");

        switch (terms) {
            case "no1":
                title = "서비스이용약관";
                break;
            case "no2":
                title = "개인정보처리방침";
                break;
            case "no3":
                title = "위치정보 이용약관";
                break;
        }

        //ActionBar 커스텀하기
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.bottom_abs_layout);

        titletext = (TextView) findViewById(R.id.actionbar_text);
        titletext.setText(title); //Actionbar title를 더보기로


        termstext = (TextView) findViewById(R.id.termstext);

        Resources res = getResources();

        XmlResourceParser xrp = res.getXml(R.xml.terms);

        StringBuffer buffer = new StringBuffer();
        String name;
        String msg;

        try {
            xrp.next();
            int eventType = xrp.getEventType();

            while (eventType != XmlResourceParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlResourceParser.START_DOCUMENT:
                        break;
                    case XmlResourceParser.START_TAG:
                        name = xrp.getName();
                        if (name.equals(terms)) {
                            xrp.next();
                            msg = xrp.getText();
                            buffer.append(msg);
                            termstext.setText(buffer.toString());
                            // buffer.setLength(0);//StringBuffer 객체 초기화하기
                        }
                        break;
                    case XmlResourceParser.TEXT:
                        break;
                    case XmlResourceParser.END_TAG:
                        break;
                    case XmlResourceParser.END_DOCUMENT:
                        break;
                }//switch
                eventType = xrp.next();
            }//end of while

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }//************************************* end of onCreate *********************************************//


}//end of class
