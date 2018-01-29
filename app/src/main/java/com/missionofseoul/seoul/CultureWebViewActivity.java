package com.missionofseoul.seoul;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class CultureWebViewActivity extends AppCompatActivity {

    WebView webView;
    String url;

    //액션바 멤버변수
    TextView abs_title;
    TextView save_text;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            setTheme(R.style.CustumTheme);
        }
        setContentView(R.layout.activity_culture_web_view);

        //ActionBar 커스텀하기      //emulator api 16에서 custom error
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);

        //액션바 타이틀
        abs_title = (TextView) findViewById(R.id.abs_title);
        abs_title.setText("문화행사정보");

        dialog = new ProgressDialog(CultureWebViewActivity.this);
        dialog.setMessage("접근 중입니다.");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCanceledOnTouchOutside(false);

        url = getIntent().getStringExtra("Url");
        url = url.toLowerCase();
        webView = (WebView) findViewById(R.id.webView);
        //웹뷰를 붙이기
        //웹뷰안에서 웹페이지가 열리도록 설정.
        //이 설정을 안하며 웹뷰는 로드된 웹페이지를 새로운 창으로 오픈함.
        //webView.setWebViewClient(new WebViewClient());

        //자바스크립트로 실행되는 alert()같은 브라우저의 별도의 필요한. 요청은.
        //아래설정이 없으면 실행되지 못함.
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                dialog.show();

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                dialog.dismiss();
            }
        });

        //로드된 페이지의 자바스크립트를 사용할 수 있도록 허용.
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        //웹뷰에게 웹페이지와 연동작업을 수행하는 인터페이스 객체 설정.
        //webView.addJavascriptInterface(new WebAppInterface(), "Droid");
        webView.loadUrl(url);

    }//end of onCreate()


    //액션바의 leftBtn 클릭
    public void backBtn(View v) {
        finish();
    }


}//end of class
