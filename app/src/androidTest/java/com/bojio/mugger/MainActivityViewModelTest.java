package com.bojio.mugger;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.database.MuggerDatabase;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class MainActivityViewModelTest {
  private FirebaseFirestore db;
  private FirebaseAuth mAuth;
  private MuggerUserCache muggerUserCache;
  private MainActivityViewModel mViewModel;

  @Before
  public void setup() {
    FirebaseApp.initializeApp(InstrumentationRegistry.getContext());
    mAuth = FirebaseAuth.getInstance();
    db = FirebaseFirestore.getInstance();
    muggerUserCache = MuggerUserCache.getInstance();
    TestUser.login(mAuth, db);
    if (mViewModel == null) {
      mViewModel = new MainActivityViewModel();
      mViewModel.init();
    }
  }

  @Test
  @SmallTest
  public void testIsLoggedIn() {
    Assert.assertTrue(mViewModel.isLoggedIn());
  }

  @Test
  @SmallTest
  public void testUpdateCache() {
    mViewModel.updateCache();
    Assert.assertNotNull(muggerUserCache.getData());
    Assert.assertNotNull(muggerUserCache.getRole());
  }

  @Test
  @SmallTest
  public void testIsRedirectToIvleLogin() {
    try {
      Assert.assertFalse(mViewModel.isRedirectToIvleLogin());
      Map<String, Object> cacheData = muggerUserCache.getData();
      String originalNusNetId = (String) cacheData.get("nusNetId");
      Task<Void> deleteTask = MuggerDatabase.getUserReference(db, mAuth.getUid())
          .update("nusNetId", FieldValue.delete());
      Tasks.await(deleteTask);
      mViewModel = new MainActivityViewModel();
      mViewModel.init();
      Assert.assertTrue(mViewModel.isRedirectToIvleLogin());
      Task<Void> restoreTask = MuggerDatabase.getUserReference(db, mAuth.getUid())
          .update("nusNetId", originalNusNetId);
      Tasks.await(restoreTask);
    } catch (ExecutionException | InterruptedException e) {
      e.printStackTrace();
      Assert.fail("Error waiting for update task");
    }
  }
}
