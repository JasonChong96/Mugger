package com.bojio.mugger.authentication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.bojio.mugger.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

import es.dmoral.toasty.Toasty;

public abstract class LoggedInActivity extends AppCompatActivity {
  protected FirebaseAuth mAuth;
  protected FirebaseFirestore db;
  protected MuggerUserCache cache;
  protected boolean stopActivity;

  /**
   * Does all necessary steps when signing out. i.e Resetting instance ID and going back to the main
   * page of the application.
   */
  public static void signOut(Activity activity) {
    activity.finish();
    try {
      FirebaseInstanceId.getInstance().deleteInstanceId();
    } catch (IOException e) {
      e.printStackTrace();
    }
    Intent intent = new Intent(activity, MainActivity
        .class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
    Toasty.success(activity, "Logged out " +
        "successfully", Toast.LENGTH_SHORT).show();
    activity.startActivity(intent);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mAuth = FirebaseAuth.getInstance();
    cache = MuggerUserCache.getInstance();
    db = FirebaseFirestore.getInstance();
    if (mAuth.getCurrentUser() == null) {
      signOut(this);
    }
    setAuthStateChangeListener();
    if (MuggerUserCache.getInstance().getData().size() == 0) {
      //    if (mAuth.getCurrentUser() == null) {
      Intent intent = new Intent(this, MainActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
          | Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(intent);
      stopActivity = true;
      return;
    }
/*      } else {
        SpotsDialog.Builder dialog = new SpotsDialog
            .Builder()
            .setContext(this)
            .setTheme(R.style.SpotsDialog)
            .setMessage("Reloading data cache...")
            .setCancelable(false);
        MuggerDatabase.getUserReference(db, mAuth.getUid()).get().addOnCompleteListener(task_ -> {
          if (!task_.isSuccessful()) {
            signOut(this);
          } else {
            cache.setData(task_.getResult().getData());
            MuggerDatabase.getUserReference(db, mAuth.getUid()).collection(MuggerDatabase.SEMESTER_COLLECTION).get()
                .addOnCompleteListener(task -> {
                  if (!task.isSuccessful()) {
                    signOut(this);
                  } else {
                    cache.loadModules(task.getResult().getDocuments());
                    recreate();
                  }
                });
          }
        });*/
    }


  /**
   * Sets the function to be invoked when log in stage is changed. i.e when user has signed out,
   * bring him back to the login page and unsubscribe him from notifications
   */
  private void setAuthStateChangeListener() {
    mAuth.addAuthStateListener(firebaseAuth -> {
      if (firebaseAuth.getCurrentUser() == null) {
        signOut(this);
      }
    });
  }
}
