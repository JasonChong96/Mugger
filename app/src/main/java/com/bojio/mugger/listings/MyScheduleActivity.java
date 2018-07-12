package com.bojio.mugger.listings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import com.bojio.mugger.R;
import com.bojio.mugger.authentication.LoggedInActivity;
import com.bojio.mugger.listings.fragments.MyScheduleFragment;

public class MyScheduleActivity extends LoggedInActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_my_schedule);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    Fragment fragment = MyScheduleFragment.newInstance();
    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    ft.replace(R.id.my_schedule_activity_frame, fragment);
    ft.commit();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == android.R.id.home) {
      onBackPressed();
    }

    return super.onOptionsItemSelected(item);
  }
}
