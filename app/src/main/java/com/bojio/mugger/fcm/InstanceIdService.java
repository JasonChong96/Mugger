package com.bojio.mugger.fcm;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class InstanceIdService extends FirebaseInstanceIdService {

  @Override
  public void onTokenRefresh() {
    super.onTokenRefresh();
    String instanceId = FirebaseInstanceId.getInstance().getToken();
    Log.d("@@@@", "onTokenRefresh: " + instanceId);
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    if (firebaseUser != null) {
      FirebaseFirestore.getInstance()
          .collection("users")
          .document(firebaseUser.getUid())
          .update("instanceId", instanceId);
    }
  }
}