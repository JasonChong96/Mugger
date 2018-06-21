package com.bojio.mugger.listings.fragments;

import android.os.Bundle;

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
    this.mQuery = db.collection("listings")
        .orderBy(mAuth.getUid());
  }
}