package com.bojio.mugger;

import com.bojio.mugger.database.MuggerDatabase;
import com.bojio.mugger.listings.ListingUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class TestListing {
  public static String MODULE_CODE = "GER1000";
  public static String DESCRIPTION = "Fake Description";
  public static long END_TIME = ListingUtils.DEFAULT_TIME_FILTER_END;
  public static long START_TIME = END_TIME - 1;
  public static long TYPE = 0;
  public static String VENUE = "LT1";

  public static String addTestListingToDatabase(FirebaseFirestore db, FirebaseAuth mAuth) throws ExecutionException, InterruptedException {
    Map<String, Object> listingData = new HashMap<>();
    listingData.put(MODULE_CODE, START_TIME);
    listingData.put("description", DESCRIPTION);
    listingData.put("endTime", END_TIME);
    listingData.put("moduleCode", MODULE_CODE);
    listingData.put("ownerId", mAuth.getUid());
    listingData.put("ownerName", mAuth.getCurrentUser().getDisplayName());
    listingData.put("startTime", START_TIME);
    listingData.put("type", TYPE);
    listingData.put("venue", VENUE);
    Task<DocumentReference> task = MuggerDatabase.createListing(db, listingData);
    Tasks.await(task);
    return task.getResult().getId();
  }
}
