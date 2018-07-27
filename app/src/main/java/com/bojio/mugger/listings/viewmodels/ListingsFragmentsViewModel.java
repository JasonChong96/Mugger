package com.bojio.mugger.listings.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import com.annimon.stream.function.Predicate;
import com.bojio.mugger.listings.Listing;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public abstract class ListingsFragmentsViewModel extends AndroidViewModel {
  protected FirebaseAuth mAuth;
  protected FirebaseFirestore db;
  protected Query mQuery;
  protected Predicate<Listing> predicate;

  public ListingsFragmentsViewModel(@NonNull Application application, Query mQuery) {
    super(application);
    this.mAuth = FirebaseAuth.getInstance();
    this.db = FirebaseFirestore.getInstance();
    this.mQuery = mQuery;
  }

  public void init() {
  }

  public Query getQuery() {
    return mQuery;
  }

  public Predicate<Listing> getPredicate() {
    return predicate;
  }
}
