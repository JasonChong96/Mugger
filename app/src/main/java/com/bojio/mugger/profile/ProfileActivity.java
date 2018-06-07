package com.bojio.mugger.profile;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.bojio.mugger.R;

public class ProfileActivity extends AppCompatActivity
    implements ProfileFragment.OnProfileFragmentInteractionListener {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile);
    Bundle b = getIntent().getExtras();
    if (b == null || b.getString("profileUid") == null) {
      Toast.makeText(this, "Error: Missing profileUid", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }
    Fragment fragment = ProfileFragment.newInstance(b.getString("profileUid"));
    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    ft.replace(R.id.profile_frame, fragment);
    ft.commit();
  }

  @Override
  public void onProfileFragmentInteraction(Uri uri) {

  }
}
