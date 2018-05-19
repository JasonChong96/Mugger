package com.bojio.mugger.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bojio.mugger.R;
import com.bojio.mugger.listings.Listing;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ItemFragment extends Fragment {

    @BindView(R.id.list)
    RecyclerView mRecyclerView;

    // TODO: Customize parameter argument names
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemFragment() {
    }
    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ItemFragment newInstance(int columnCount) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
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
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Listing item);
    }

    private void initListings() {
        Query mQuery = db.collection("listings")
                .orderBy("startTime", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Listing> options = new FirestoreRecyclerOptions.Builder<Listing>()
                .setQuery(mQuery, snapshot -> {
                    if ((Long) snapshot.get("endTime") < System.currentTimeMillis()) {
                        // Delete outdated entries
                        snapshot.getReference().delete();
                    }
                    return new Listing((String) snapshot.get("ownerId"),
                            (String) snapshot.get("moduleCode"),
                            (Long) snapshot.get("startTime"),
                            (Long) snapshot.get("endTime"),
                            (String) snapshot.get("description"),
                            (String) snapshot.get("venue")
                    );
                })
                .build();
        FirestoreRecyclerAdapter adapter = new FirestoreRecyclerAdapter<Listing, ListingsViewHolder>(options) {
            @Override
            public void onBindViewHolder(ListingsViewHolder holder, int position, Listing listing) {
                holder.moduleCode.setText(listing.getModuleCode());
                holder.venue.setText(listing.getVenue());
                DateFormat df = new SimpleDateFormat("dd/MM HH:mm", Locale.US);
                holder.dateTime.setText(new StringBuilder()
                        .append(df.format(new Date(listing.getStartTime())))
                        .append(" - ")
                        .append(df.format(new Date(listing.getEndTime())))
                        .toString());
               // holder.moduleCode.setOnClickListener(view -> Toast.makeText(ItemFragment.this.getActivity(), "Clicked", Toast.LENGTH_SHORT).show());
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

    public class ListingsViewHolder extends RecyclerView.ViewHolder {
        public TextView moduleCode;
        public TextView dateTime;
        public TextView venue;

        ListingsViewHolder(View view) {
            super(view);
            moduleCode = view.findViewById(R.id.module_code);
            dateTime = view.findViewById(R.id.date_time);
            venue = view.findViewById(R.id.venue);
        }
    }
}