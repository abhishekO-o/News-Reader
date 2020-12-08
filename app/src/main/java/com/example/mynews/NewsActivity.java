package com.example.mynews;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class NewsActivity extends AppCompatActivity {
WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        webView=(WebView)findViewById(R.id.webView);

        webView.getSettings().getJavaScriptEnabled();
        webView.setWebViewClient(new WebViewClient());

        Intent intent=getIntent();
        int pos=intent.getIntExtra("pos",-1);
        String url=MainActivity.urlArrayList.get(pos);

        webView.loadUrl(url);
    }
}