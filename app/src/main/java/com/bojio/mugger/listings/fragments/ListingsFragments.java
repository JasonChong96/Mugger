package com.bojio.mugger.listings.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bojio.mugger.R;
import com.bojio.mugger.Roles;
import com.bojio.mugger.listings.AvailableListingDetailsActivity;
import com.bojio.mugger.listings.Listing;
import com.bojio.mugger.listings.ListingsViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

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

  @BindView(R.id.list)
  RecyclerView mRecyclerView;

  FirebaseFirestore db = FirebaseFirestore.getInstance();
  FirebaseAuth mAuth;
  protected Query mQuery;
  private int mColumnCount = 1;
  private OnListingsFragmentInteractionListener mListener;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public ListingsFragments() {
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
    // Set the adapter

    if (view instanceof RecyclerView) {
      Context context = view.getContext();
      RecyclerView recyclerView = (RecyclerView) view;
      if (mColumnCount <= 1) {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
      } else {
        recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
      }
    }

    initListings();
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

  private void initListings() {



    FirestoreRecyclerOptions<Listing> options = new FirestoreRecyclerOptions.Builder<Listing>()
        .setQuery(mQuery, snapshot -> {
          if ((Long) snapshot.get("endTime") < System.currentTimeMillis()) {
            // Delete outdated entries
            snapshot.getReference().delete();
          }
          return Listing.getListingFromSnapshot(snapshot);
        })
        .build();
    FirestoreRecyclerAdapter adapter = new FirestoreRecyclerAdapter<Listing, ListingsViewHolder>(options) {
      @Override
      public void onBindViewHolder(ListingsViewHolder holder, int position, Listing listing) {
        holder.itemView.setOnClickListener((view) -> {
          Intent intent = new Intent(view.getContext(), AvailableListingDetailsActivity.class);
          Bundle b = new Bundle();
          b.putParcelable("listing", listing);
          intent.putExtras(b);
          view.getContext().startActivity(intent);
        });
        int type = listing.getType();
        String title = listing.getModuleCode();
        if (listing.getOwnerId().equals(mAuth.getCurrentUser().getUid())) {
          holder.cardView.setCardBackgroundColor(holder.view.getContext().getResources().getColor
              (R.color.own_listing_background));
          title += " (Yours)";
        } else if (type == Roles.PROFESSOR) {
          holder.cardView.setCardBackgroundColor(holder.view.getContext().getResources().getColor
              (R.color.prof_listing_background));
          title += " (Professor)";
        } else if (type == Roles.TEACHING_ASSISTANT) {
          holder.cardView.setCardBackgroundColor(holder.view.getContext().getResources().getColor
              (R.color.ta_listing_background));
          title += " (TA)";
        }
        holder.moduleCode.setText(title);
        holder.venue.setText(listing.getVenue());
        DateFormat df = android.text.format.DateFormat.getDateFormat(ListingsFragments.this
            .getActivity());
        DateFormat dfTime = android.text.format.DateFormat.getTimeFormat(ListingsFragments
            .this.getActivity());
        Date startDateTime = new Date(listing.getStartTime());
        Date endDateTime = new Date(listing.getEndTime());
        holder.dateTime.setText(new StringBuilder()
            .append(df.format(startDateTime))
            .append(" ")
            .append(dfTime.format(startDateTime))
            .append(" - ")
            .append(df.format(endDateTime))
            .append(" ")
            .append(dfTime.format(endDateTime))
            .toString());
        holder.numAttendees.setText(String.format(Locale.getDefault(), "%d",listing.getNumAttendees
            ()));

        // holder.moduleCode.setOnClickListener(view -> Toast.makeText(AvailableListingsFragments.this.getActivity(), "Clicked", Toast.LENGTH_SHORT).show());
      }


      @Override
      public ListingsViewHolder onCreateViewHolder(ViewGroup group, int i) {
        // Create a new instance of the ViewHolder, in this case we are using a custom
        // layout called R.layout.message for each item
        View view = LayoutInflater.from(group.getContext())
            .inflate(R.layout.listing_card_view, group, false);

        return new ListingsViewHolder(view);
      }
    };
    mRecyclerView.setAdapter(adapter);
    adapter.startListening();
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
