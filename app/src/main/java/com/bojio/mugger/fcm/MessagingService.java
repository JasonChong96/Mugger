package com.bojio.mugger.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bojio.mugger.Main2Activity;
import com.bojio.mugger.R;
import com.bojio.mugger.authentication.MuggerUser;
import com.bojio.mugger.constants.DebugSettings;
import com.bojio.mugger.constants.MuggerRole;
import com.bojio.mugger.listings.AvailableListingDetailsActivity;
import com.bojio.mugger.listings.chat.ListingChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MessagingService extends FirebaseMessagingService {

  private static final String TAG = "MessagingService";
  private static AtomicInteger notificationId = new AtomicInteger(0);
  private static Map<String, Integer> chatToId = new HashMap<>();
  public static String CHAT_NOTIFICATION = "chat";
  public static String DELETED_NOTIFICATION = "delete";
  public static String CREATED_NOTIFICATION = "create";
  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {
    // [START_EXCLUDE]
    // There are two types of messages data messages and notification messages. Data messages are handled
    // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
    // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
    // is in the foreground. When the app is in the background an automatically generated notification is displayed.
    // When the user taps on the notification they are returned to the app. Messages containing both notification
    // and data payloads are treated as notification messages. The Firebase console always sends notification
    // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
    // [END_EXCLUDE]

    // TODO(developer): Handle FCM messages here.
    // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
    Log.d(TAG, "From: " + remoteMessage.getFrom());
    Map<String, String> data = remoteMessage.getData();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    // Check if message contains a data payload.
    if (remoteMessage.getData().size() > 0) {
      Log.d(TAG, "Message data payload: " + remoteMessage.getData());

      if (/* Check if data needs to be processed by long running job */ true) {
        // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
        scheduleJob();
      } else {
        // Handle message within 10 seconds
        handleNow();
      }

    }
    Log.d(TAG, "Message received");
    if (user != null) {
      switch (data.get("type")) {
        case "mute":
          MuggerUser.getInstance().getData().put("muted", Long.parseLong((String) data.get("until")));
          break;
        case "unmute":
          MuggerUser.getInstance().getData().remove("muted");
          break;
        case "role":
          MuggerRole newRole = MuggerRole.valueOf(data.get("newRoleName"));
          MuggerUser.getInstance().setRole(newRole);
          break;
      }
    }

    // Check if message contains a notification payload.
    if (data.get("notification") != null) {
      String senderUid = data.get("senderUid");
      if (DebugSettings.NOTIFICATION_TO_SELF || senderUid == null ||
          (user != null && senderUid.equals(user.getUid()))) {
        sendNotification(remoteMessage.getData());
      }
    }

    // Also if you intend on generating your own notifications as a result of a received FCM
    // message, here is where that should be initiated. See sendNotification method below.
  }
  /**
   * Schedule a job using FirebaseJobDispatcher.
   */
  private void scheduleJob() {
  /*  // [START dispatch_job]
    FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
    Job myJob = dispatcher.newJobBuilder()
        .setService(MyJobService.class)
        .setTag("my-job-tag")
        .build();
    dispatcher.schedule(myJob);*/
    // [END dispatch_job]
  }

  /**
   * Handle time allotted to BroadcastReceivers.
   */
  private void handleNow() {
    //Log.d(TAG, "Short lived task is done.");
  }

  /**
   * Create and show a simple notification containing the received FCM message.
   *
   * @param data FCM data received.
   */
  private void sendNotification(Map<String, String> data) {
    String listingUid = data.get("listingUid");
    String type = data.get("type");
    Map<String, Object> cache = MuggerUser.getInstance().getData();
    if (cache == null) {
      return;
    } else if (type.equals(CHAT_NOTIFICATION) && cache.get(CHAT_NOTIFICATION) != null && ((Long)
        cache.get(CHAT_NOTIFICATION)).equals(Long.valueOf(0))) {
      return;
    } else if (type.equals(CREATED_NOTIFICATION) && cache.get(CREATED_NOTIFICATION) != null && ((Long)
        cache.get(CREATED_NOTIFICATION)).equals(Long.valueOf(0))) {
      return;
    } else if (type.equals(DELETED_NOTIFICATION) && cache.get(DELETED_NOTIFICATION) != null && ((Long)
        cache.get(DELETED_NOTIFICATION)).equals(Long.valueOf(0))) {
      return;
    }
    if (!chatToId.containsKey(listingUid)) {
      chatToId.put(listingUid, notificationId.getAndIncrement());
    }
    int id = chatToId.get(listingUid);
    String messageBody = data.get("body");
    String messageTitle = data.get("title");
    Intent intent;
    if (data.get("type").equals(MessagingService.CHAT_NOTIFICATION)) {
      intent = new Intent(this, ListingChatActivity.class);
      Bundle b = new Bundle();
      b.putString("listingUid", data.get("listingUid"));
      intent.putExtras(b);
    } else if (data.get("type").equals(MessagingService.CREATED_NOTIFICATION)) {
      intent = new Intent(this, AvailableListingDetailsActivity.class);
      Bundle b = new Bundle();
      b.putString("listingUid", data.get("listingUid"));
      intent.putExtras(b);
    } else if (data.get("type").equals(MessagingService.DELETED_NOTIFICATION)) {
      intent = null;
    } else {
      intent = null;
    }
    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


    String channelId = "aaa";
    Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    NotificationCompat.Builder notificationBuilder =
        new NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(messageTitle)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(data.get("listingUid"));
    if (intent != null) {
      PendingIntent pendingIntent = PendingIntent.getActivity(this, id /* Request code */, intent,
          PendingIntent.FLAG_ONE_SHOT);
      notificationBuilder.setContentIntent(pendingIntent);
    }
    NotificationManager notificationManager =
        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    // Since android Oreo notification channel is needed.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel channel = new NotificationChannel(channelId,
          "Channel human readable title",
          NotificationManager.IMPORTANCE_DEFAULT);
      notificationManager.createNotificationChannel(channel);
    }
    notificationManager.notify(id /* ID of notification */, notificationBuilder.build());
  }
}