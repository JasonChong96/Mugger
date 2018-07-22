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

  @BindView(R.id.module_code)
  public TextView moduleCode;

  @BindView(R.id.date_time)
  public TextView dateTime;

  @BindView(R.id.venue)
  public TextView venue;

  @BindView(R.id.num_attendees)
  public TextView numAttendees;

  @BindView(R.id.card_view_listing)
  public CardView cardView;

  @BindView(R.id.owner_name)
  public TextView nameView;

  @BindView(R.id.color_code_drawable)
  public ImageView colorCode;

  public View view;

  private boolean expanded;

  public ListingsViewHolder(View view) {
    super(view);
    ButterKnife.bind(this, view);
    this.view = view;
  }

  public boolean isExpanded() {
    return expanded;
  }

  public void toggleExpanded() {
    this.expanded ^= true;
  }
}