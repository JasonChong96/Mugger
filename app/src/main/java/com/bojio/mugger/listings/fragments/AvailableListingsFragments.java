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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListingsFragmentInteractionListener}
 * interface.
 */
public class AvailableListingsFragments extends ListingsFragments {
  ArrayList<String> modules;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    this.mQuery = db.collection("listings")
        .orderBy("startTime", Query.Direction.ASCENDING);
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    MuggerUserCache cache = MuggerUserCache.getInstance();
    View view = super.onCreateView(inflater, container, savedInstanceState);
    constraintLayout2.setVisibility(View.VISIBLE);
    long unrelatedModules = 0L;
    if (cache.getData().containsKey("showUnrelatedModules")) {
      unrelatedModules = (long) cache.getData().get("showUnrelatedModules");
    }
    if (unrelatedModules != 0) {
      modules = new ArrayList<>(cache.getAllModules());;
    } else {
      modules = new ArrayList<>(cache.getModules().firstEntry()
          .getValue().keySet());
    }
    modules.add(0, "Show all modules");
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
          AvailableListingsFragments.this.mQuery = db.collection("listings")
              .orderBy("startTime", Query.Direction.ASCENDING);
        } else {
          AvailableListingsFragments.this.mQuery = db.collection("listings")
              .orderBy(getMods().get(index), Query.Direction.ASCENDING);
        }
        initListings();
      }
    });
    return view;
  }

  private List<String> getMods() {
    return modules;
  }
}
