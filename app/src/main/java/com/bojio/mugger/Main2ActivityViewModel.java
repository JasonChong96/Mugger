package com.bojio.mugger;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.annimon.stream.Stream;
import com.bojio.mugger.authentication.MuggerRole;
import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.database.MuggerDatabase;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

public class Main2ActivityViewModel extends AndroidViewModel {
  private FirebaseAuth mAuth;
  private FirebaseFirestore db;
  private FirebaseUser user;
  private String userUid;
  private String userName;
  private String email;
  private MutableLiveData<String> liveDisplayName;
  private MuggerUserCache cache;
  private MutableLiveData<String> title;
  private ListenerRegistration listener;

  /**
   * Constructor for the Main2Activity ViewModel.
   *
   * @param application the application that the ViewModel is in
   */
  public Main2ActivityViewModel(@NonNull Application application) {
    super(application);
    this.init();
  }

  /**
   * Initializes the view model for Main2Activity.
   */
  private void init() {
    this.mAuth = FirebaseAuth.getInstance();
    this.db = FirebaseFirestore.getInstance();
    user = mAuth.getCurrentUser();
    userUid = user.getUid();
    userName = user.getDisplayName();
    email = user.getEmail();
    cache = MuggerUserCache.getInstance();
    liveDisplayName = new MutableLiveData<>();
    title = new MutableLiveData<>();
    updateProfileCache();
    updateInstanceId();
    subscribeToTopics();
    listener =
        MuggerDatabase.getUserReference(db, userUid).addSnapshotListener((documentSnapshot, e)
            -> {
          String displayName = (String) documentSnapshot.get("displayName");
          if (!displayName.equals(liveDisplayName.getValue())) {
            liveDisplayName.postValue(displayName);
          }
        });
  }

  /**
   * Updates the Firestore database with the current email and display name of the user.
   */
  private void updateProfileCache() {
    MuggerDatabase.getUserReference(db, user.getUid()).update("displayName", userName);
    MuggerDatabase.getUserReference(db, user.getUid()).update("email", email);
  }

  /**
   * Updates the Firestore database with the current device instance Id. This is used for
   * Firebase Cloud Messaging.
   */
  private void updateInstanceId() {
    String instanceId = FirebaseInstanceId.getInstance().getToken();
    // Update instance id of this account in database
    if (instanceId != null) {
      MuggerDatabase.getUserReference(db, user.getUid()).update("instanceId", instanceId);
    }
  }

  /**
   * Subscribes this client to the relevant listing notifications.
   */
  private void subscribeToTopics() {
    if (mAuth == null) {
      return;
    }
    Query q = MuggerDatabase.getAllListingsReference(db)
        .whereGreaterThan(mAuth.getUid(), 0);
    q.get().addOnCompleteListener(snap -> {
      List<DocumentSnapshot> results = snap.getResult().getDocuments();
      Stream.of(results)
          .map(DocumentSnapshot::getId)
          .forEach(FirebaseMessaging.getInstance()::subscribeToTopic);
    });
  }

  /**
   * Checks if the user's modules have been loaded into the cache.
   *
   * @return a boolean representing whether or not the modules have been loaded
   */
  public boolean isModulesLoaded() {
    return MuggerUserCache.getInstance().getModules() != null;
  }

  /**
   * Loads module data into cache. Has to be done on a Background thread as it awaits for results
   * from Firestore API.
   *
   * @return true if the data has been loaded successfully, false if not
   */
  public boolean loadModuleData() {
    Thread.dumpStack();
    List<Task<?>> tasks = new ArrayList<>();
    tasks.add(MuggerDatabase.getUserAllSemestersDataReference(db, user.getUid()).get()
        .addOnCompleteListener(task -> {
          cache.loadModules(task.getResult().getDocuments());
          Stream.of(cache.getModules().firstEntry().getValue().keySet())
              .forEach(FirebaseMessaging.getInstance()::subscribeToTopic);
        }));
    tasks.add(MuggerDatabase.getAllModuleTitlesRef(db).get().addOnSuccessListener(snapshot -> {
      if (snapshot.exists()) {
        TreeSet<String> allMods = new TreeSet<>(snapshot.getData().keySet());
        cache.setAllModules(allMods);
      }
    }));
    Task<Void> allTasks = Tasks.whenAll(tasks);
    try {
      Tasks.await(allTasks);
    } catch (ExecutionException | InterruptedException e) {
      return false;
    }
    if (!allTasks.isSuccessful()) {
      return false;
    }
    return true;
  }

  /**
   * Signs out of the current account.
   */
  public void signOut() {
    // Firebase sign out
    mAuth.signOut();

    // Google sign out
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("292230336625-pa93l9untqrvad2mc6m3i77kckjkk4k1.apps.googleusercontent.com")
        .requestEmail()
        .build();
    GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getApplication().getApplicationContext(), gso);
    mGoogleSignInClient.signOut();

    MuggerUserCache.clear();
  }

  /**
   * Performs necessary operations when the user finishes viewing the introduction slides, i.e
   * marks the intro as done for the user so that he/she is not forced to view it another time.
   */
  public void onIntroComplete() {
    MuggerUserCache.getInstance().getData().put("introDone", 1L);
    MuggerDatabase.getUserReference(db, userUid).update("introDone", 1L);
  }

  /**
   * Checks if the user should be forced into the introduction slides.
   *
   * @return true if the user has to view the slides, false if not
   */
  public boolean shouldShowIntro() {
    return MuggerUserCache.getInstance().getData().get("introDone") == null;
  }

  /**
   * Checks if the user has access to moderator tools.
   *
   * @return true if the user has access, false if not
   */
  public boolean isModeratorToolsVisible() {
    return MuggerRole.MODERATOR.check(cache.getRole());
  }

  /**
   * Checks if the user has access to admin tools.
   *
   * @return true if the user has access, false if not
   */
  public boolean isAdminToolsVisible() {
    return MuggerRole.ADMIN.check(cache.getRole());
  }

  public String getUserName() {
    return userName;
  }

  public String getEmail() {
    return email;
  }

  public String getUserUid() {
    return userUid;
  }

  public MutableLiveData<String> getLiveDisplayName() {
    return liveDisplayName;
  }

  public MutableLiveData<String> getLiveTitle() {
    return title;
  }

  /**
   * Updates the title of the activity.
   *
   * @param newTitle the new title
   */
  public void updateTitle(String newTitle) {
    title.postValue(newTitle);
  }

  @Override
  public void onCleared() {
    listener.remove();
  }
}
