package com.bojio.mugger;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.bojio.mugger.administration.feedback.MakeFeedbackActivity;
import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.profile.ProfileListRecyclerAdapter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class Main2ActivityViewModelTest {
  private FirebaseFirestore db;
  private FirebaseAuth mAuth;
  private MuggerUserCache muggerUserCache;
  private Main2ActivityViewModel mViewModel;

  @Rule
  public final ActivityTestRule<MakeFeedbackActivity> mActivityRule = new ActivityTestRule<>(
      MakeFeedbackActivity.class, true, true);
  // Dummy activity to get Application reference

  @Before
  public void setup() {
    FirebaseApp.initializeApp(InstrumentationRegistry.getContext());
    mAuth = FirebaseAuth.getInstance();
    db = FirebaseFirestore.getInstance();
    muggerUserCache = MuggerUserCache.getInstance();
    TestUser.login(mAuth, db);
    if (mViewModel == null) {
      mViewModel = new Main2ActivityViewModel(mActivityRule.getActivity().getApplication());
    }
  }

  @SmallTest
  @Test
  public void testModuleLoading() {
    // Unable to reliably test isModulesLoaded before this step. The application might have
    // loaded the data beforehand on login, hence the result will be undefined depending on how
    // long this takes to run.
    mViewModel.loadModuleData();
    Assert.assertTrue(mViewModel.isModulesLoaded());
    Assert.assertNotNull(muggerUserCache.getAllModules());
    Assert.assertNotNull(muggerUserCache.getModules());
  }
}
