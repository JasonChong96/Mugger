package com.bojio.mugger.administration.feedback;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

public class Feedback {
  private String uid;
  private String userUid;
  private String title;
  private String description;
  private String userName;
  private long time;
  private DocumentReference docRef;

  private Feedback(String uid, String userUid, String title, String description, String userName,
                   Long time, DocumentReference docRef) {
    this.uid = uid;
    this.title = title;
    this.userUid = userUid;
    this.description = description;
    this.userName = userName;
    this.time = time == null ? 0 : time;
    this.docRef = docRef;
  }

  /**
   * Loads a feedback instance from an input DocumentSnapshot and returns it.
   * @param snapshot the document snapshot to load from
   * @return a Feedback instance
   */
  public static Feedback getFeedbackFromSnapshot(DocumentSnapshot snapshot) {
    return new Feedback(snapshot.getId(), (String) snapshot.get("userUid"),
        (String) snapshot.get("title"), (String) snapshot.get("description"), (String) snapshot
        .get("userName"), (Long) snapshot.get("time"), snapshot.getReference());
  }

  public String getUserName() {
    return userName;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public String getUserUid() {
    return userUid;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public DocumentReference getDocRef() {
    return docRef;
  }
}
