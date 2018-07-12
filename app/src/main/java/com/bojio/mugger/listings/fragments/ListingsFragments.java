package com.bojio.mugger.listings.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.annimon.stream.function.Predicate;
import com.bojio.mugger.R;
import com.bojio.mugger.administration.reports.MakeReportActivity;
import com.bojio.mugger.administration.reports.Report;
import com.bojio.mugger.authentication.MuggerRole;
import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.constants.ModuleRole;
import com.bojio.mugger.database.MuggerDatabase;
import com.bojio.mugger.fcm.MessagingService;
import com.bojio.mugger.listings.CreateEditListingActivity;
import com.bojio.mugger.listings.Listing;
import com.bojio.mugger.listings.ListingUtils;
import com.bojio.mugger.listings.ListingsViewHolder;
import com.bojio.mugger.listings.ViewAttendeesActivity;
import com.bojio.mugger.listings.chat.ListingChatActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.mateware.snacky.Snacky;
import dmax.dialog.SpotsDialog;

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
      private String uid = mAuth.getUid();

      @Override
      public void onDataChanged() {
        if (this.getItemCount() == 0) {
          emptyTextView.setVisibility(View.VISIBLE);
        } else if (emptyTextView.getVisibility() == View.VISIBLE) {
          emptyTextView.setVisibility(View.GONE);
        }
      }

      private void onClick_report(Listing listing) {
        Intent intent = new Intent(ListingsFragments.this.getContext(), MakeReportActivity.class);
        Bundle b = new Bundle();
        b.putParcelable("listing", listing);
        b.putString("reportType", Report.ReportType.LISTING.name());
        b.putString("listingUid", listing.getUid());
        intent.putExtras(b);
        startActivity(intent);
      }

      private void onClick_chat(Listing listing) {
        Intent intent = new Intent(ListingsFragments.this.getContext(), ListingChatActivity.class);
        Bundle b = new Bundle();
        b.putParcelable("listing", listing);
        intent.putExtras(b);
        startActivity(intent);
      }

      private void onClick_delete(Listing listing) {
        new MaterialDialog.Builder(ListingsFragments.this.getContext()).title("Confirmation").content
            ("Are you sure you want to delete this listing?").positiveText("Yes").negativeText("No")
            .onPositive((dialog, which) -> {
              AlertDialog dialogg = new SpotsDialog
                  .Builder()
                  .setContext(ListingsFragments.this.getContext())
                  .setMessage("Deleting listing...")
                  .setCancelable(false)
                  .setTheme(R.style.SpotsDialog)
                  .build();
              dialogg.show();
              MuggerDatabase.deleteListing(db, listing.getUid()).addOnCompleteListener(task -> {
                dialogg.dismiss();
                if (task.isSuccessful()) {
                  Map<String, Object> notificationData = new HashMap<>();
                  notificationData.put("title", "Listing Deleted");
                  StringBuilder body = new StringBuilder();
                  body.append(listing.getOwnerName()).append("'s ").append(listing.getModuleCode())
                      .append(" Listing has been deleted.");
                  notificationData.put("body", body.toString());
                  notificationData.put("type", MessagingService.DELETED_NOTIFICATION);
                  notificationData.put("fromUid", mAuth.getUid());
                  notificationData.put("topicUid", listing.getUid());
                  MuggerDatabase.sendNotification(db, notificationData);
                } else {
                  Snacky.builder().setActivity(ListingsFragments.this.getActivity())
                      .setText("Failed to delete listing, please try again later")
                      .error()
                      .show();
                }
              });
            }).show();
      }

      private void onClick_edit(Listing listing) {
        Intent intent = new Intent(ListingsFragments.this.getContext(), CreateEditListingActivity.class);
        Bundle b = new Bundle();
        b.putParcelable("listing", listing);
        intent.putExtras(b);
        startActivity(intent);
      }

      private void onClick_join(MaterialButton button, Listing listing) {
        DocumentReference listingRef = MuggerDatabase.getAllListingsReference(db).document(listing.getUid());
        Map<String, Object> updates = new HashMap<>();
        if (listing.isAttending(uid)) {
          if (mAuth.getUid().equals(listing.getOwnerId())) {
            Snacky.builder().setActivity(ListingsFragments.this.getActivity())
                .setText("You must be attending listings that you own.")
                .error()
                .show();
          } else {
            updates.put(mAuth.getUid(), FieldValue.delete());
            fcm.unsubscribeFromTopic(listing.getUid());
            listingRef.update(updates);
          }
        } else {
          if (listing.getNumAttendees() > 19) {
            Snacky.builder().setActivity(ListingsFragments.this.getActivity())
                .setText("There are too many people attending this listing")
                .error()
                .show();
          } else {
            updates.put(mAuth.getUid(), listing.getStartTime());
            fcm.subscribeToTopic(listing.getUid());
            listingRef.update(updates);
          }
        }
      }

      @Override
      public void onBindViewHolder(ListingsViewHolder holder, int position, Listing listing) {
        if (mAuth.getCurrentUser() == null) {
          return;
        }
        if (predicateFilter != null && !predicateFilter.test(listing)) {
          holder.cardView.setVisibility(View.GONE);
          return;
        }
        int type = listing.getType();
        String title = listing.getModuleCode();
        if (listing.isAttending(mAuth.getCurrentUser().getUid())) {
          holder.colorCode.setColorFilter(holder.view.getContext().getResources().getColor
              (R.color.own_listing_background));
          holder.colorCode.setVisibility(View.VISIBLE);
          if (listing.getOwnerId().equals(mAuth.getCurrentUser().getUid())) {
            title += " (Yours)";
          } else {
            title += " (Attending)";
          }
        } else if (type == ModuleRole.PROFESSOR) {
          holder.colorCode.setColorFilter(holder.view.getContext().getResources().getColor
              (R.color.prof_listing_background));
          holder.colorCode.setVisibility(View.VISIBLE);
          title += " (Professor)";
        } else if (type == ModuleRole.TEACHING_ASSISTANT) {
          holder.colorCode.setColorFilter(holder.view.getContext().getResources().getColor
              (R.color.ta_listing_background));
          holder.colorCode.setVisibility(View.VISIBLE);
          title += " (TA)";
        } else {
          holder.colorCode.setVisibility(View.INVISIBLE);
        }
        holder.moduleCode.setText(title);
        holder.venue.setText(listing.getVenue());
        /*DateFormat df = android.text.format.DateFormat.getDateFormat(ListingsFragments.this
            .getActivity(ListingsFragments.this));
        DateFormat dfTime = android.text.format.DateFormat.getTimeFormat(ListingsFragments
            .this.getActivity(ListingsFragments.this));
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
            */
        holder.dateTime.setText(getStartEndTimeDisplay(listing.getStartTime(), listing.getEndTime
            ()));
        holder.numAttendees.setText(String.format(Locale.getDefault(), "%d", listing.getNumAttendees
            ()));
        holder.nameView.setText(String.format("By %s", listing.getOwnerName()));
        holder.expandClickView.setOnClickListener(v -> {
          holder.expandLayout.callOnClick();
        });
        holder.numAttendeesClickView.setOnClickListener(v -> {
          Intent intent = new Intent(v.getContext(), ViewAttendeesActivity.class);
          Bundle b = new Bundle();
          b.putStringArrayList("profiles", (ArrayList<String>) listing.getAttendees());
          b.putString("ownerUid", listing.getOwnerId());
          intent.putExtras(b);
          startActivity(intent);
        });
        holder.expandLayout.setOnClickListener(view -> {
          if (!holder.isExpanded()) {
            TransitionManager.beginDelayedTransition(holder.cardView);
            holder.expandedLayout.setVisibility(View.VISIBLE);
            holder.expandedLayout2.setVisibility(View.VISIBLE);
            if (uid.equals(listing.getOwnerId()) || MuggerRole.MODERATOR
                .check(MuggerUserCache.getInstance().getRole())) {
              holder.creatorControlsLayout.setVisibility(View.VISIBLE);
            }
            holder.expandImage.setImageDrawable(getResources().getDrawable(R.drawable
                .ic_baseline_expand_less_24px));
          } else {
            holder.expandedLayout.setVisibility(View.GONE);
            holder.creatorControlsLayout.setVisibility(View.GONE);
            holder.expandedLayout2.setVisibility(View.GONE);
            holder.expandImage.setImageDrawable(getResources().getDrawable(R.drawable
                .ic_baseline_expand_more_24px));
          }
          holder.toggleExpanded();
        });
        holder.chatButton.setOnClickListener(v -> onClick_chat(listing));
        holder.reportButton.setOnClickListener(v -> onClick_report(listing));
        holder.editButton.setOnClickListener(v -> onClick_edit(listing));
        holder.deleteButton.setOnClickListener(v -> onClick_delete(listing));
        holder.descriptionView.setText(listing.getDescription());
        if (listing.getOwnerId().equals(mAuth.getUid())) {
          holder.joinButton.setVisibility(View.INVISIBLE);
          holder.unjoinButton.setVisibility(View.INVISIBLE);
        } else if (listing.isAttending(uid)) {
          holder.joinButton.setVisibility(View.INVISIBLE);
          holder.unjoinButton.setVisibility(View.VISIBLE);
        } else {
          holder.joinButton.setVisibility(View.VISIBLE);
          holder.unjoinButton.setVisibility(View.INVISIBLE);
        }
        holder.joinButton.setOnClickListener(v -> onClick_join((MaterialButton) v, listing));
        holder.unjoinButton.setOnClickListener(v -> onClick_join((MaterialButton) v, listing));
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
    adapter.startListening();
    mRecyclerView.setAdapter(adapter);

  }

  private String getStartEndTimeDisplay(long startTime, long endTime) {
    Calendar startTimeDate = Calendar.getInstance(TimeZone.getTimeZone("Asia/Singapore"));
    Calendar endTimeDate = Calendar.getInstance(TimeZone.getTimeZone("Asia/Singapore"));
    startTimeDate.setTimeInMillis(startTime);
    endTimeDate.setTimeInMillis(startTime);
    boolean sameDate = ListingUtils.isSameDate(startTime, endTime);
    boolean isToday = ListingUtils.isSameDate(System.currentTimeMillis(), startTime);
    boolean withinWeekCurToStart = Math.abs(ListingUtils.getDayTimestamp(startTime) - ListingUtils
        .getDayTimestamp(System.currentTimeMillis())) < 1000 * 60 * 60 * 24 * 7; // 1 week
    boolean withinWeekCurToEnd = Math.abs(ListingUtils.getDayTimestamp(endTime) - ListingUtils
        .getDayTimestamp(System.currentTimeMillis())) < 1000 * 60 * 60 * 24 * 7; // 1 week
    boolean withinWeekStartToEnd = ListingUtils.getDayTimestamp(endTime) - ListingUtils
        .getDayTimestamp(startTime) < 1000 * 60 * 60 * 24 * 7; // 1 week
    boolean endsToday = ListingUtils.isSameDate(System.currentTimeMillis(), endTime);
    String startDateDisplay;
    DateFormat df = android.text.format.DateFormat.getDateFormat(ListingsFragments.this
        .getActivity(ListingsFragments.this));
    df.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
    DateFormat dfTime = android.text.format.DateFormat.getTimeFormat(ListingsFragments
        .this.getActivity(ListingsFragments.this));
    dfTime.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
    long daysTillStart = ListingUtils.daysApart(startTime, System.currentTimeMillis());
    long daysTillEnd = ListingUtils.daysApart(endTime, System.currentTimeMillis());
    long daysDuration = ListingUtils.daysApart(endTime, startTime);
    String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    if (isToday) {
      startDateDisplay = "Today";
    } else if (daysTillStart == 1) {
      startDateDisplay = "Tomorrow";
    } else if (daysTillStart == -1) {
      startDateDisplay = "Yesterday";
    } else if (withinWeekCurToStart) {
      startDateDisplay = (startTime < System.currentTimeMillis() ? "Last " : "This ") +
          days[startTimeDate.get(Calendar.DAY_OF_WEEK) - 1];
    } else {
      startDateDisplay = df.format(new Date(startTime)) + ", " +
          days[startTimeDate.get(Calendar.DAY_OF_WEEK) - 1];
    }
    String endDateDisplay;

    if (sameDate) {
      endDateDisplay = "";
    } else if (endsToday) {
      endDateDisplay = "Today";
    } else if (daysTillEnd == 1) {
      endDateDisplay = "Tomorrow";
    } else if (daysDuration == 1) {
      endDateDisplay = "The day after,";
    } else if (withinWeekStartToEnd && withinWeekCurToStart) {
      endDateDisplay = days[endTimeDate.get(Calendar.DAY_OF_WEEK)];
    } else if (withinWeekCurToEnd) {
      endDateDisplay = (endTime < System.currentTimeMillis() ? "Last " : "This ") +
          days[endTimeDate.get(Calendar.DAY_OF_WEEK) - 1];
    } else {
      endDateDisplay = df.format(new Date(endTime)) + ", " + days[endTimeDate.get(Calendar
          .DAY_OF_WEEK) - 1];
    }
    String startTimeDisplay = dfTime.format(new Date(startTime));
    String endTimeDisplay = dfTime.format(new Date(endTime));
    StringBuilder sb = new StringBuilder(startDateDisplay);
    return sb.append(" ")
        .append(startTimeDisplay)
        .append(" to \n")
        .append(endDateDisplay)
        .append(endDateDisplay.isEmpty() ? "" : " ")
        .append(endTimeDisplay)
        .toString();
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
