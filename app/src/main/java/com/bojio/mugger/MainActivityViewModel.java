package com.bojio.mugger;

import android.arch.lifecycle.ViewModel;

import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.constants.DebugSettings;
import com.bojio.mugger.database.MuggerDatabase;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainActivityViewModel extends ViewModel {
  private FirebaseFirestore db;
  private FirebaseAuth mAuth;
  private Map<String, Object> userData;

  public MainActivityViewModel() {
    db = FirebaseFirestore.getInstance();
    mAuth = FirebaseAuth.getInstance();
  }

  /**
   * Initializes this ViewModel. Loads the users' data if he/she is logged in. Returns false if
   * loading of data fails. Must be ran on a background thread as it awaits results from the
   * Firestore API.
   * @return true if the user is not logged in or is logged in and data is loaded successfully,
   * false if data retrieval is unsuccessful
   */
  public boolean init() {
    if (userData == null) {
      if (isLoggedIn()) {
        // Is already logged in, proceed to load user data from database
        Task<DocumentSnapshot> task = MuggerDatabase.getUserReference(db, mAuth.getUid()).get();
        try {
          Tasks.await(task);
        } catch (ExecutionException | InterruptedException e) {
          e.printStackTrace();
          return false;
        }
        if (!task.isSuccessful()) {
          return false;
        } else {
          if (task.getResult().exists()) {
            userData = task.getResult().getData();
          }
          return true;
        }
      } else {
        return true;
      }
    } else {
      // User data has already been loaded. No possibility for error.
      return true;
    }
  }

  /**
   * Checks if the user is logged in.
   * @return true if the user is logged in, false if not
   */
  public boolean isLoggedIn() {
    return mAuth.getCurrentUser() != null;
  }


  /**
   * Checks the account and starts the appropriate activity for the account. i.e If the account
   * has not logged in to IVLE for verification before, redirect to IVLE login. If not, redirect
   * to the listings main page.
   */
  public boolean isRedirectToIvleLogin() {
    // Checks if user has been verified as an NUS student by checking if NUSNETID has been
    // logged before
    if (userData != null && userData.get("nusNetId") != null && !DebugSettings
        .ALWAYS_REDIRECT_TO_IVLE) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Returns the display name of the user, throws a NullPointerException if the user is not
   * logged in.
   * @return the display name of currently logged in user
   */
  public String getDisplayName() {
    return mAuth.getCurrentUser().getDisplayName();
  }

  /**
   * Updates the local cache with user data previously loaded into this ViewModel.
   */
  public void updateCache() {
    MuggerUserCache.getInstance().setData(userData);
  }
}
