package com.bojio.mugger;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bojio.mugger.authentication.GoogleLoginActivity;
import com.bojio.mugger.authentication.IvleLoginActivity;
import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.constants.DebugSettings;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity {

  @BindView(R.id.progressBar5)
  ProgressBar progressBar;
  private FirebaseAuth mAuth;
  private FirebaseFirestore db;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Toasty.Config.getInstance().setToastTypeface(Typeface.DEFAULT).apply();
    super.onCreate(savedInstanceState);
    mAuth = FirebaseAuth.getInstance();
    db = FirebaseFirestore.getInstance();
    FirebaseUser acc = mAuth.getCurrentUser();
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
    if (acc != null) { // Logged in
      SpotsDialog.Builder dialog = new SpotsDialog
          .Builder()
          .setContext(this)
          .setTheme(R.style.SpotsDialog)
          .setMessage("Signing in...")
          .setCancelable(false);
      dialog.build().show();
      // Checks if user has been verified as an NUS student by checking if NUSNETID has been
      // logged before
      checkAccount(acc);
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
   * Checks the account and starts the appropriate activity for the account. i.e If the account
   * has not logged in to IVLE for verification before, redirect to IVLE login. If not, redirect
   * to the listings main page.
   *
   * @param acc the account to check
   */
  private void checkAccount(FirebaseUser acc) {
    // Checks if user has been verified as an NUS student by checking if NUSNETID has been
    // logged before
    db.collection("users").document(acc.getUid()).get().addOnCompleteListener(task_ -> {
      if (!task_.isSuccessful()) {
        Toasty.error(this, "Error fetching user data. Please try again later.").show();
        mAuth.signOut();
        // Go back to login page
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
      } else {
        DocumentSnapshot result = task_.getResult();
        if (result.exists() && result.get("nusNetId") != null && !DebugSettings
            .ALWAYS_REDIRECT_TO_IVLE) {
          // Already have record of nus net id
          // Clears back stack
          Toasty.normal(this, "Welcome back, " + acc.getDisplayName(), Toast.LENGTH_SHORT).show();
          MuggerUserCache.getInstance().setData(result.getData());
          Intent intent = new Intent(this, Main2Activity.class);
          startActivity(intent);
          finish();
        } else {
          // No record of nusnetid, redirect to IVLE login
          Intent intent = new Intent(this, IvleLoginActivity.class);
          startActivity(intent);
        }
      }
    });
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
