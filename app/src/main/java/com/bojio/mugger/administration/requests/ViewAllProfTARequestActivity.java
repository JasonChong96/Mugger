package com.bojio.mugger.administration.requests;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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

public class ViewAllProfTARequestActivity extends LoggedInActivity {
  FirebaseFirestore db;

  @BindView(R.id.profta_request_view_empty_text)
  TextView emptyTextView;

  @BindView(R.id.profta_request_view_recycler)
  RecyclerView recyclerView;

  @BindView(android.R.id.content)
  View activityView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    db = FirebaseFirestore.getInstance();
    super.onCreate(savedInstanceState);
    if (stopActivity) {  finish();
      return;
    }
    setContentView(R.layout.activity_view_all_prof_tarequest);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    ButterKnife.bind(this);
    initRecycler();
  }

  private void initRecycler() {
    Query mQuery = db.collection("requestsProfTA").orderBy("time", Query.Direction.DESCENDING);
    FirestoreRecyclerOptions<ProfTARequest> options = new FirestoreRecyclerOptions.Builder<ProfTARequest>()
        .setQuery(mQuery, ProfTARequest::getRequestFromSnapshot).build();
    FirestoreRecyclerAdapter adapter = new FirestoreRecyclerAdapter<ProfTARequest,
        ProfTARequestViewHolder>(options) {
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
      public ProfTARequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.profta_request_view, parent, false);
        return new ProfTARequestViewHolder(view);
      }

      @Override
      protected void onBindViewHolder(@NonNull ProfTARequestViewHolder holder, int position,
                                      @NonNull ProfTARequest request) {
        holder.nameView.setText(request.getUserName());
        holder.descriptionView.setText(request.getDescription());
        StringBuilder moduleCode = new StringBuilder();
        moduleCode.append(request.getModuleCode()).append(" ").append(request.getRole());
        holder.moduleCodeView.setText(moduleCode.toString());
        holder.profileButton.setOnClickListener(view -> {
          Intent intent = new Intent(ViewAllProfTARequestActivity.this, ProfileActivity.class);
          Bundle b = new Bundle();
          b.putString("profileUid", request.getUserUid());
          intent.putExtras(b);
          startActivity(intent);
        });
        holder.deleteButton.setOnClickListener(view -> {
          new MaterialDialog.Builder(ViewAllProfTARequestActivity.this).title("Confirmation").content
              ("Are you sure you want to delete this feedback?").positiveText("Yes").negativeText("No")
              .onPositive((dialog, which) -> {
                AlertDialog dialogg = new SpotsDialog
                    .Builder()
                    .setContext(ViewAllProfTARequestActivity.this)
                    .setMessage("Deleting Feedback...")
                    .setCancelable(false)
                    .build();
                dialogg.show();
                request.getDocRef().delete().addOnCompleteListener(task -> {
                  dialogg.dismiss();
                  if (!task.isSuccessful()) {
                    Snackbar.make(activityView, "Error deleting request, please try again " +
                        "later", Snackbar.LENGTH_SHORT).show();
                  } else {
                    Snackbar.make(activityView, "Successfully deleted request.",
                        Snackbar.LENGTH_SHORT).show();
                  }
                });
              }).show();
        });
      }
    };
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
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

  class ProfTARequestViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.prof_ta_request_view_module_code)
    TextView moduleCodeView;

    @BindView(R.id.prof_ta_request_view_name)
    TextView nameView;

    @BindView(R.id.prof_ta_request_view_description)
    TextView descriptionView;

    @BindView(R.id.prof_ta_request_view_button_delete)
    Button deleteButton;

    @BindView(R.id.prof_ta_request_view_button_profile)
    Button profileButton;

    View view;

    public ProfTARequestViewHolder(View itemView) {
      super(itemView);
      this.view = itemView;
      ButterKnife.bind(this, itemView);
    }
  }
}
