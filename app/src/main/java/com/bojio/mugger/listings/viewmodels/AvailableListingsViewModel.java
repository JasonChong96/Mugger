package com.bojio.mugger.listings.viewmodels;

import android.app.Application;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.bojio.mugger.database.MuggerDatabase;
import com.bojio.mugger.listings.ListingUtils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import javax.annotation.Nullable;

public class AvailableListingsViewModel extends ListingsFragmentsViewModel {
  private MutableLiveData<Boolean> showUnrelatedModules;

  public AvailableListingsViewModel(@NonNull Application application) {
    super(application, ListingUtils
        .getAvailableListingsQuery(FirebaseFirestore.getInstance())
        .orderBy("startTime"));
    showUnrelatedModules = new MutableLiveData<>();
    showUnrelatedModules.setValue(false);
    init();
  }

  @Override
  public void init() {
    super.init();
    MuggerDatabase.getUserReference(db, mAuth.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
      @Override
      public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
        Long newValue = documentSnapshot.getLong("showUnrelatedModules");
        Boolean newValueBoolean = newValue != null && newValue != 0;
        if (!newValueBoolean.equals(showUnrelatedModules.getValue())) {
          showUnrelatedModules.postValue(newValueBoolean);
        }
      }
    });
  }

  public void selectionChanged(boolean allModules, String selected) {
    mQuery = ListingUtils.getAvailableListingsQuery(db);
    if (!allModules) {
      mQuery = mQuery.orderBy(selected, Query.Direction.ASCENDING);
    }
  }

  public MutableLiveData<Boolean> getShowUnrelatedModules() {
    return showUnrelatedModules;
  }
}
