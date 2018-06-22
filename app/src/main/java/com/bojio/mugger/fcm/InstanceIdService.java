package com.bojio.mugger.fcm;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class InstanceIdService extends FirebaseInstanceIdService {

  public static void updateToken() {
    String instanceId = FirebaseInstanceId.getInstance().getToken();
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    if (firebaseUser != null) {
      FirebaseFirestore.getInstance()
          .collection("users")
          .document(firebaseUser.getUid())
          .update("instanceId", instanceId);
    }

  }

  @Override
  public void onTokenRefresh() {
    super.onTokenRefresh();
    //Log.d("@@@@", "onTokenRefresh: " + instanceId);
    updateToken();
  }
}