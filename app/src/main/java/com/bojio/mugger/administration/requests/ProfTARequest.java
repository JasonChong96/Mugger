package com.bojio.mugger.administration.requests;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

public class ProfTARequest {
  private String userUid;
  private String userName;
  private String role;
  private String description;
  private String moduleCode;
  private long time;
  private DocumentReference docRef;

  private ProfTARequest(String userUid, String userName, String role, String moduleCode, String
      description, long time, DocumentReference docRef) {
    this.userUid = userUid;
    this.userName = userName;
    this.role = role;
    this.moduleCode = moduleCode;
    this.description = description;
    this.time = time;
    this.docRef = docRef;
  }

  public static ProfTARequest getRequestFromSnapshot(DocumentSnapshot snapshot) {
    return new ProfTARequest((String) snapshot.get("userUid"), (String) snapshot.get("userName"),
        (String) snapshot.get("role"), (String) snapshot.get("moduleCode")
        , (String) snapshot.get("description"), snapshot.get("time") == null ? 0 : (Long) snapshot
        .get("time"), snapshot.getReference());
  }

  public String getUserUid() {
    return userUid;
  }

  public void setUserUid(String userUid) {
    this.userUid = userUid;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getModuleCode() {
    return moduleCode;
  }

  public void setModuleCode(String moduleCode) {
    this.moduleCode = moduleCode;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public DocumentReference getDocRef() {
    return docRef;
  }

  public void setDocRef(DocumentReference docRef) {
    this.docRef = docRef;
  }
}
