package com.bojio.mugger.listings;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bojio.mugger.R;

public class ListingsViewHolder extends RecyclerView.ViewHolder {
  public TextView moduleCode;
  public TextView dateTime;
  public TextView venue;
  public TextView numAttendees;
  public CardView cardView;
  public TextView nameView;
  public View view;

  public ListingsViewHolder(View view) {
    super(view);
    moduleCode = view.findViewById(R.id.module_code);
    dateTime = view.findViewById(R.id.date_time);
    venue = view.findViewById(R.id.venue);
    numAttendees = view.findViewById(R.id.num_attendees);
    cardView = view.findViewById(R.id.card_view_listing);
    nameView = view.findViewById(R.id.owner_name);
    this.view = view;
  }
}