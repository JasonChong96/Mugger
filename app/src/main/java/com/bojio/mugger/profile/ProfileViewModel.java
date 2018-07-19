package com.bojio.mugger.profile;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.annimon.stream.Stream;
import com.bojio.mugger.authentication.MuggerRole;
import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.constants.ModuleRole;
import com.bojio.mugger.database.MuggerDatabase;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.common.base.Joiner;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ProfileViewModel extends ViewModel {
  private MutableLiveData<String> status;
  private MutableLiveData<String> firstMajor;
  private MutableLiveData<String> secondMajor;
  private FirebaseFirestore db;
  private String uid;
  private MutableLiveData<String> displayName;
  private MutableLiveData<String> email;
  private MutableLiveData<String> faculty;
  private List<ListenerRegistration> listeners;
  private MutableLiveData<TreeMap<String, TreeMap<String, Byte>>> modulesBySem;
  private MutableLiveData<DocumentSnapshot> moduleTitles;
  private MutableLiveData<MuggerRole> role;
  private MuggerUserCache userCache;
  private String instanceId;
  private String selectedSemester;
  private boolean profileLoaded;

  public ProfileViewModel() {
    super();
    db = FirebaseFirestore.getInstance();
    userCache = MuggerUserCache.getInstance();
    displayName = new MutableLiveData<>();
    email = new MutableLiveData<>();
    faculty = new MutableLiveData<>();
    firstMajor = new MutableLiveData<>();
    secondMajor = new MutableLiveData<>();
    modulesBySem = new MutableLiveData<>();
    listeners = new LinkedList<>();
    moduleTitles = new MutableLiveData<>();
    status = new MutableLiveData<>();
    role = new MutableLiveData<>();
  }

  public void init(String uid) {
    if (listeners.isEmpty() && this.uid == null) {
      this.uid = uid;
      registerProfileListener();
      registerModulesListener();
      registerModuleTitlesListener();
    }
  }

  public MutableLiveData<MuggerRole> getRole() {
    return role;
  }

  private void registerModulesListener() {
    listeners.add(MuggerDatabase.getUserAllSemestersDataReference(db, uid)
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
        }));
  }

  private void registerProfileListener() {
    Log.e("A", "AAASdasdAS");
    listeners.add(

        MuggerDatabase.getUserReference(db, uid).addSnapshotListener((documentSnapshot, e) -> {
          String newEmail = documentSnapshot.getString("email");
          Log.e("A", "AAASdasdASSS");
          if (newEmail != null && !newEmail.equals(email.getValue())) {
            email.setValue(newEmail);
          }
          String newFaculty = documentSnapshot.getString("faculty");
          if (newFaculty != null && !newFaculty.equals(faculty.getValue())) {
            faculty.setValue(newFaculty);
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
            displayName.setValue(newName);
          }
          String newFirstMajor = documentSnapshot.getString("firstMajor");
          if (newFirstMajor != null && !newFirstMajor.equals(firstMajor.getValue())) {
            firstMajor.setValue(newFirstMajor);
          }
          String newSecondMajor = documentSnapshot.getString("secondMajor");
          if (newSecondMajor != null && !newSecondMajor.equals(secondMajor.getValue())) {
            secondMajor.setValue(newSecondMajor);
          }
          String newStatus = documentSnapshot.getString("status");
          if (newStatus != null && !newStatus.equals(status.getValue())) {
            status.setValue(newStatus);
          }
          MuggerRole newRole = MuggerRole.getByRoleId(documentSnapshot.getLong("roleId"));
          if (!newRole.equals(role.getValue())) {
            role.setValue(newRole);
          }
          instanceId = documentSnapshot.getString("instanceId");
        }));
  }

  private void registerModuleTitlesListener() {
    listeners.add(MuggerDatabase.getAllModuleTitlesRef(db).addSnapshotListener((documentSnapshot, e)
        -> {
      moduleTitles.postValue(documentSnapshot);
    }));
  }

  public String getSemesterModulesDisplay(String semester) {
    TreeMap<String, TreeMap<String, Byte>> allModules = modulesBySem.getValue();
    DocumentSnapshot titles = moduleTitles.getValue();
    if (allModules == null || !allModules.containsKey(semester)) {
      return "Error: Semester data not found.";
    }
    if (titles == null) {
      return "Error: Module titles not found";
    }
    TreeMap<String, Byte> semModules = allModules.get(semester);
    List<String> moduleStrings = new LinkedList<>();
    for (Map.Entry<String, Byte> entry : semModules.entrySet()) {
      String moduleCode = entry.getKey();
      StringBuilder sb = new StringBuilder();
      if (entry.getValue().equals(ModuleRole.PROFESSOR)) {
        sb.append("(Professor) ");
      } else if (entry.getValue().equals(ModuleRole.TEACHING_ASSISTANT)) {
        sb.append("(TA) ");
      }
      sb.append(moduleCode)
          .append(" ")
          .append(titles.contains(moduleCode) ? titles.getString(moduleCode) : "");
      moduleStrings.add(sb.toString());
    }
    return Joiner.on("\n").join(moduleStrings);
  }

  @Override
  public void onCleared() {
    Stream.of(listeners).forEach(ListenerRegistration::remove);
  }

  public boolean isProfileLoaded() {
    if (profileLoaded) {
      return true;
    } else {
      if (role != null && moduleTitles.getValue() != null && modulesBySem.getValue() != null) {
        profileLoaded = true;
      }
      return profileLoaded;
    }
  }

  public Task<Void> muteUser(int hours) {
    List<Task<?>> tasks = new ArrayList<>();
    long until = hours * 3600000 + System.currentTimeMillis();
    tasks.add(MuggerDatabase.getUserReference(db, uid).update("muted", until));
    Map<String, Object> notificationData = new HashMap<>();
    if (instanceId != null) {
      notificationData.put("instanceId", instanceId);
      notificationData.put("duration", Integer.toString(hours));
      notificationData.put("fromUid", "");
      notificationData.put("topicUid", "");
      notificationData.put("type", hours == 0 ? "unmute" : "mute");
      notificationData.put("until", Long.toString(until));
      tasks.add(MuggerDatabase.sendNotification(db, notificationData));
    }
    return Tasks.whenAll(tasks);
  }

  public MutableLiveData<String> getStatus() {
    return status;
  }

  public MutableLiveData<String> getFirstMajor() {
    return firstMajor;
  }

  public MutableLiveData<String> getSecondMajor() {
    return secondMajor;
  }

  public MutableLiveData<String> getDisplayName() {
    return displayName;
  }

  public MutableLiveData<String> getEmail() {
    return email;
  }

  public MutableLiveData<String> getFaculty() {
    return faculty;
  }

  public boolean adminControlsVisible() {
    return MuggerRole.ADMIN.check(userCache.getRole());
  }

  public boolean moderatorControlsVisible() {
    return MuggerRole.MODERATOR.check(userCache.getRole());
  }

  public MutableLiveData<TreeMap<String, TreeMap<String, Byte>>> getModulesBySem() {
    return modulesBySem;
  }

  public boolean isOwnProfile() {
    return FirebaseAuth.getInstance().getUid().equals(uid);
  }

  public MutableLiveData<DocumentSnapshot> getModuleTitles() {
    return moduleTitles;
  }

  public Task<Void> updateStatus(String newStatus) {
    return MuggerDatabase.getUserReference(db, uid).update("status", newStatus);
  }

  public String getSelectedSemester() {
    return selectedSemester;
  }

  public void setSelectedSemester(String selectedSemester) {
    this.selectedSemester = selectedSemester;
  }
}
