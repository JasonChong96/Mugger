package com.bojio.mugger.profile;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileViewModel extends ViewModel {
  private FirebaseFirestore db;
  private FirebaseAuth mAuth;
  private MutableLiveData<String> displayName;
  private MutableLiveData<String> email;
  private MutableLiveData<String> faculty;

  public ProfileViewModel() {
    super();
    db = FirebaseFirestore.getInstance();
    mAuth = FirebaseAuth.getInstance();
  }


}
