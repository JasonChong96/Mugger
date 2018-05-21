package com.bojio.mugger.authentication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.bojio.mugger.R;

import butterknife.BindView;

public class IvleLoginActivity extends AppCompatActivity {

  @BindView(R.id.ivle_login)
  WebView webView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ivle_login);
    webView.setWebViewClient(new WebViewClient() {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url.startsWith("https://ivle.nus.edu.sg/api/login/muggerapp.com?token=")) {
          String token = url.substring(54);
          return true;
        }
        return false;
      }
    });
    webView.loadUrl("https://ivle.nus.edu.sg/api/login/?apikey=OEoF4T2bHfpAn85PuAqoN&url=muggerapp.com");

  }


}
