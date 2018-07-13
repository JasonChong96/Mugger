package com.bojio.mugger.listings.fragments;

import android.os.Bundle;

import com.bojio.mugger.listings.ListingUtils;

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
    this.mQuery = ListingUtils.getMyListingsQuery(db, mAuth.getUid());
  }
}