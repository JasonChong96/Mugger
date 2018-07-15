package com.bojio.mugger.profile;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.bojio.mugger.constants.ModuleRole;
import com.bojio.mugger.database.MuggerDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

public class ProfileViewModel extends ViewModel {
  private MutableLiveData<String> firstMajor;
  private MutableLiveData<String> secondMajor;
  private FirebaseFirestore db;
  private FirebaseAuth mAuth;
  private MutableLiveData<String> displayName;
  private MutableLiveData<String> email;
  private MutableLiveData<String> faculty;
  private ListenerRegistration profileListener;
  private ListenerRegistration moduleListener;
  private MutableLiveData<TreeMap<String, TreeMap<String, Byte>>> modulesBySem;

  public ProfileViewModel() {
    super();
    db = FirebaseFirestore.getInstance();
    mAuth = FirebaseAuth.getInstance();
    displayName = new MutableLiveData<>();
    email = new MutableLiveData<>();
    faculty = new MutableLiveData<>();
    firstMajor = new MutableLiveData<>();
    secondMajor = new MutableLiveData<>();
    modulesBySem = new MutableLiveData<>();
  }

  private void init() {
    registerProfileListener();
    registerModulesListener();
  }

  private void registerModulesListener() {
    moduleListener = MuggerDatabase.getUserAllSemestersDataReference(db, mAuth.getUid())
        .addSnapshotListener((queryDocumentSnapshots, e) -> {
          TreeMap<String, TreeMap<String, Byte>> modules = modulesBySem.getValue() == null ?
              new TreeMap<>(Collections.reverseOrder()) : modulesBySem.getValue();
          Stream.of(queryDocumentSnapshots.getDocumentChanges())
              .forEach(docChange -> {
                QueryDocumentSnapshot doc = docChange.getDocument();
                if (docChange.getType().equals(DocumentChange.Type.REMOVED)) {
                  modules.remove(doc.getId().replace(".", "/"));
                } else {
                  TreeMap<String, Byte> mods = new TreeMap<>();
                  modules.put(doc.getId().replace(".", "/"), mods);
                  for (String mod : (List<String>) doc.get("moduleCodes")) {
                    mods.put(mod, ModuleRole.EMPTY);
                  }
                  List<String> ta = (List<String>) doc.get("ta");
                  if (ta != null) {
                    for (String mod : ta) {
                      mods.put(mod, ModuleRole.TEACHING_ASSISTANT);
                    }
                  }
                  List<String> prof = (List<String>) doc.get("professor");
                  if (prof != null) {
                    for (String mod : (List<String>) doc.get("professor")) {
                      mods.put(mod, ModuleRole.PROFESSOR);
                    }
                  }
                }
              });
          modulesBySem.postValue(modules);
        });
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
    moduleListener.remove();
  }
}
