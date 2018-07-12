package com.bojio.mugger;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.widget.TextView;

import com.bojio.mugger.authentication.GoogleLoginActivity;
import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.database.MuggerDatabase;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class Main2ActivityInstrumentalTest {
  private FirebaseFirestore db;
  private FirebaseAuth mAuth;
  private MuggerUserCache muggerUserCache;

  @Rule
  public final ActivityTestRule<Main2Activity> mActivityRule = new ActivityTestRule<>(
      Main2Activity.class, true, true);

  @Before
  public void setup() {
   // if (FirebaseApp.getApps(InstrumentationRegistry.getContext()).isEmpty()) {
      FirebaseApp.initializeApp(InstrumentationRegistry.getContext());
    mAuth = FirebaseAuth.getInstance();
    try {
      AuthCredential creds = EmailAuthProvider.getCredential(TestUser.USERNAME, TestUser.PASSWORD);
      Task<?> task = FirebaseAuth.getInstance().signInWithCredential(creds);
      Tasks.await(task);
      Assert.assertNull(task.getException());
      testLoggedIn();
      Task<?> taskkk = mAuth.getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder()
          .setDisplayName
          (TestUser.DISPLAY_NAME).build());
      Tasks.await(taskkk);
      Assert.assertNull(taskkk.getException());
      db = FirebaseFirestore.getInstance();
      muggerUserCache = MuggerUserCache.getInstance();
      Task<DocumentSnapshot> taskk = MuggerDatabase.getUserReference(db, TestUser.UID).get();
      Tasks.await(taskk);
      Assert.assertNull(taskk.getException());
      muggerUserCache.setData(taskk.getResult().getData());
      // }
    } catch (ExecutionException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();

    }
  }

  @Test
  @SmallTest
  public void testLoggedIn() {
    Assert.assertNotNull(FirebaseAuth.getInstance().getCurrentUser());
  }

  @Test
  @SmallTest
  public void testModules() {
    Assert.assertNotNull(muggerUserCache.getModules());
  }

  @Test
  @SmallTest
  public void testAllModules() {
    Assert.assertNotNull(muggerUserCache.getAllModules());
  }

  @Test
  @SmallTest
  public void testDisplayNameView() {
    Assert.assertNotNull(mAuth.getCurrentUser().getDisplayName());
    Assert.assertEquals(((TextView) mActivityRule.getActivity().findViewById(R.id.username))
        .getText(), mAuth.getCurrentUser().getDisplayName());
    Assert.assertEquals(((TextView) mActivityRule.getActivity().findViewById(R.id.email))
        .getText(), mAuth.getCurrentUser().getEmail());
  }
}