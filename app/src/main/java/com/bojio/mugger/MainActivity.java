package com.bojio.mugger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bojio.mugger.authentication.GoogleLoginActivity;
import com.bojio.mugger.authentication.IvleLoginActivity;
import com.bojio.mugger.authentication.MuggerUser;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

  private FirebaseAuth mAuth;
  private FirebaseFirestore db;

  @BindView(R.id.progressBar5)
  ProgressBar progressBar;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mAuth = FirebaseAuth.getInstance();
    db = FirebaseFirestore.getInstance();
    FirebaseUser acc = mAuth.getCurrentUser();
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    if (acc != null) { // Logged in
      progressBar.setVisibility(View.VISIBLE);
      db.collection("users").document(acc.getUid()).get().addOnCompleteListener(task_ -> {
        if (!task_.isSuccessful()) {
          Toast.makeText(this, "Error fetching user data. Please try again later.", Toast
              .LENGTH_SHORT);
          mAuth.signOut();
          Intent intent = new Intent(this, MainActivity.class);
          startActivity(intent);
          finish();
        } else {
          DocumentSnapshot result = task_.getResult();
          if (result.exists() && result.get("nusNetId") != null) {
            // Clears back stack
            Toast.makeText(this, "Welcome back, " + acc.getDisplayName(), Toast.LENGTH_SHORT).show();
            MuggerUser.getInstance().setData(result.getData());
            Intent intent = new Intent(this, Main2Activity.class);
            startActivity(intent);
            finish();
          } else {
            Intent intent = new Intent(this, IvleLoginActivity.class);
            startActivity(intent);
          }
        }
      });

    }
    SignInButton signInButton = findViewById(R.id.sign_in_button);
    signInButton.setSize(SignInButton.SIZE_WIDE);
    signInButton.setOnClickListener((view) -> {
      // Starts login activity
      Intent intent = new Intent(MainActivity.this, GoogleLoginActivity.class);
      startActivity(intent);
    });
  }
}
