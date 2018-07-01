package com.bojio.mugger.administration.feedback;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bojio.mugger.R;
import com.bojio.mugger.authentication.LoggedInActivity;
import com.bojio.mugger.profile.ProfileActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;

public class ViewAllFeedbackActivity extends LoggedInActivity {
  @BindView(android.R.id.content)
  View activityView;
  @BindView(R.id.feedback_view_recycler)
  RecyclerView recyclerView;
  @BindView(R.id.feedback_view_empty_text)
  TextView emptyTextView;
  private FirebaseFirestore db;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    db = FirebaseFirestore.getInstance();
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_view_all_feedback);
    ButterKnife.bind(this);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    initRecycler();
  }

  private void initRecycler() {
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    Query mQuery = db.collection("feedback").orderBy("time", Query.Direction.DESCENDING);
    FirestoreRecyclerOptions<Feedback> options = new FirestoreRecyclerOptions.Builder<Feedback>()
        .setQuery(mQuery, Feedback::getFeedbackFromSnapshot).build();
    FirestoreRecyclerAdapter adapter = new FirestoreRecyclerAdapter<Feedback, FeedbackViewHolder>
        (options) {
      @Override
      public void onDataChanged() {
        if (this.getItemCount() == 0) {
          emptyTextView.setVisibility(View.VISIBLE);
        } else if (emptyTextView.getVisibility() == View.VISIBLE) {
          emptyTextView.setVisibility(View.GONE);
        }
      }

      @NonNull
      @Override
      public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.feedback_view, parent, false);
        return new FeedbackViewHolder(view);
      }

      @Override
      protected void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position, @NonNull
          Feedback feedback) {
        holder.titleView.setText(feedback.getTitle());
        holder.descriptionView.setText(feedback.getDescription());
        StringBuilder nameTime = new StringBuilder("Posted By ");
        nameTime.append(feedback.getUserName());
        holder.nameTimeView.setText(nameTime.toString());
        holder.profileButton.setOnClickListener(view -> {
          Intent intent = new Intent(ViewAllFeedbackActivity.this, ProfileActivity.class);
          Bundle b = new Bundle();
          b.putString("profileUid", feedback.getUserUid());
          intent.putExtras(b);
          startActivity(intent);
        });
        holder.deleteButton.setOnClickListener(view -> {
          new MaterialDialog.Builder(ViewAllFeedbackActivity.this).title("Confirmation").content
              ("Are you sure you want to delete this feedback?").positiveText("Yes").negativeText("No")
              .onPositive((dialog, which) -> {
                AlertDialog dialogg = new SpotsDialog
                    .Builder()
                    .setContext(ViewAllFeedbackActivity.this)
                    .setMessage("Deleting Feedback...")
                    .setCancelable(false)
                    .setTheme(R.style.SpotsDialog)
                    .build();
                dialogg.show();
                feedback.getDocRef().delete().addOnCompleteListener(task -> {
                  dialogg.dismiss();
                  if (!task.isSuccessful()) {
                    Snackbar.make(activityView, "Error deleting feedback, please try again " +
                        "later", Snackbar.LENGTH_SHORT).show();
                  } else {
                    Snackbar.make(activityView, "Successfully deleted feedback.",
                        Snackbar.LENGTH_SHORT).show();
                  }
                });
              }).show();
        });
      }
    };
    adapter.startListening();
    recyclerView.setAdapter(adapter);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      // When back button on the top left is clicked
      case android.R.id.home:
        onBackPressed();
        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  class FeedbackViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.feedback_title)
    TextView titleView;

    @BindView(R.id.feedback_description)
    TextView descriptionView;

    @BindView(R.id.feedback_delete_button)
    Button deleteButton;

    @BindView(R.id.feedback_profile_button)
    Button profileButton;

    @BindView(R.id.feedback_name_time)
    TextView nameTimeView;

    View view;

    FeedbackViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
      this.view = itemView;
    }
  }
}
