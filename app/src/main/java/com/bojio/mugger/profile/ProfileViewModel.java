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

  /**
   * Load user's modules and starts listening for any changes in them.
   */
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
                  loadModulesFromSnapshot(doc, "moduleCodes", mods, ModuleRole.EMPTY);
                  loadModulesFromSnapshot(doc, "ta", mods, ModuleRole.TEACHING_ASSISTANT);
                  loadModulesFromSnapshot(doc, "professor", mods, ModuleRole.PROFESSOR);
                }
              });
          modulesBySem.postValue(modules);
        }));
  }

  /**
   * Load modules from a list under fieldName in the DocumentSnapshot doc. The modules will be
   * loaded into the input Map mods with the input module role.
   * @param doc the documentsnapshot to be loaded from
   * @param fieldName the field name of the list
   * @param mods the map to load the modules into
   * @param role the role of the user in these modules
   */
  private static void loadModulesFromSnapshot(DocumentSnapshot doc, String fieldName, Map<String,
      Byte> mods, byte role) {
    List<String> moduleCodes = (List<String>) doc.get(fieldName);
    if (moduleCodes != null) {
      for (String mod : moduleCodes) {
        mods.put(mod, role);
      }
    }
  }

  /**
   * Loads the profile of the user and starts listening to changes in data.
   */
  private void registerProfileListener() {
    listeners.add(
        MuggerDatabase.getUserReference(db, uid).addSnapshotListener((documentSnapshot, e) -> {
          updateDisplayNameView(documentSnapshot);
          updateValue(documentSnapshot, "email", email);
          updateValue(documentSnapshot, "faculty", faculty);
          updateValue(documentSnapshot, "firstMajor", firstMajor);
          updateValue(documentSnapshot, "secondMajor", secondMajor);
          updateValue(documentSnapshot, "status", status);
          MuggerRole newRole = MuggerRole.getByRoleId(documentSnapshot.getLong("roleId"));
          if (!newRole.equals(role.getValue())) {
            role.setValue(newRole);
          }
          instanceId = documentSnapshot.getString("instanceId");
        }));
  }

  /**
   * Updates the value of the LiveData input and posts the new value to observers if the data
   * under the fieldName of the input document snapshot is different from the old value.
   * @param documentSnapshot the document snapshot to be loaded from
   * @param fieldName the field name of the data in the snapshot
   * @param liveData the LiveData reference
   */
  private static void updateValue(DocumentSnapshot documentSnapshot, String fieldName,
                            MutableLiveData<String> liveData) {
    String newValue = documentSnapshot.getString(fieldName);
    if (newValue != null && !newValue.equals(liveData.getValue())) {
      liveData.setValue(newValue);
    }
  }

  /**
   * Checks the input documentSnapshot if there are any changes to display name. If there are,
   * posts the new value to all observers of the LiveData. The display name is appended with a
   * "(Muted)" if the user is currently muted.
   * @param documentSnapshot the document snapshot to load from
   */
  private void updateDisplayNameView(DocumentSnapshot documentSnapshot) {
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
  }

  /**
   * Loads and starts listening to changes in module titles. Posts value to observers if this is
   * changed.
   */
  private void registerModuleTitlesListener() {
    listeners.add(MuggerDatabase.getAllModuleTitlesRef(db).addSnapshotListener((documentSnapshot, e)
        -> moduleTitles.postValue(documentSnapshot)));
  }

  /**
   * Gets the String representation of all modules that the user is taking in the input semester.
   * Along with labels for modules which they have a special role in.
   * @param semester the semester to check
   * @return the string representation of modules taken in the input semester
   */
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
      switch (entry.getValue()) {
        case ModuleRole.PROFESSOR:
          sb.append("(Professor) ");
          break;
        case ModuleRole.TEACHING_ASSISTANT:
          sb.append("(TA) ");
          break;
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

  /**
   * Checks if the profile has been loaded.
   * @return true if the profile has been loaded, false if not
   */
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

  /**
   * Mutes the user for an input number of hours. If the input is 0, then the user is unmuted.
   * @param hours the number of hours to mute the user for
   * @return a Task reference for the muting of the user.
   */
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

  /**
   * Checks if admin controls should be visible for the current user.
   * @return true if the admin controls are visible, false if not
   */
  public boolean adminControlsVisible() {
    return MuggerRole.ADMIN.check(userCache.getRole());
  }

  /**
   * Checks if moderator controls should be visible for the current user.
   * @return true if the moderator controls are visible, false if not
   */
  public boolean moderatorControlsVisible() {
    return MuggerRole.MODERATOR.check(userCache.getRole());
  }

  public MutableLiveData<TreeMap<String, TreeMap<String, Byte>>> getModulesBySem() {
    return modulesBySem;
  }

  /**
   * Checks if the user is viewing his/her own profile.
   * @return a boolean representing if the user is viewing his/her own profile
   */
  public boolean isOwnProfile() {
    return FirebaseAuth.getInstance().getUid().equals(uid);
  }

  public MutableLiveData<DocumentSnapshot> getModuleTitles() {
    return moduleTitles;
  }

  /**
   * Updates the status of this user to be shown on his/her profile with the input string.
   * @param newStatus the new status string
   * @return a Task reference for updating status
   */
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
