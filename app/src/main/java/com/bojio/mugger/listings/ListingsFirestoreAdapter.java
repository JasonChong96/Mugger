package com.bojio.mugger.listings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Predicate;
import com.bojio.mugger.R;
import com.bojio.mugger.administration.reports.MakeReportActivity;
import com.bojio.mugger.administration.reports.Report;
import com.bojio.mugger.authentication.MuggerRole;
import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.constants.ModuleRole;
import com.bojio.mugger.database.MuggerDatabase;
import com.bojio.mugger.fcm.MessagingService;
import com.bojio.mugger.listings.chat.ListingChatActivity;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.Nullable;

import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;

public class ListingsFirestoreAdapter extends FirestoreRecyclerAdapter<Listing,
    ListingsViewHolder> {
  /** The predicate used to test if listings are to be filtered, false = filtered */
  private final Predicate<Listing> predicateFilter;

  /** The DateFormat used to format the time */
  private final DateFormat dfTime;

  /** The DateFormat used to format the date */
  private final DateFormat df;

  /** The FirebaseAuth instance */
  private FirebaseAuth mAuth;

  /** The context containing the RecyclerView */
  private Context context;

  /** The Firestore database instance */
  private FirebaseFirestore db;

  /** The unique id of the user */
  private String uid;

  /** The FirebaseMessaging instance */
  private FirebaseMessaging fcm;

  /** A hashset of filtered items */
  private HashSet<Listing> filtered;

  /** The TextView to show to indicate to the user that there are no listings to be shown */
  private TextView emptyTextView;

  /** Listeners for changes in listing data */
  private List<ListenerRegistration> listeners;

  /**
   * Default constructor for the adapter.
   * @param options The FirestoreRecyclerOptions to be used
   * @param activity the activity that contains the RecyclerView
   * @param mAuth the FirebaseAuth instance
   * @param db the Firestore database instance
   * @param fcm the FirebaseMessaging instance
   * @param predicateFilter the predicate to use to filter listings
   * @param emptyTextView the TextView to show when there are no listings to show
   */
  public ListingsFirestoreAdapter(FirestoreRecyclerOptions<Listing> options, Activity activity,
                                  FirebaseAuth mAuth, FirebaseFirestore
                                      db, FirebaseMessaging fcm, Predicate<Listing>
                                      predicateFilter, TextView emptyTextView) {
    super(options);
    this.context = activity;
    this.mAuth = mAuth;
    this.db = db;
    this.uid = mAuth.getUid();
    this.fcm = fcm;
    this.predicateFilter = predicateFilter;
    this.emptyTextView = emptyTextView;
    listeners = new LinkedList<>();
    filtered = new HashSet<>();
    df = android.text.format.DateFormat.getDateFormat(context);
    df.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
    dfTime = android.text.format.DateFormat.getTimeFormat(context);
    dfTime.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
  }

  @Override
  public void onDataChanged() {
    if (getUnfilteredItemCount() == 0) {
      emptyTextView.setVisibility(View.VISIBLE);
    } else if (emptyTextView.getVisibility() == View.VISIBLE) {
      emptyTextView.setVisibility(View.GONE);
    }
  }

  /**
   * Gets the number of items that are not filtered.
   * @return the number of unfiltered items
   */
  public int getUnfilteredItemCount() {
    return this.getItemCount() - filtered.size();
  }

  @Override
  public void onChildChanged(@NonNull ChangeEventType type, @NonNull DocumentSnapshot snapshot,
                             int newIndex, int oldIndex) {
    if (type == ChangeEventType.REMOVED) {
      filtered.remove(Listing.getListingFromSnapshot(snapshot));
    } else if (type == ChangeEventType.CHANGED) {
      return;
    }
    super.onChildChanged(type, snapshot, newIndex, oldIndex);
  }

  /**
   * Called when the user clicks on the report button, Opens the UI to report the listing.
   * @param listing the listing to report
   */
  private void onClick_report(Listing listing) {
    Intent intent = new Intent(context, MakeReportActivity.class);
    Bundle b = new Bundle();
    b.putParcelable("listing", listing);
    b.putString("reportType", Report.ReportType.LISTING.name());
    b.putString("listingUid", listing.getUid());
    intent.putExtras(b);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
  }

  /**
   * Called when the user clicks on the chat button, Opens the listing chat UI.
   * @param listing the listing
   */
  private void onClick_chat(Listing listing) {
    Intent intent = new Intent(context, ListingChatActivity.class);
    Bundle b = new Bundle();
    b.putParcelable("listing", listing);
    intent.putExtras(b);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
  }

  /**
   * Called when the user clicks on the delete button. Opens a confirmation dialog to confirm if
   * the user would like to delete the listing.
   * @param listing the listing to be deleted
   */
  private void onClick_delete(Listing listing) {
    new MaterialDialog.Builder(context).title("Confirmation").content
        ("Are you sure you want to delete this listing?").positiveText("Yes").negativeText("No")
        .onPositive((dialog, which) -> {
          AlertDialog dialogg = new SpotsDialog
              .Builder()
              .setContext(context)
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
              Toasty.error(context, "Failed to delete listing, please try again later")
                  .show();
            }
          });
        }).show();
  }

  /**
   * Called when the user clicks on the edit button. Opens the edit listing UI.
   * @param listing the listing to be editted
   */
  private void onClick_edit(Listing listing) {
    Intent intent = new Intent(context, CreateEditListingActivity.class);
    Bundle b = new Bundle();
    b.putParcelable("listing", listing);
    intent.putExtras(b);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
  }

  /**
   * Called when the user clicks on the join button. Adds the user to the attendees list and
   * suscribed him/her to the notifications from it.
   * @param button the button clicked
   * @param listing the listing to join
   */
  private void onClick_join(MaterialButton button, Listing listing) {
    DocumentReference listingRef = MuggerDatabase.getAllListingsReference(db).document(listing.getUid());
    Map<String, Object> updates = new HashMap<>();
    if (listing.isAttending(uid)) {
      if (mAuth.getUid().equals(listing.getOwnerId())) {
        Toasty.error(context, "You must be attending listings that you own.").show();
      } else {
        updates.put(mAuth.getUid(), FieldValue.delete());
        fcm.unsubscribeFromTopic(listing.getUid());
        listingRef.update(updates);
      }
    } else {
      if (listing.getNumAttendees() > 19) {
        Toasty.error(context, "There are too many people attending this listing").show();
      } else {
        updates.put(mAuth.getUid(), listing.getStartTime());
        fcm.subscribeToTopic(listing.getUid());
        listingRef.update(updates);
      }
    }
  }

  @Override
  public void onBindViewHolder(ListingsViewHolder holder, int position, Listing originalListing) {
    if (mAuth.getCurrentUser() == null) {
      return;
    }
    listeners.add(MuggerDatabase.getListingReference(db, originalListing.getUid())
        .addSnapshotListener(
        (snapshot,
                                                                                        e) -> {
      final Listing listing = Listing.getListingFromSnapshot(snapshot);
      int type = listing.getType();
      String title = listing.getModuleCode();
      if (predicateFilter != null && !predicateFilter.test(listing)) {
        holder.cardView.setVisibility(View.GONE);
        filtered.add(listing);
        onDataChanged();
        return;
      } else {
        if (holder.cardView.getVisibility() == View.GONE) {
          holder.cardView.setVisibility(View.VISIBLE);
        }
        filtered.remove(listing);
      }

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
      holder.reportButton.setOnClickListener(v -> onClick_report(listing));
      holder.editButton.setOnClickListener(v -> onClick_edit(listing));
      updateView(title, holder.moduleCode);
      updateView(listing.getVenue(), holder.venue);
      updateView(listing.getDescription(), holder.descriptionView);
      holder.dateTime.setText(ListingUtils.getStartEndTimeDisplay(listing.getStartTime(), listing
          .getEndTime(), df, dfTime));
      holder.numAttendees.setText(String.format(Locale.getDefault(), "%d", listing.getNumAttendees
          ()));
      holder.numAttendeesClickView.setOnClickListener(v -> {
        Intent intent = new Intent(v.getContext(), ViewAttendeesActivity.class);
        Bundle b = new Bundle();
        b.putStringArrayList("profiles", (ArrayList<String>) listing.getAttendees());
        b.putString("ownerUid", listing.getOwnerId());
        intent.putExtras(b);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
      });
      if (listing.getOwnerId().equals(mAuth.getUid())) {
        holder.joinButton.setVisibility(View.INVISIBLE);
        holder.unjoinButton.setVisibility(View.INVISIBLE);
      } else if (listing.isAttending(uid)) {
        holder.joinButton.setVisibility(View.INVISIBLE);
        holder.unjoinButton.setVisibility(View.VISIBLE);
        holder.unjoinButton.setOnClickListener(v -> onClick_join((MaterialButton) v, listing));
      } else {
        holder.joinButton.setVisibility(View.VISIBLE);
        holder.joinButton.setOnClickListener(v -> onClick_join((MaterialButton) v, listing));
        holder.unjoinButton.setVisibility(View.INVISIBLE);
      }
    }));
    holder.nameView.setText(String.format("By %s", originalListing.getOwnerName()));
    holder.expandClickView.setOnClickListener(v -> {
      holder.expandLayout.callOnClick();
    });
    holder.expandLayout.setOnClickListener(view -> {
      if (!holder.isExpanded()) {
        TransitionManager.beginDelayedTransition(holder.cardView);
        holder.expandedLayout.setVisibility(View.VISIBLE);
        holder.expandedLayout2.setVisibility(View.VISIBLE);
        if (uid.equals(originalListing.getOwnerId()) || MuggerRole.MODERATOR
            .check(MuggerUserCache.getInstance().getRole())) {
          holder.creatorControlsLayout.setVisibility(View.VISIBLE);
        }
        holder.expandImage.setImageDrawable(context.getResources().getDrawable(R.drawable
            .ic_baseline_expand_less_24px));
      } else {
        holder.expandedLayout.setVisibility(View.GONE);
        holder.creatorControlsLayout.setVisibility(View.GONE);
        holder.expandedLayout2.setVisibility(View.GONE);
        holder.expandImage.setImageDrawable(context.getResources().getDrawable(R.drawable
            .ic_baseline_expand_more_24px));
      }
      holder.toggleExpanded();
    });
    holder.chatButton.setOnClickListener(v -> onClick_chat(originalListing));
    holder.deleteButton.setOnClickListener(v -> onClick_delete(originalListing));
  }


  @NonNull
  @Override
  public ListingsViewHolder onCreateViewHolder(ViewGroup group, int i) {
    // Create a new instance of the ViewHolder, in this case we are using a custom
    // layout called R.layout.message for each item
    View view = LayoutInflater.from(group.getContext())
        .inflate(R.layout.listing_card_view, group, false);
    return new ListingsViewHolder(view);
  }

  /**
   * Updates the view with the new String if it is different from the currently shown view.
   * @param newValue the new String
   * @param view the TextView to update
   */
  public static void updateView(String newValue, TextView view) {
    if (!view.getText().toString().equals(newValue)) {
      view.setText(newValue);
    }
  }

  @Override
  public void stopListening() {
    Stream.of(listeners).forEach(ListenerRegistration::remove);
    super.stopListening();
  }
}
