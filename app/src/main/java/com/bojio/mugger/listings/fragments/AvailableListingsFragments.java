package com.bojio.mugger.listings.fragments;

import android.os.Bundle;

import com.google.firebase.firestore.Query;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListingsFragmentInteractionListener}
 * interface.
 */
public class AvailableListingsFragments extends ListingsFragments {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    this.mQuery = db.collection("listings")
        .orderBy("startTime", Query.Direction.ASCENDING);
    super.onCreate(savedInstanceState);
  }
}
