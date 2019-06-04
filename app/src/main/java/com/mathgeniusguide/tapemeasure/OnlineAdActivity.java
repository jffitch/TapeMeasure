package com.mathgeniusguide.tapemeasure;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by grenade on 11/26/2017.
 */

public class OnlineAdActivity extends AppCompatActivity {
    CountDownTimer fiveSeconds = new CountDownTimer(5000, 1000) {
        public void onTick(long a) {

        }

        public void onFinish() {
            TextView close = (TextView) findViewById(R.id.adFinish);
            close.setVisibility(View.VISIBLE);
            backAllowed = true;
        }
    };

    boolean backAllowed;

    protected void onCreate(Bundle savedInstanceState) {
        int i;
        super.onCreate(savedInstanceState);
        backAllowed = false;
        setContentView(R.layout.ads_online);

        TextView close = (TextView) findViewById(R.id.adFinish);
        close.setVisibility(View.GONE);
        fiveSeconds.start();
        close.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                finish();
            }
        });

        WebView webview = (WebView) findViewById(R.id.adView);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl("http://mathgeniusguide.com/one ad.html");
        webview.setWebViewClient(new WebViewClient(){
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error){
                setContentView(R.layout.ads);
                TextView close = (TextView) findViewById(R.id.adFinish);
                close.setVisibility(View.GONE);
                fiveSeconds.start();
                close.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View view) {
                        finish();
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (backAllowed) {
            super.onBackPressed();
        }
    }
}
