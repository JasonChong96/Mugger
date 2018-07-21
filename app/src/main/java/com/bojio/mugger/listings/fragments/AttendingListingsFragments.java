package com.bojio.mugger.listings.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bojio.mugger.lifecycle.LifecycleUtils;
import com.bojio.mugger.listings.ListingUtils;
import com.bojio.mugger.listings.MyScheduleActivity;
import com.bojio.mugger.listings.viewmodels.AttendingListingsViewModel;

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
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    mViewModel = ViewModelProviders.of(this, LifecycleUtils.getAndroidViewModelFactory
        (getActivity().getApplication())).get(AttendingListingsViewModel.class);
    super.onActivityCreated(savedInstanceState);
  }
}