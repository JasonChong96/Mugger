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
import android.service.notification.NotificationListenerService;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bojio.mugger.CustomSettings;
import com.bojio.mugger.MainActivity;
import com.bojio.mugger.R;
import com.bojio.mugger.listings.chat.ListingChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MessagingService extends FirebaseMessagingService {

  private static final String TAG = "MessagingService";
  private static AtomicInteger notificationId = new AtomicInteger(0);
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

    // Check if message contains a notification payload.
    if (data.get("notification") != null) {
      String senderUid = data.get("senderUid");
      if (CustomSettings.NOTIFICATION_TO_SELF || senderUid == null ||
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
    int id = notificationId.getAndIncrement();
    String messageBody = data.get("body");
    String messageTitle = data.get("title");
    Intent intent = new Intent(this, ListingChatActivity.class);
    Bundle b = new Bundle();
    b.putString("listingUid", data.get("listingUid"));
    intent.putExtras(b);
    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    PendingIntent pendingIntent = PendingIntent.getActivity(this, id /* Request code */, intent,
        PendingIntent.FLAG_ONE_SHOT);

    String channelId = "aaa";
    Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    NotificationCompat.Builder groupBuilder =
        new NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(messageTitle)
            .setContentText("a")
            .setGroupSummary(true)
            .setGroup(data.get("listingUid"))
            .setStyle(new NotificationCompat.BigTextStyle().bigText("a"))
            .setContentIntent(pendingIntent);
    NotificationCompat.Builder notificationBuilder =
        new NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(messageTitle)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(data.get("listingUid"));

    NotificationManager notificationManager =
        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    // Since android Oreo notification channel is needed.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel channel = new NotificationChannel(channelId,
          "Channel human readable title",
          NotificationManager.IMPORTANCE_DEFAULT);
      notificationManager.createNotificationChannel(channel);
    }
    notificationManager.notify(id /* ID of notification */, groupBuilder.build());
    notificationManager.notify(id /* ID of notification */, notificationBuilder.build());
  }
}