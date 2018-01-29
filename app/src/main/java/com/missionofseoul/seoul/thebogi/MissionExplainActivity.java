package com.missionofseoul.seoul.thebogi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.missionofseoul.seoul.G;
import com.missionofseoul.seoul.R;

public class MissionExplainActivity extends AppCompatActivity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_explain);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//statusbar hidden

        //웹뷰를 붙이기
        webView = (WebView) findViewById(R.id.webview);

        webView.setWebViewClient(new WebViewClient());

        webView.setWebChromeClient(new WebChromeClient());

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.loadUrl(G.domain + "web/explain/ex.html");

    }

}
