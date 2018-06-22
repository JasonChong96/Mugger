package com.bojio.mugger.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bojio.mugger.R;
import com.bojio.mugger.listings.ViewAttendeesActivity;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileListRecyclerAdapter extends RecyclerView.Adapter<ProfileListRecyclerAdapter.ProfileListViewHolder> {
  private ArrayList<DocumentSnapshot> mCustomObjects;
  String ownerUid;


  public class ProfileListViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.profile_list_name)
    public TextView nameView;

    @BindView(R.id.profile_list_first_major)
    public TextView firstMajorView;

    @BindView(R.id.profile_list_second_major)
    public TextView secondMajorView;

    public ProfileListViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  public ProfileListRecyclerAdapter(ArrayList<DocumentSnapshot> arrayList, String ownerUid) {
    mCustomObjects = arrayList;
    this.ownerUid = ownerUid;
  }

  @Override
  public int getItemCount() {
    return mCustomObjects.size();
  }

  @Override
  public ProfileListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout
        .profile_list_view, parent, false);
    return new ProfileListViewHolder(view);
  }

  @Override
  public void onBindViewHolder(ProfileListViewHolder holder, int position) {
    DocumentSnapshot snapshot = mCustomObjects.get(position);

    Map<String, Object> data = snapshot.getData();
    holder.nameView.setText(String.format("%s%s", data.get("displayName"), snapshot.getId().equals(ownerUid)
        ? " (Listing Creator)" : ""));
    holder.firstMajorView.setText((String) data.get("firstMajor"));
    if (data.containsKey("secondMajor")) {
      holder.secondMajorView.setText((String) data.get("secondMajor"));
      holder.secondMajorView.setVisibility(View.VISIBLE);
    }
    holder.itemView.setOnClickListener(v -> {
      Intent intent = new Intent(v.getContext(), ProfileActivity.class);
      Bundle b = new Bundle();
      b.putString("profileUid", mCustomObjects.get(position).getId());
      intent.putExtras(b);
      v.getContext().startActivity(intent);
    });
  }

  @Override
  public void onAttachedToRecyclerView(RecyclerView recyclerView) {
    super.onAttachedToRecyclerView(recyclerView);
  }


  private OnEntryClickListener mOnEntryClickListener;

  public interface OnEntryClickListener {
    void onEntryClick(View view, int position);
  }

  public void setOnEntryClickListener(OnEntryClickListener onEntryClickListener) {
    mOnEntryClickListener = onEntryClickListener;
  }
}
