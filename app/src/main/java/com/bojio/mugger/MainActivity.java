package com.bojio.mugger;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.bojio.mugger.authentication.GoogleLoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        FirebaseUser acc = mAuth.getCurrentUser();
        setContentView(R.layout.activity_main);
        if (acc == null) { // Not logged in
            Intent intent = new Intent(MainActivity.this, GoogleLoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Logged in to " + acc.getDisplayName(), Toast.LENGTH_SHORT).show();
            ((TextView) findViewById(R.id.textView)).setText("Logged in to " + acc.getDisplayName() + " " + acc.getUid());
            Intent intent = new Intent(this, Main2Activity.class);
            startActivity(intent);
            //account.getDisplayName()
        }
    }
}
