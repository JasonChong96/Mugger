package com.bojio.mugger;

import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.database.MuggerDatabase;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Assert;

import java.util.concurrent.ExecutionException;

public class TestUser {
  public static String UID = "cx9hFspoXvb0ykpgAvMEuakQJDR2";
  public static String USERNAME = "test@mugger.com";
  public static String PASSWORD = "tester";
  public static String DISPLAY_NAME = "Tester";

  public static void login(FirebaseAuth mAuth, FirebaseFirestore db) {
    try {
      AuthCredential creds = EmailAuthProvider.getCredential(TestUser.USERNAME, TestUser.PASSWORD);
      Task<?> task = FirebaseAuth.getInstance().signInWithCredential(creds);
      Tasks.await(task);
      Assert.assertNull(task.getException());
      Task<?> taskkk = mAuth.getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder()
          .setDisplayName
              (TestUser.DISPLAY_NAME).build());
      Tasks.await(taskkk);
      Assert.assertNull(taskkk.getException());
      Task<DocumentSnapshot> taskk = MuggerDatabase.getUserReference(db, TestUser.UID).get();
      Tasks.await(taskk);
      Assert.assertNull(taskk.getException());
      MuggerUserCache.getInstance().setData(taskk.getResult().getData());
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
      throw new RuntimeException("Login failed");
    }
  }
}
