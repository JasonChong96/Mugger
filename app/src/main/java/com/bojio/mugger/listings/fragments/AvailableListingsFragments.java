package com.bojio.mugger.listings.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.lifecycle.LifecycleUtils;
import com.bojio.mugger.listings.ListingUtils;
import com.bojio.mugger.listings.viewmodels.AvailableListingsViewModel;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListingsFragmentInteractionListener}
 * interface.
 */
public class AvailableListingsFragments extends ListingsFragments {
  /**
   * ArrayList of module filters that can be selected by the user
   **/
  private ArrayList<String> modules;

  /**
   * User data cache
   **/
  private MuggerUserCache cache;

  /**
   * {@inheritDoc}
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    this.mQuery = ListingUtils.getAvailableListingsQuery(db).orderBy("startTime");
    cache = MuggerUserCache.getInstance();
    super.onCreate(savedInstanceState);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    mViewModel = ViewModelProviders.of(this, LifecycleUtils.getAndroidViewModelFactory
        (getActivity().getApplication())).get(AvailableListingsViewModel.class);
    getViewModel().getShowUnrelatedModules().observe(this, show -> {
      modules = ListingUtils.getFilterModules(cache);
      boolean triggerChange = false;
      if (getViewModel().getSelected() == null || modules.indexOf(getViewModel().getSelected()) < 0) {
        getViewModel().setSelected(modules.get(0));
        triggerChange = true;
      }
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout
          .simple_dropdown_item_1line, modules);
      spinner.setAdapter(adapter);
      spinner.setSelection(modules.indexOf(getViewModel().getSelected()), triggerChange);
    });
    super.onActivityCreated(savedInstanceState);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View view = super.onCreateView(inflater, container, savedInstanceState);
    constraintLayout2.setVisibility(View.VISIBLE);

    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        getViewModel().setSelected(modules.get(position));
        changeModule(position);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
        changeModule(0);
      }

      private void changeModule(int index) {
        getViewModel().selectionChanged(index == 0, modules.get
            (index));
        initListings();
      }
    });
    return view;
  }

  /**
   * Returns the view model type cast as an AvailableListingsViewModel
   *
   * @return ViewModel type cast as an AvailableListingsViewModel;
   */
  private AvailableListingsViewModel getViewModel() {
    return (AvailableListingsViewModel) mViewModel;
  }
}
