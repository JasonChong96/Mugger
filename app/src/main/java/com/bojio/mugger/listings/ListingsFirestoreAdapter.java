package com.bojio.mugger.listings;

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
import com.bojio.mugger.listings.chat.ListingChatActivity;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;

public class ListingsFirestoreAdapter extends FirestoreRecyclerAdapter<Listing,
    ListingsViewHolder> {
  private final Predicate<Listing> predicateFilter;
  private final DateFormat dfTime;
  private final DateFormat df;
  private FirebaseAuth mAuth;
  private Context context;
  private FirebaseFirestore db;
  private String uid;
  private FirebaseMessaging fcm;
  private HashSet<Listing> filtered;

  public ListingsFirestoreAdapter(FirestoreRecyclerOptions<Listing> options, Context context,
                                  FirebaseAuth mAuth, FirebaseFirestore
                                      db, FirebaseMessaging fcm, Predicate<Listing>
                                      predicateFilter) {
    super(options);
    this.context = context;
    this.mAuth = mAuth;
    this.db = db;
    this.uid = mAuth.getUid();
    this.fcm = fcm;
    this.predicateFilter = predicateFilter;
    filtered = new HashSet<>();
    df = android.text.format.DateFormat.getDateFormat(context);
    df.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
    dfTime = android.text.format.DateFormat.getTimeFormat(context);
    dfTime.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
  }

  @Override
  public void onDataChanged() {
    if (this.getItemCount() == filtered.size()) {
      Toasty.warning(context, "There are currently no listings matching the criteria.").show();
    }
  }

  @Override
  public void onChildChanged(@NonNull ChangeEventType type, @NonNull DocumentSnapshot snapshot,
                             int newIndex, int oldIndex) {
    if (type == ChangeEventType.REMOVED) {
      filtered.remove(Listing.getListingFromSnapshot(snapshot));
    }
    super.onChildChanged(type, snapshot, newIndex, oldIndex);
  }

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

  private void onClick_chat(Listing listing) {
    Intent intent = new Intent(context, ListingChatActivity.class);
    Bundle b = new Bundle();
    b.putParcelable("listing", listing);
    intent.putExtras(b);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
  }

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

  private void onClick_edit(Listing listing) {
    Intent intent = new Intent(context, CreateEditListingActivity.class);
    Bundle b = new Bundle();
    b.putParcelable("listing", listing);
    intent.putExtras(b);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
  }

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
  public void onBindViewHolder(ListingsViewHolder holder, int position, Listing listing) {
    if (mAuth.getCurrentUser() == null) {
      return;
    }
    if (predicateFilter != null && !predicateFilter.test(listing)) {
      holder.cardView.setVisibility(View.GONE);
      filtered.add(listing);
      onDataChanged();
      return;
    } else {
      filtered.remove(listing);
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
    holder.dateTime.setText(ListingUtils.getStartEndTimeDisplay(listing.getStartTime(), listing
        .getEndTime(), df, dfTime));
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
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(intent);
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
}
