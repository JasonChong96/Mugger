package com.bojio.mugger.profile;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.bojio.mugger.MainActivityViewModel;
import com.bojio.mugger.TestUser;
import com.bojio.mugger.authentication.MuggerRole;
import com.bojio.mugger.authentication.MuggerUserCache;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ProfileViewModelTest {
  private FirebaseFirestore db;
  private FirebaseAuth mAuth;
  private MuggerUserCache muggerUserCache;
  private ProfileViewModel mViewModel;

  @Before
  public void setup() {
    FirebaseApp.initializeApp(InstrumentationRegistry.getContext());
    mAuth = FirebaseAuth.getInstance();
    db = FirebaseFirestore.getInstance();
    muggerUserCache = MuggerUserCache.getInstance();
    TestUser.login(mAuth, db);
    if (mViewModel == null) {
      mViewModel = new ProfileViewModel();
      mViewModel.init(mAuth.getUid());
    }
  }

  @SmallTest
  @Test
  public void testProfileLoaded() {
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    Assert.assertTrue(mViewModel.isProfileLoaded());
    mViewModel = new ProfileViewModel();
    Assert.assertFalse(new ProfileViewModel().isProfileLoaded());
  }

  @SmallTest
  @Test
  public void testIsOwnProfile() {
    Assert.assertTrue(mViewModel.isOwnProfile());
    ProfileViewModel otherUserProfile = new ProfileViewModel();
    otherUserProfile.init("aaa");
    Assert.assertFalse(otherUserProfile.isOwnProfile());
  }

  @SmallTest
  @Test
  public void testModeratorControlsVisible() {
    muggerUserCache.setRole(MuggerRole.USER);
    Assert.assertFalse(mViewModel.moderatorControlsVisible());
    muggerUserCache.setRole(MuggerRole.MODERATOR);
    Assert.assertTrue(mViewModel.moderatorControlsVisible());
    muggerUserCache.setRole(MuggerRole.ADMIN);
    Assert.assertTrue(mViewModel.moderatorControlsVisible());
    muggerUserCache.setRole(MuggerRole.MASTER);
    Assert.assertTrue(mViewModel.moderatorControlsVisible());
  }

  @SmallTest
  @Test
  public void testAdminControlsVisible() {
    muggerUserCache.setRole(MuggerRole.USER);
    Assert.assertFalse(mViewModel.adminControlsVisible());
    muggerUserCache.setRole(MuggerRole.MODERATOR);
    Assert.assertFalse(mViewModel.adminControlsVisible());
    muggerUserCache.setRole(MuggerRole.ADMIN);
    Assert.assertTrue(mViewModel.adminControlsVisible());
    muggerUserCache.setRole(MuggerRole.MASTER);
    Assert.assertTrue(mViewModel.adminControlsVisible());
  }
}
