package com.bojio.mugger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.bojio.mugger.authentication.GoogleLoginActivity;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser acc = mAuth.getCurrentUser();
        setContentView(R.layout.activity_main);
        if (acc != null) { // Logged in, redirect to listings immediately
            Toast.makeText(this, "Welcome back, " + acc.getDisplayName(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, Main2Activity.class);
            startActivity(intent);
            finish();
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
