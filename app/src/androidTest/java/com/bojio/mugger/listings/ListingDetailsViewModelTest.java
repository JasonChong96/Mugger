package com.bojio.mugger.listings;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.bojio.mugger.Main2Activity;
import com.bojio.mugger.TestListing;
import com.bojio.mugger.TestUser;
import com.bojio.mugger.administration.feedback.MakeFeedbackActivity;
import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.database.MuggerDatabase;
import com.bojio.mugger.listings.viewmodels.ListingDetailsViewModel;
import com.bojio.mugger.listings.viewmodels.MyScheduleViewModel;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class ListingDetailsViewModelTest {
  private FirebaseFirestore db;
  private FirebaseAuth mAuth;
  private MuggerUserCache muggerUserCache;
  private ListingDetailsViewModel mViewModel;

  @Rule
  public final ActivityTestRule<MakeFeedbackActivity> mActivityRule = new ActivityTestRule<>(
      MakeFeedbackActivity.class, true, true);
  // Dummy activity to get application reference

  @Before
  public void setup() {
    FirebaseApp.initializeApp(InstrumentationRegistry.getContext());
    mAuth = FirebaseAuth.getInstance();
    db = FirebaseFirestore.getInstance();
    muggerUserCache = MuggerUserCache.getInstance();
    TestUser.login(mAuth, db);
    if (mViewModel == null) {
      mViewModel = new ListingDetailsViewModel(mActivityRule.getActivity().getApplication());
    }
    try {
      String listingUid = TestListing.addTestListingToDatabase(db, mAuth);
      mViewModel.init(listingUid);
      Thread.sleep(5000);
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
      Assert.fail("Exception occured when adding test listing");
    }
  }

  @After
  public void clearTestListing() {
    mViewModel.deleteListing();
  }

  @SmallTest
  @Test
  public void testCanEditDelete() {
    Assert.assertTrue(mViewModel.canEditDelete());
  }

  @MediumTest
  @Test
  public void testGetters() {
    // Getting string representation of time varies based on system settings of the android
    // system that is used for testing, hence it is not automatically tested here due to
    // undefined behavior
    Assert.assertEquals(mViewModel.getAttendees().size(), 0);
    Assert.assertEquals(mViewModel.getModuleCode(), TestListing.MODULE_CODE);
    Assert.assertEquals(mViewModel.getOwnerUid(), mAuth.getUid());
    Assert.assertEquals(mViewModel.getVenue().getValue(), TestListing.VENUE);
    Assert.assertEquals(mViewModel.getDescription().getValue(), TestListing.DESCRIPTION);
    Assert.assertEquals(mViewModel.getListing().getStartTime(), TestListing.START_TIME);
    Assert.assertEquals(mViewModel.getListing().getEndTime(), TestListing.END_TIME);
  }
}
