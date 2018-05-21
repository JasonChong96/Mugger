package com.bojio.mugger.listings;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bojio.mugger.R;

public class ListingsViewHolder extends RecyclerView.ViewHolder {
  public TextView moduleCode;
  public TextView dateTime;
  public TextView venue;
  public View view;

  public ListingsViewHolder(View view) {
    super(view);
    moduleCode = view.findViewById(R.id.module_code);
    dateTime = view.findViewById(R.id.date_time);
    venue = view.findViewById(R.id.venue);
    this.view = view;
  }
}