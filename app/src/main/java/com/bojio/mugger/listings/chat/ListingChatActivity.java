package com.bojio.mugger.listings.chat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bojio.mugger.R;
import com.bojio.mugger.fragments.ListingsFragments;
import com.bojio.mugger.listings.AvailableListingDetailsActivity;
import com.bojio.mugger.listings.Listing;
import com.bojio.mugger.listings.ListingsViewHolder;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ListingChatActivity extends AppCompatActivity {

  Listing listing;
  FirebaseFirestore db;
  FirebaseUser user;

  @BindView(R.id.messages)
  RecyclerView messages;

  @BindView(R.id.activity_thread_input_edit_text)
  EditText toSendView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    db = FirebaseFirestore.getInstance();
    setContentView(R.layout.activity_listing_chat);
    ButterKnife.bind(this);
    Bundle b = getIntent().getExtras();
    listing = b.getParcelable("listing");
    user = FirebaseAuth.getInstance().getCurrentUser();
    if (user == null) {
      finish();
    }
    initMessages();
  }

  @OnClick(R.id.activity_thread_send_fab)
  public void onClick() {
    long timestamp = System.currentTimeMillis();
    long dayTimestamp = getDayTimestamp(timestamp);
    String body = toSendView.getText().toString().trim();
    String userUid = user.getUid();
    Message message =
        new Message(userUid, user.getDisplayName(), body, timestamp, dayTimestamp);
    Map<String, Object> messageData = new HashMap<>();
    messageData.put("fromUid", userUid);
    messageData.put("fromName", user.getDisplayName());
    messageData.put("content", body);
    messageData.put("time", timestamp);
    messageData.put("day", dayTimestamp);
    /*mDatabase
        .child("notifications")
        .child("messages")
        .push()
        .setValue(message);*/
    db.collection("chats").document(listing.getUid()).collection("messages").add(messageData);
    /*if (!userUid.equals(ownerUid)) {
      mDatabase
          .child("messages")
          .child(ownerUid)
          .child(userUid)
          .push()
          .setValue(message);
    }*/
    toSendView.setText("");
  }

  private long getDayTimestamp(long timestamp) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(timestamp);
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MINUTE, 0);
    return calendar.getTimeInMillis();
  }

  private void initMessages() {
    Query mQuery = db.collection("chats").document(listing.getUid()).collection("messages")
        .orderBy("time", Query.Direction.DESCENDING);
    FirestoreRecyclerOptions<Message> options = new FirestoreRecyclerOptions.Builder<Message>()
        .setQuery(mQuery, snapshot -> new Message((String) snapshot.get("fromUid"),
            (String) snapshot.get("fromName"),
            (String) snapshot.get("content"),
            (Long) snapshot.get("time"),
            (Long) snapshot.get("day")))
        .build();
    FirestoreRecyclerAdapter adapter = new FirestoreRecyclerAdapter<Message, MessageViewHolder>
        (options) {
      private static final int VIEW_TYPE_SENT = 0;
      private static final int VIEW_TYPE_SENT_WITH_DATE = 1;
      private static final int VIEW_TYPE_RECEIVED = 2;
      private static final int VIEW_TYPE_RECEIVED_WITH_DATE = 3;

      @NonNull
      @Override
      public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
          case VIEW_TYPE_SENT:
          case VIEW_TYPE_SENT_WITH_DATE:
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent,
                parent, false);
            break;
          case VIEW_TYPE_RECEIVED:
          case VIEW_TYPE_RECEIVED_WITH_DATE:
          default:
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received,
                parent, false);
            break;
        }
        MessageViewHolder viewHolder = new MessageViewHolder(view);
        if (viewType == VIEW_TYPE_SENT || viewType == VIEW_TYPE_RECEIVED) {
          viewHolder.dateView.setVisibility(View.GONE);
        }
        return viewHolder;
      }

      @Override
      public void onBindViewHolder(MessageViewHolder holder, int position, Message message) {
        holder.contentView.setText(message.getContent());
        holder.senderView.setText(message.getFromName());
        DateFormat dfTime = android.text.format.DateFormat.getTimeFormat(ListingChatActivity
            .this);
        holder.timeView.setText(dfTime.format(new Date(message.getTime())));
        DateFormat dfDate = android.text.format.DateFormat.getDateFormat(ListingChatActivity
            .this);
        holder.dateView.setText(dfDate.format(new Date(message.getTime())));

      }

      @Override
      public int getItemViewType(int position) {
        Message message = getItem(position);
        if (message.getFromUid().equals(user.getUid())) {
          if (position == getItemCount() - 1 ||
              !Objects.equals(getItem(position + 1).getDay(), message.getDay())) {
            return VIEW_TYPE_SENT_WITH_DATE;
          } else {
            return VIEW_TYPE_SENT;
          }
        } else {
          if (position == getItemCount() - 1 ||
              !Objects.equals(getItem(position + 1).getDay(), message.getDay())) {
            return VIEW_TYPE_RECEIVED_WITH_DATE;
          } else {
            return VIEW_TYPE_RECEIVED;
          }
        }
      }
    };
    messages.setAdapter(adapter);
    LinearLayoutManager manager = new LinearLayoutManager(this);
    manager.setReverseLayout(true);
    messages.setLayoutManager(manager);
    adapter.startListening();
    messages.getAdapter().registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
      @Override
      public void onItemRangeInserted(int positionStart, int itemCount) {
        super.onItemRangeInserted(positionStart, itemCount);
        messages.smoothScrollToPosition(0);
      }
    });
  }

  class MessageViewHolder extends RecyclerView.ViewHolder {
    @BindView (R.id.item_message_content)
    TextView contentView;

    @BindView (R.id.item_message_sender)
    TextView senderView;

    @BindView (R.id.item_message_time)
    TextView timeView;

    @BindView (R.id.item_message_date_text_view)
    TextView dateView;

    View view;

    MessageViewHolder(View view) {
      super(view);
      ButterKnife.bind(this, view);
      this.view = view;
    }
  }
}
