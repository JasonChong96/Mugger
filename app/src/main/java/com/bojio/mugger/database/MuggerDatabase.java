package com.bojio.mugger.database;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class MuggerDatabase {
  private static String USERS_COLLECTION = "users";
  private static String SEMESTER_COLLECTION = "semesters";
  private static String LISTINGS_COLLECTION = "listings";
  private static String NOTIFICATIONS_COLLECTION = "notifications";
  private static String DATA_COLLECTION = "data";
  private static String MODULE_TITLES_COLLECTION = "moduleTitles";
  private static String MISC_DATA_DOCUMENT = "otherData";
  private static String FEEDBACK_COLLECTION = "feedback";
  private static String REPORTS_COLLECTION = "reports";
  private static String PROF_TA_REQUESTS_COLLECTION = "requestsProfTA";
  private static String CHATS_COLLECTION = "chats";
  private static String CHAT_MESSAGES_COLLECTION = "messages";
  private static String FIELD_INSTANCE_ID = "instanceId";

  public static DocumentReference getUserReference(FirebaseFirestore db, String uid) {
    return getAllUsersReference(db).document(uid);
  }

  public static CollectionReference getAllUsersReference(FirebaseFirestore db) {
    return db.collection(USERS_COLLECTION);
  }

  public static Task<Void> updateInstanceId(FirebaseFirestore db, String uid, String
      instanceId) {
    return getUserReference(db, uid).update(FIELD_INSTANCE_ID, instanceId);
  }

  public static CollectionReference getUserAllSemestersDataReference(FirebaseFirestore db, String uid) {
    return getUserReference(db, uid).collection(SEMESTER_COLLECTION);
  }

  public static DocumentReference getUserSemesterDataReference(FirebaseFirestore db, String uid,
                                                               String semester) {
    return getUserAllSemestersDataReference(db, uid).document(semester.replace("/", "."));
  }

  public static Task<Void> addUserSemesterData(FirebaseFirestore db, String uid,
                                               String semester, Map<String, Object>
                                                   data) {
    return getUserSemesterDataReference(db, uid, semester).set(data);
  }

  public static Task<DocumentReference> sendNotification(FirebaseFirestore db, Map<String, Object>
      notificationData) {
    return db.collection(NOTIFICATIONS_COLLECTION).add(notificationData);
  }

  public static DocumentReference getAllModuleTitlesRef(FirebaseFirestore db) {
    return db.collection(DATA_COLLECTION).document(MODULE_TITLES_COLLECTION);
  }

  public static CollectionReference getAllListingsReference(FirebaseFirestore db) {
    return db.collection(LISTINGS_COLLECTION);
  }

  public static Task<Void> deleteListing(FirebaseFirestore db, String listingUid) {
    return getListingReference(db, listingUid).delete();
  }

  public static Task<DocumentReference> createListing(FirebaseFirestore db, Map<String, Object>
      listingData) {
    return getAllListingsReference(db).add(listingData);
  }

  public static DocumentReference getListingReference(FirebaseFirestore db, String listingUid) {
    return getAllListingsReference(db).document(listingUid);
  }

  public static DocumentReference getOtherDataReference(FirebaseFirestore db) {
    return db.collection(MuggerDatabase.DATA_COLLECTION).document(MISC_DATA_DOCUMENT);
  }

  public static CollectionReference getAllFeedbackReference(FirebaseFirestore db) {
    return db.collection(FEEDBACK_COLLECTION);
  }

  public static Task<DocumentReference> sendFeedback(FirebaseFirestore db, Map<String, Object>
      feedbackData) {
    return getAllFeedbackReference(db).add(feedbackData);
  }

  public static CollectionReference getAllReportsReference(FirebaseFirestore db) {
    return db.collection(REPORTS_COLLECTION);
  }

  public static DocumentReference getReportReference(FirebaseFirestore db, String uid) {
    return getAllReportsReference(db).document(uid);
  }

  public static Task<Void> deleteReport(FirebaseFirestore db, String uid) {
    return getReportReference(db, uid).delete();
  }

  public static Task<DocumentReference> sendReport(FirebaseFirestore db, Map<String, Object>
      reportData) {
    return getAllReportsReference(db).add(reportData);
  }

  public static CollectionReference getAllProfTARequestsReference(FirebaseFirestore db) {
    return db.collection(PROF_TA_REQUESTS_COLLECTION);
  }

  public static Task<DocumentReference> sendProfTARequest(FirebaseFirestore db, Map<String, Object>
      requestData) {
    return getAllProfTARequestsReference(db).add(requestData);
  }

  public static CollectionReference getListingChatHistory(FirebaseFirestore db, String listingUid) {
    return db.collection(CHATS_COLLECTION).document(listingUid)
        .collection(CHAT_MESSAGES_COLLECTION);
  }

  public static Task<DocumentReference> sendListingChatMessage(FirebaseFirestore db, String
      listingUid, Map<String, Object> messageData) {
    return MuggerDatabase.getListingChatHistory(db, listingUid).add(messageData);
  }
}
