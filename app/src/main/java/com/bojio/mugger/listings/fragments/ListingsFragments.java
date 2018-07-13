package com.bojio.mugger.listings.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.annimon.stream.function.Predicate;
import com.bojio.mugger.R;
import com.bojio.mugger.listings.Listing;
import com.bojio.mugger.listings.ListingUtils;
import com.bojio.mugger.listings.ListingsFirestoreAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.DateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListingsFragmentInteractionListener}
 * interface.
 */
public abstract class ListingsFragments extends Fragment {

  private static final String ARG_COLUMN_COUNT = "column-count";
  protected Query mQuery;
  protected Predicate<Listing> predicateFilter;
  protected boolean delayInitListings;
  @BindView(R.id.list)
  RecyclerView mRecyclerView;
  @BindView(R.id.listings_fragments_spinner)
  Spinner spinner;
  @BindView(R.id.listings_fragments_constraint_layout_2)
  ConstraintLayout constraintLayout2;
  @BindView(R.id.listings_fragments_empty_text)
  TextView emptyTextView;
  @BindView(R.id.listings_fragments_filter_to_date)
  TextInputEditText filterToDateView;
  @BindView(R.id.listings_fragments_filter_from_date)
  TextInputEditText filterFromDateView;
  @BindView(R.id.listings_fragments_button_filter_settings)
  MaterialButton filterSettingsButton;
  @BindView(R.id.listings_fragments_view_schedule_button)
  MaterialButton myScheduleButton;
  FirebaseFirestore db = FirebaseFirestore.getInstance();
  FirebaseMessaging fcm = FirebaseMessaging.getInstance();
  FirebaseAuth mAuth;
  private int mColumnCount = 1;
  private OnListingsFragmentInteractionListener mListener;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public ListingsFragments() {
    delayInitListings = false;
  }


  public static ListingsFragments newInstance(int columnCount) {
    AvailableListingsFragments fragment = new AvailableListingsFragments();
    Bundle args = new Bundle();
    args.putInt(ARG_COLUMN_COUNT, columnCount);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    mAuth = FirebaseAuth.getInstance();
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
    }
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_available_listings, container, false);
    ButterKnife.bind(this, view);
    Context context = view.getContext();
    if (mColumnCount <= 1) {
      mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
    } else {
      mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
    }
    DateFormat df = android.text.format.DateFormat.getDateFormat(ListingsFragments.this
        .getActivity(ListingsFragments.this));
    filterFromDateView.setText(df.format(new Date()));
    filterToDateView.setText(df.format(new Date(ListingUtils.DEFAULT_TIME_FILTER_END)));
    if (!delayInitListings) {
      initListings();
    }
    return view;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnListingsFragmentInteractionListener) {
      mListener = (OnListingsFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString()
          + " must implement OnAvailableListingsFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  /**
   * When inside a nested fragment and Activity gets recreated due to reasons like orientation
   * change, {@link android.support.v4.app.Fragment#getActivity()} returns old Activity but the top
   * level parent fragment's {@link android.support.v4.app.Fragment#getActivity()} returns current,
   * recreated Activity. Hence use this method in nested fragments instead of
   * android.support.v4.app.Fragment#getActivity()
   *
   * @param fragment The current nested Fragment
   * @return current Activity that fragment is hosted in
   */
  public Activity getActivity(Fragment fragment) {
    if (fragment == null) {
      return null;
    }
    while (fragment.getParentFragment() != null) {
      fragment = fragment.getParentFragment();
    }
    return fragment.getActivity();
  }

  protected void initListings() {
    if (mRecyclerView.getAdapter() != null) {
      ((FirestoreRecyclerAdapter) mRecyclerView.getAdapter()).stopListening();
    }
    FirestoreRecyclerOptions<Listing> options = new FirestoreRecyclerOptions.Builder<Listing>()
        .setQuery(mQuery, snapshot -> {
          if ((Long) snapshot.get("endTime") < System.currentTimeMillis()) {
            // Delete outdated entries
            snapshot.getReference().delete();
          }
          return Listing.getListingFromSnapshot(snapshot);
        })
        .build();
    FirestoreRecyclerAdapter adapter = new ListingsFirestoreAdapter(options, getActivity(this)
        .getApplicationContext(), mAuth, db, fcm, predicateFilter);
    adapter.startListening();
    mRecyclerView.setAdapter(adapter);

  }


  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p/>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  public interface OnListingsFragmentInteractionListener {
    void onListingFragmentInteraction(Listing item);
  }
}
