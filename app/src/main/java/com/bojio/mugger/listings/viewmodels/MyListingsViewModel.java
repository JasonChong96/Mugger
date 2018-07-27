package com.bojio.mugger.listings.viewmodels;

import android.app.Application;
import android.support.annotation.NonNull;

import com.bojio.mugger.listings.ListingUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MyListingsViewModel extends ListingsFragmentsViewModel {
  public MyListingsViewModel(@NonNull Application application) {
    super(application, ListingUtils.getMyListingsQuery(FirebaseFirestore.getInstance(),
        FirebaseAuth.getInstance().getUid()));
  }
}
