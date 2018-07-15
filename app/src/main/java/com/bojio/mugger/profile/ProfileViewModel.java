package com.bojio.mugger.profile;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import com.bojio.mugger.database.MuggerDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class ProfileViewModel extends ViewModel {
  private MutableLiveData<String> firstMajor;
  private MutableLiveData<String> secondMajor;
  private FirebaseFirestore db;
  private FirebaseAuth mAuth;
  private MutableLiveData<String> displayName;
  private MutableLiveData<String> email;
  private MutableLiveData<String> faculty;
  private ListenerRegistration profileListener;
  private MutableLiveData<List<String>> semesters;
  private MutableLiveData<List<String>> modulesBySem;
  private MutableLiveData<List<String>> modulesBySem_ta;
  private MutableLiveData<List<String>> modulesBySem_prof;

  public ProfileViewModel() {
    super();
    db = FirebaseFirestore.getInstance();
    mAuth = FirebaseAuth.getInstance();
    displayName = new MutableLiveData<>();
    email = new MutableLiveData<>();
    faculty = new MutableLiveData<>();
    firstMajor = new MutableLiveData<>();
    secondMajor = new MutableLiveData<>();
    semesters = new MutableLiveData<>();
    modulesBySem = new MutableLiveData<>();
    modulesBySem_ta = new MutableLiveData<>();
    modulesBySem_prof = new MutableLiveData<>();
  }

  private void init() {
    registerProfileListener();
    registerModulesListener();
  }

  private void registerModulesListener() {

  }

  private void registerProfileListener() {
    profileListener =
        MuggerDatabase.getUserReference(db, mAuth.getUid()).addSnapshotListener((documentSnapshot, e) -> {
          String newEmail = documentSnapshot.getString("email");
          if (newEmail != null && !newEmail.equals(email.getValue())) {
            email.postValue(newEmail);
          }
          String newFaculty = documentSnapshot.getString("faculty");
          if (newFaculty != null && !newFaculty.equals(faculty.getValue())) {
            faculty.postValue(newFaculty);
          }
          String newName = documentSnapshot.getString("displayName");
          if (newName == null) {
            newName = "";
          }
          Long muted = documentSnapshot.getLong("muted");
          if (muted != null && muted > System.currentTimeMillis()) {
            newName = String.format("%s (Muted)", newName);
          }
          if (!newName.equals(displayName.getValue())) {
            displayName.postValue(newName);
          }
          String newFirstMajor = documentSnapshot.getString("firstMajor");
          if (newFirstMajor != null && !newFirstMajor.equals(firstMajor.getValue())) {
            firstMajor.postValue(newFirstMajor);
          }
          String newSecondMajor = documentSnapshot.getString("secondMajor");
          if (newSecondMajor != null && !newSecondMajor.equals(secondMajor.getValue())) {
            secondMajor.postValue(newSecondMajor);
          }
        });
  }
  @Override
  public void onCleared() {
    profileListener.remove();
  }
}
