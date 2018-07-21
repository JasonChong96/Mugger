package com.bojio.mugger.listings.chat;

import android.arch.lifecycle.ViewModel;

import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.database.MuggerDatabase;
import com.bojio.mugger.fcm.MessagingService;
import com.bojio.mugger.listings.Listing;
import com.bojio.mugger.listings.ListingUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ListingChatViewModel extends ViewModel {
  private Listing listing;
  private FirebaseFirestore db;
  private FirebaseAuth mAuth;
  private MuggerUserCache cache;

  public ListingChatViewModel() {
    db = FirebaseFirestore.getInstance();
    mAuth = FirebaseAuth.getInstance();
    cache = MuggerUserCache.getInstance();
  }

  public void setListing(Listing listing) {
    this.listing = listing;
  }

  public String getListingUid() {
    return listing.getUid();
  }

  public String getModuleCode() {
    return listing.getModuleCode();
  }

  public String getOwnerName() {
    return listing.getOwnerName();
  }

  public double getMuteTimeLeft() {
    return (double) cache.isMuted() / 3600000D;
  }

  /**
   * Sends the input message into the chat along with notification to all who are joining this
   * listing.
   * @param message the message to be sent
   */
  public void sendMessage(String message) {
    FirebaseUser user = mAuth.getCurrentUser();
    // Easter egg. Entirely for fun
    if (message.equalsIgnoreCase("fuck you")) {
      String[] wholesome = {"Wholly accept your flaws, and suddenly no one is strong enough to " +
          "use them against you.",
          "Remember, you have been criticizing yourself for years and it hasn’t worked. Try " +
              "approving of yourself and see what happens.",
          "Accepting yourself is about respecting yourself. It’s about honoring yourself right " +
              "now, here today, in this moment. Not just who you could become somewhere down the line."};
      message = wholesome[new Random().nextInt(wholesome.length)];
    }
    long timestamp = System.currentTimeMillis();
    long dayTimestamp = ListingUtils.getDayTimestamp(timestamp);
    String userUid = user.getUid();
    Map<String, Object> messageData = new HashMap<>();
    messageData.put("fromUid", userUid);
    messageData.put("fromName", user.getDisplayName());
    messageData.put("content", message);
    messageData.put("time", timestamp);
    messageData.put("day", dayTimestamp);
    // Add to listing chat
    MuggerDatabase.sendListingChatMessage(db, getListingUid(), messageData);
    Map<String, Object> notificationData = new HashMap<>();
    notificationData.put("topicUid", getListingUid());
    StringBuilder content = new StringBuilder("(Latest Message) ");
    content.append(user.getDisplayName()).append(" : ").append(message);
    notificationData.put("body", content.toString());
    StringBuilder title = new StringBuilder();
    title.append(getOwnerName()).append("'s ").append(getModuleCode()).append(" Listing");
    notificationData.put("title", title.toString());
    notificationData.put("type", MessagingService.CHAT_NOTIFICATION);
    notificationData.put("fromUid", user.getUid());
    // Add to notification db
    MuggerDatabase.sendNotification(db, notificationData);
  }
}
