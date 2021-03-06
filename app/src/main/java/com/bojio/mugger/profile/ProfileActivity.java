package com.bojio.mugger.profile;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.widget.Toast;

import com.bojio.mugger.R;
import com.bojio.mugger.authentication.LoggedInActivity;

import es.dmoral.toasty.Toasty;

/**
 * Activity that encapsulates the ProfileFragment when needed.
 */
public class ProfileActivity extends LoggedInActivity
    implements ProfileFragment.OnProfileFragmentInteractionListener {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (stopActivity) {
      finish();
      return;
    }
    setContentView(R.layout.activity_profile);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    Bundle b = getIntent().getExtras();
    if (b == null || b.getString("profileUid") == null) {
      Toasty.error(this, "Error: Missing profileUid", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }
    if (savedInstanceState == null) {
      Fragment fragment = ProfileFragment.newInstance(b.getString("profileUid"));
      FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
      ft.replace(R.id.profile_frame, fragment);
      ft.commit();
    }
  }

  @Override
  public void onProfileFragmentInteraction(Uri uri) {

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      // When back button on the top left is clicked
      case android.R.id.home:
        finish();
        return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
