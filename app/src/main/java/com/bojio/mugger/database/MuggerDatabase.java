package com.bojio.mugger.database;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class MuggerDatabase {
  public static String USERS_COLLECTION = "users";
  public static String SEMESTER_COLLECTION = "semesters";

  public static DocumentReference getUserReference(FirebaseFirestore db, String uid) {
    return db.collection(USERS_COLLECTION).document(uid);
  }

  public static Task<DocumentReference> addNotification(FirebaseFirestore db, Map<String, Object>
      notificationData) {
    return db.collection("notifications").add(notificationData);
  }

  public static DocumentReference getAllModuleTitlesRef(FirebaseFirestore db) {
    return db.collection("data").document("moduleTitles");
  }

}
