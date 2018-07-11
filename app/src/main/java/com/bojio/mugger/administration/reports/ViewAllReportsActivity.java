package com.bojio.mugger.administration.reports;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bojio.mugger.R;
import com.bojio.mugger.authentication.LoggedInActivity;
import com.bojio.mugger.database.MuggerDatabase;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.DateFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ViewAllReportsActivity extends LoggedInActivity {
  FirebaseFirestore db;

  @BindView(R.id.view_all_reports_recycler)
  RecyclerView recyclerView;

  @BindView(R.id.view_all_reports_empty_text)
  TextView emptyTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    db = FirebaseFirestore.getInstance();
    super.onCreate(savedInstanceState);
    if (stopActivity) {  finish();
      return;
    }
    setContentView(R.layout.activity_view_all_reports);
    ButterKnife.bind(this);
    initRecycler();
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  private void initRecycler() {
    Query mQuery = MuggerDatabase.getAllReportsReference(db).orderBy("time", Query.Direction
        .DESCENDING);
    FirestoreRecyclerOptions<Report> options = new FirestoreRecyclerOptions.Builder<Report>()
        .setQuery(mQuery, Report::getReportFromSnapshot).build();
    FirestoreRecyclerAdapter<Report, ReportViewHolder> adapter = new FirestoreRecyclerAdapter<Report, ReportViewHolder>(options) {
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
      public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.report_view, parent, false);
        return new ReportViewHolder(view);
      }

      @Override
      protected void onBindViewHolder(@NonNull ReportViewHolder holder, int position, @NonNull
          Report report) {
        holder.reportedView.setText(report.getReportedName());
        holder.reporterView.setText(String.format("Reported by %s", report.getReporterName()));
        holder.typeView.setText(report.getType().name());
        DateFormat df = android.text.format.DateFormat.getDateFormat(ViewAllReportsActivity.this);
        DateFormat dfTime = android.text.format.DateFormat.getTimeFormat(ViewAllReportsActivity
            .this);
        holder.timeView.setText(String.format("%s %s", df.format(report.getTime()),
            dfTime.format(report.getTime())));
        holder.view.setClickable(true);
        holder.view.setFocusable(true);
        holder.view.setOnClickListener(report.getOnClickListener());
      }
    };
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.setAdapter(adapter);
    adapter.startListening();
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

  class ReportViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.report_view_type)
    TextView typeView;

    @BindView(R.id.report_view_reported_name)
    TextView reportedView;

    @BindView(R.id.report_view_reporter_name)
    TextView reporterView;

    @BindView(R.id.report_view_time)
    TextView timeView;

    View view;

    public ReportViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
      this.view = itemView;
    }
  }
}
