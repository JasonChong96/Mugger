package com.bojio.mugger.listings;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.annimon.stream.function.Predicate;
import com.bojio.mugger.TestUser;
import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.listings.viewmodels.MyScheduleViewModel;
import com.bojio.mugger.profile.ProfileViewModel;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;

@RunWith(AndroidJUnit4.class)
public class MyScheduleViewModelTest {
  private FirebaseFirestore db;
  private FirebaseAuth mAuth;
  private MuggerUserCache muggerUserCache;
  private MyScheduleViewModel mViewModel;

  @Before
  public void setup() {
    FirebaseApp.initializeApp(InstrumentationRegistry.getContext());
    mAuth = FirebaseAuth.getInstance();
    db = FirebaseFirestore.getInstance();
    muggerUserCache = MuggerUserCache.getInstance();
    TestUser.login(mAuth, db);
    if (mViewModel == null) {
      mViewModel = new MyScheduleViewModel();
    }
  }

  @SmallTest
  @Test
  public void testGetFilter() {
    Predicate<Listing> predicate = mViewModel.getFilter(CalendarDay.today());
    Listing listing = new Listing(null, null, null, null,
        System.currentTimeMillis(), System.currentTimeMillis() + 24 * 60 * 60 * 1000 * 5, null,
        null, new ArrayList<>(), 0);
    Assert.assertTrue(predicate.test(listing));
    Assert.assertTrue(mViewModel.getFilter(CalendarDay.from(new Date(System.currentTimeMillis() +
        24 * 60 * 60 * 1000 * 5))).test(listing));
    Assert.assertFalse(mViewModel.getFilter(CalendarDay.from(new Date(System.currentTimeMillis() +
        24 * 60 * 60 * 1000 * 6))).test(listing));
  }
}
