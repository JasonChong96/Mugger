package com.bojio.mugger.listings;

import android.support.constraint.ConstraintLayout;
import android.support.design.button.MaterialButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bojio.mugger.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListingsViewHolder extends RecyclerView.ViewHolder {
  public TextView moduleCode;
  public TextView dateTime;
  public TextView venue;
  public TextView numAttendees;
  public CardView cardView;
  public TextView nameView;
  public ImageView colorCode;
  public View view;

  @BindView(R.id.num_attendees_click_view)
  public View numAttendeesClickView;

  @BindView(R.id.expand_click_view)
  public View expandClickView;

  @BindView(R.id.listing_view_expand_image)
  public ImageView expandImage;

  @BindView(R.id.listing_view_expand_layout)
  public ConstraintLayout expandLayout;

  @BindView(R.id.listing_view_description)
  public TextView descriptionView;

  @BindView(R.id.listing_view_expanded_layout)
  public ConstraintLayout expandedLayout;

  @BindView(R.id.listing_view_expanded_layout2)
  public ConstraintLayout expandedLayout2;

  @BindView(R.id.listing_view_creator_controls_layout)
  public ConstraintLayout creatorControlsLayout;

  @BindView(R.id.listing_view_button_chat)
  public MaterialButton chatButton;

  @BindView(R.id.listing_view_button_delete)
  public MaterialButton deleteButton;

  @BindView(R.id.listing_view_button_edit)
  public MaterialButton editButton;

  @BindView(R.id.listing_view_button_join)
  public MaterialButton joinButton;

  @BindView(R.id.listing_view_button_unjoin)
  public MaterialButton unjoinButton;

  @BindView(R.id.listing_view_button_report)
  public MaterialButton reportButton;

  private boolean expanded;

  public ListingsViewHolder(View view) {
    super(view);
    ButterKnife.bind(this, view);
    moduleCode = view.findViewById(R.id.module_code);
    dateTime = view.findViewById(R.id.date_time);
    venue = view.findViewById(R.id.venue);
    numAttendees = view.findViewById(R.id.num_attendees);
    cardView = view.findViewById(R.id.card_view_listing);
    nameView = view.findViewById(R.id.owner_name);
    colorCode = view.findViewById(R.id.color_code_drawable);
    this.view = view;
  }

  public boolean isExpanded() {
    return expanded;
  }

  public void toggleExpanded() {
    this.expanded ^= true;
  }
}