package com.bojio.mugger.authentication;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bojio.mugger.Main2Activity;
import com.bojio.mugger.MainActivity;
import com.bojio.mugger.R;
import com.bojio.mugger.authentication.viewmodels.IvleLoginViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import needle.Needle;

public class IvleLoginActivity extends AppCompatActivity {

  @BindView(R.id.ivle_login)
  WebView webView;

  @BindView(R.id.progressBar4)
  ProgressBar progressBar;

  private IvleLoginViewModel mViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ivle_login);
    mViewModel = ViewModelProviders.of(this).get(IvleLoginViewModel.class);
    ButterKnife.bind(this);
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy);
    new MaterialDialog.Builder(this)
        .title("IVLE Login")
        .cancelable(false)
        .content("This IVLE login will only be done once per account (Unless your current modules" +
            " change). Information fetched will be your modules, faculty, major, gender and a " +
            "hashed " +
            "version of your NUSNET ID. Your NUSNET ID in our database is hashed using SHA-256 " +
            "for your privacy in case of data leaks. It is only stored to ensure each person " +
            "only has one Mugger account. Rest assured that no data collected can " +
            "be too easily traced back to a specific person. We also not store your real name. " +
            "Your default display name will be fetched from your google account and can be " +
            "changed in the settings page.")
        .positiveText("Proceed")
        .negativeText("Cancel and Quit")
        .onNegative((dialog, which) -> {
          finish();
          System.exit(0);
        })
        .show();
    webView.setWebViewClient(new WebViewClient() {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url.startsWith("https://ivle.nus.edu.sg/api/login/muggerapp.com?token=")) {
          webView.setVisibility(View.GONE);
          Needle.onMainThread().execute(() -> {
            AlertDialog dialog = new SpotsDialog
                .Builder()
                .setContext(IvleLoginActivity.this)
                .setMessage("Loading data from IVLE...")
                .setTheme(R.style.SpotsDialog)
                .setCancelable(false)
                .build();
            dialog.show();
          });
          String token = url.substring(54);
          // Run in parallel so the loading screen still shows for the user while data is loading.
          Needle.onBackgroundThread().execute(() -> {
            Looper.prepare();
            mViewModel.init(token);
            if (!mViewModel.loadProfile()) {
              onError();
              return;
            }
            if (!mViewModel.checkNusNetId()) {
              Intent intent = new Intent(IvleLoginActivity.this, MainActivity.class);
              // Clears back stack
              intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
              Bundle b = new Bundle();
              b.putString("errorMessage", "Either your IVLE account is already tagged to another " +
                  "Mugger account or there was an error loading your data. " +
                  "You are only allowed one account per person.");
              intent.putExtras(b);
              startActivity(intent);
              finish();
              return;
            }
            if (!mViewModel.getModules()) {
              onError();
              return;
            }
            if (!mViewModel.saveData()) {
              onError();
            } else {
              Intent intent = new Intent(IvleLoginActivity.this, Main2Activity.class);
              // Clears back stack
              intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
              startActivity(intent);
              finish();
            }
          });

          return true;
        }
        return false;
      }
    });
    webView.loadUrl("https://ivle.nus.edu.sg/api/login/?apikey=OEoF4T2bHfpAn85PuAqoN&url=muggerapp.com");
  }


  private void onError() {
    Intent intent = new Intent(this, MainActivity.class);
    // Clears back stack
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
    Bundle b = new Bundle();
    b.putString("errorMessage", "An error has occured, please try again later.");
    intent.putExtras(b);
    startActivity(intent);
    finish();
    return;
  }
}
