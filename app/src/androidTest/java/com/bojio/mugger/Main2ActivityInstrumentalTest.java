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
    FirebaseApp.initializeApp(InstrumentationRegistry.getContext());
    mAuth = FirebaseAuth.getInstance();
    db = FirebaseFirestore.getInstance();
    muggerUserCache = MuggerUserCache.getInstance();
    TestUser.login(mAuth, db);
  }

  @Test
  @SmallTest
  public void testSetup() {
    Assert.assertNotNull(db);
    Assert.assertNotNull(mAuth);
    Assert.assertNotNull(muggerUserCache);
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