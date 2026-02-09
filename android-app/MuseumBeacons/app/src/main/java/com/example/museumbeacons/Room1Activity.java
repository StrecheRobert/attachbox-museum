package com.example.museumbeacons;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Room1Activity extends AppCompatActivity {

    private static final String URL_101 =
            "https://strecherobert.github.io/attachbox-museum/101.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room1);

        WebView webView = findViewById(R.id.webViewRoom1);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(false);
        webView.getSettings().setDomStorageEnabled(true);

        webView.loadUrl(URL_101);
    }

    @Override
    public void onBackPressed() {
        WebView webView = findViewById(R.id.webViewRoom1);
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
