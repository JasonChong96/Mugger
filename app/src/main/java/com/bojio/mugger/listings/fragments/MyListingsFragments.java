package com.bojio.mugger.listings.fragments;

import android.os.Bundle;

import com.google.firebase.firestore.Query;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListingsFragmentInteractionListener}
 * interface.
 */
public class MyListingsFragments extends ListingsFragments {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.mQuery = db.collection("listings")
        .orderBy("startTime", Query.Direction.ASCENDING)
        .whereEqualTo("ownerId", mAuth.getCurrentUser().getUid());

  }
}