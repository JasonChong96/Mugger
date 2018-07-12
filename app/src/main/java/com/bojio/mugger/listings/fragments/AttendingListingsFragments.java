package com.bojio.mugger.listings.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bojio.mugger.listings.ListingUtils;
import com.bojio.mugger.listings.MyScheduleActivity;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListingsFragmentInteractionListener}
 * interface.
 */
public class AttendingListingsFragments extends ListingsFragments {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.mQuery = ListingUtils.getAttendingListingsQuery(db, mAuth.getUid());
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = super.onCreateView(inflater, container, savedInstanceState);
    myScheduleButton.setVisibility(View.VISIBLE);
    myScheduleButton.setOnClickListener(v -> {
      startActivity(new Intent(getContext(), MyScheduleActivity.class));
    });
    return view;
  }
}