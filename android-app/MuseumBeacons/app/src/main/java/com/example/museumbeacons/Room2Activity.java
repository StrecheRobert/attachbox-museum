package com.example.museumbeacons;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Room2Activity extends AppCompatActivity {

    private static final String URL_102 =
            "https://strecherobert.github.io/attachbox-museum/102.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room2);

        WebView webView = findViewById(R.id.webViewRoom2);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(true);

        webView.loadUrl(URL_102);
    }

    @Override
    public void onBackPressed() {
        WebView webView = findViewById(R.id.webViewRoom2);
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
