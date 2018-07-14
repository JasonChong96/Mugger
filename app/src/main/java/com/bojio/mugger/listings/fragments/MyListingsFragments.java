package com.bojio.mugger.listings.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.bojio.mugger.lifecycle.LifecycleUtils;
import com.bojio.mugger.listings.viewmodels.MyListingsViewModel;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListingsFragmentInteractionListener}
 * interface.
 */
public class MyListingsFragments extends ListingsFragments {

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    mViewModel = ViewModelProviders.of(this, LifecycleUtils.getAndroidViewModelFactory
        (getActivity().getApplication())).get(MyListingsViewModel.class);
    super.onActivityCreated(savedInstanceState);
  }

}