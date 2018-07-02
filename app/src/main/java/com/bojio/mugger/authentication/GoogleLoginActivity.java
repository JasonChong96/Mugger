package com.bojio.mugger.authentication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bojio.mugger.Main2Activity;
import com.bojio.mugger.MainActivity;
import com.bojio.mugger.R;
import com.bojio.mugger.constants.DebugSettings;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;

public class GoogleLoginActivity extends AppCompatActivity {
  private static final int RC_SIGN_IN = 9001;
  private static final String TAG = "GoogleLoginActivity";
  GoogleSignInClient mGoogleSignInClient;
  private FirebaseAuth mAuth;
  private FirebaseFirestore db;

  /**
   * {@inheritDoc}
   *
   * @param savedInstanceState
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mAuth = FirebaseAuth.getInstance();
    db = FirebaseFirestore.getInstance();
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("292230336625-pa93l9untqrvad2mc6m3i77kckjkk4k1.apps.googleusercontent.com")
        .requestEmail()
        .build();
    mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    signIn();
    setContentView(R.layout.activity_google_login);
    ProgressBar pgsBar = findViewById(R.id.progressBar);
    pgsBar.setVisibility(View.GONE);
    // Sets behavior when logged in state changes
    initAuthStateListener();
  }

  /**
   * Opens google authentication.
   */
  private void signIn() {
    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
    startActivityForResult(signInIntent, RC_SIGN_IN);
  }

  /**
   * Attempt to sign in using google authentication. If login is successful,
   * brings user to the listings page.
   *
   * @param requestCode requestCode
   * @param resultCode  resultCode
   * @param data        data
   */
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
    if (requestCode == RC_SIGN_IN) {
      Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
      try {
        // Google Sign In was successful, authenticate with Firebase
        GoogleSignInAccount account = task.getResult(ApiException.class);
        firebaseAuthWithGoogle(account);
        AlertDialog dialog = new SpotsDialog
            .Builder()
            .setContext(this)
            .setMessage("Signing in...")
            .setTheme(R.style.SpotsDialog)
            .setCancelable(false)
            .build();
        dialog.show();

      } catch (ApiException e) {
        // Google Sign In failed,
        Log.w(TAG, "Google sign in failed.", e);
        Toasty.error(this, "Sign in failed, Please try again", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
      }
    }
  }

  private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
    Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
    AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
    mAuth.signInWithCredential(credential)
        .addOnCompleteListener(this, task -> {
          if (task.isSuccessful()) {
            // Sign in success, update UI with the signed-in user's information
            Log.d(TAG, "signInWithCredential:success");
          } else {
            // Sign in fails
            Log.w(TAG, "signInWithCredential:failure", task.getException());
          }
        });
  }

  private void initAuthStateListener() {
    mAuth.addAuthStateListener(auth -> {
      FirebaseUser user = auth.getCurrentUser();
      if (user != null) {
        // If signed in, check if user has been verified as an NUS student by checking if
        // hashed nusnetid has been cached
        db.collection("users").document(user.getUid()).get().addOnCompleteListener(task_ -> {
          if (!task_.isSuccessful()) {
            Toasty.error(this, "Error fetching user data. Please try again later.", Toast
                .LENGTH_SHORT).show();
            mAuth.signOut();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
          } else {
            DocumentSnapshot result = task_.getResult();
            if (result.exists() && result.get("nusNetId") != null && !DebugSettings
                .ALWAYS_REDIRECT_TO_IVLE) {
              // If already verified, then go straight to main listings page
              Intent intent = new Intent(this, Main2Activity.class);
              MuggerUserCache.getInstance().setData(result.getData());
              // Clears back stack
              intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
              startActivity(intent);
              finish();
            } else {
              // If not cached then go to IVLE Login
              Intent intent = new Intent(this, IvleLoginActivity.class);
              startActivity(intent);
              finish();
            }
          }
        });
      }
    });
  }
}
