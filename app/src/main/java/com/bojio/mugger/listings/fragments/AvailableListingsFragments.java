package com.bojio.mugger.listings.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.database.MuggerDatabase;
import com.bojio.mugger.listings.ListingUtils;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

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
  ArrayList<String> modules;

  /**
   * {@inheritDoc}
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    this.mQuery = ListingUtils.getAvailableListingsQuery(db).orderBy("startTime");
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    MuggerUserCache cache = MuggerUserCache.getInstance();
    View view = super.onCreateView(inflater, container, savedInstanceState);
    constraintLayout2.setVisibility(View.VISIBLE);
    modules = ListingUtils.getFilterModules(cache);
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout
        .simple_dropdown_item_1line, modules);
    spinner.setAdapter(adapter);
    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        changeModule(position);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
        changeModule(0);
      }

      private void changeModule(int index) {
        if (index == 0) {
          AvailableListingsFragments.this.mQuery = MuggerDatabase.getAllListingsReference(db)
              .orderBy("startTime", Query.Direction.ASCENDING);
        } else {
          AvailableListingsFragments.this.mQuery = MuggerDatabase.getAllListingsReference(db)
              .orderBy(getMods().get(index), Query.Direction.ASCENDING);
        }
        initListings();
      }
    });
    return view;
  }

  /**
   * Returns the module filters that are selectable by the user
   *
   * @return An array list of the module filters that are selectable by the user
   */
  private List<String> getMods() {
    return modules;
  }
}
