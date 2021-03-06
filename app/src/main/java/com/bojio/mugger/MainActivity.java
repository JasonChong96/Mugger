package com.bojio.mugger;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bojio.mugger.authentication.GoogleLoginActivity;
import com.bojio.mugger.authentication.IvleLoginActivity;
import com.google.android.gms.common.SignInButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.mateware.snacky.Snacky;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;
import needle.Needle;

public class MainActivity extends AppCompatActivity {

  @BindView(R.id.progressBar5)
  ProgressBar progressBar;
  private MainActivityViewModel mViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Toasty.Config.getInstance().setToastTypeface(Typeface.DEFAULT).apply();
    super.onCreate(savedInstanceState);
    mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
    setContentView(R.layout.activity_main);
    initGoogleSignInButton();
    ButterKnife.bind(this);
    Bundle b = getIntent().getExtras();
    if (b != null) {
      String errMsg = b.getString("errorMessage");
      if (errMsg != null) {
        Toasty.error(this, errMsg).show();
      }
    }
    AlertDialog dialog = new SpotsDialog
        .Builder()
        .setContext(this)
        .setTheme(R.style.SpotsDialog)
        .setMessage("Signing in...")
        .setCancelable(false)
        .build();
    if (mViewModel.isLoggedIn()) {
      dialog.show();
    }
    Needle.onBackgroundThread().execute(() -> {
      // ViewModel init method requires waiting for Firestore API response, done on background
      // thread so main UI thread doesn't stall.
      if (!mViewModel.init()) {
        // Error loading user data.
        Needle.onMainThread().execute(() -> {
          dialog.dismiss();
          Snacky.builder()
              .setActivity(this)
              .setText("Error signing in, please try again.")
              .error()
              .show();
        });
      } else if (mViewModel.isLoggedIn()) {
        Needle.onMainThread().execute(() -> {
          dialog.dismiss();
          startNextActivity();
        });
      }
    });
  }

  /**
   * Starts the next activity. To be called if the user is already logged in. Checks if the user
   * has done IVLE log in before, if he/she has, then redirects to Main2Activity, or else it
   * redirects to IvleLoginActivity.
   */
  private void startNextActivity() {
    if (mViewModel.isRedirectToIvleLogin()) {
      // No record of nusnetid, redirect to IVLE login
      startActivity(new Intent(this, IvleLoginActivity.class));
    } else {
      Toasty.normal(this, "Welcome back, " + mViewModel.getDisplayName(), Toast.LENGTH_SHORT)
          .show();
      mViewModel.updateCache();
      startActivity(new Intent(this, Main2Activity.class));
      finish();
    }
  }

  /**
   * Changes the google sign in button to a larger size.
   */
  private void initGoogleSignInButton() {
    SignInButton signInButton = findViewById(R.id.sign_in_button);
    signInButton.setSize(SignInButton.SIZE_WIDE);
  }

  /**
   * Invoked when the google sign in button is clicked. Starts the login activity.
   */
  @OnClick(R.id.sign_in_button)
  void onClickGoogleSignIn() {
    // Starts login activity
    Intent intent = new Intent(MainActivity.this, GoogleLoginActivity.class);
    startActivity(intent);
  }
}
