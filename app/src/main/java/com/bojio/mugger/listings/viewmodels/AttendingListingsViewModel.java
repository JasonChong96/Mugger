package com.bojio.mugger.listings.viewmodels;

import android.app.Application;
import android.support.annotation.NonNull;

import com.bojio.mugger.listings.ListingUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AttendingListingsViewModel extends ListingsFragmentsViewModel {
  public AttendingListingsViewModel(@NonNull Application application) {
    super(application, ListingUtils.getAttendingListingsQuery(FirebaseFirestore.getInstance(),
        FirebaseAuth.getInstance().getUid()));
  }
}
