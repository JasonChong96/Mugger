package com.bojio.mugger;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

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

  public Main2ActivityViewModel(@NonNull Application application) {
    super(application);
    this.init();
  }

  private void init() {
    this.mAuth = FirebaseAuth.getInstance();
    this.db = FirebaseFirestore.getInstance();
    user = mAuth.getCurrentUser();
    userUid = user.getUid();
    userName = user.getDisplayName();
    email = user.getEmail();
    cache = MuggerUserCache.getInstance();
    liveDisplayName = new MutableLiveData<>();
    liveDisplayName.postValue(userName);
    title = new MutableLiveData<>();
    updateProfileCache();
    updateInstanceId();
    subscribeToTopics();
    MuggerDatabase.getUserReference(db, userUid).addSnapshotListener(new
                                                                         EventListener<DocumentSnapshot>() {
                                                                           @Override
                                                                           public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                                                             String displayName = (String) documentSnapshot.get("displayName");
                                                                             if (!displayName.equals(liveDisplayName.getValue())) {
                                                                               liveDisplayName.postValue(displayName);
                                                                             }
                                                                           }
                                                                         });
  }

  private void updateProfileCache() {
    MuggerDatabase.getUserReference(db, user.getUid()).update("displayName", userName);
    MuggerDatabase.getUserReference(db, user.getUid()).update("email", email);
  }

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
      for (DocumentSnapshot doc : results) {
        FirebaseMessaging.getInstance().subscribeToTopic(doc.getId());
      }
    });
  }

  public boolean isModulesLoaded() {
    return MuggerUserCache.getInstance().getModules() != null;
  }

  public boolean loadModuleData() {
    List<Task<?>> tasks = new ArrayList<>();
    tasks.add(MuggerDatabase.getUserAllSemestersDataReference(db, user.getUid()).get()
        .addOnCompleteListener(task -> {
          cache.loadModules(task.getResult().getDocuments());
          for (String mod : cache.getModules().firstEntry().getValue().keySet()) {
            FirebaseMessaging.getInstance().subscribeToTopic(mod);
          }
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

  public void onIntroComplete() {
    MuggerUserCache.getInstance().getData().put("introDone", 1L);
    MuggerDatabase.getUserReference(db, userUid).update("introDone", 1L);
  }

  public boolean shouldShowIntro() {
    return MuggerUserCache.getInstance().getData().get("introDone") == null;
  }

  public boolean isModeratorToolsVisible() {
    return MuggerRole.MODERATOR.check(cache.getRole());
  }

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

  public void updateTitle(String newTitle) {
    title.postValue(newTitle);
  }
}
