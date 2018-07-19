package com.bojio.mugger.administration.requests;

import android.arch.lifecycle.ViewModel;

import com.bojio.mugger.database.MuggerDatabase;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MakeProfTARequestViewModel extends ViewModel {
  private FirebaseFirestore db;
  private FirebaseAuth mAuth;

  public MakeProfTARequestViewModel() {
    db = FirebaseFirestore.getInstance();
    mAuth = FirebaseAuth.getInstance();
  }

  public Task<DocumentReference> submitRequest(String moduleCode, String description, String role) {
    Map<String, Object> request = new HashMap<>();
    request.put("time", System.currentTimeMillis());
    request.put("moduleCode", moduleCode);
    request.put("description", description);
    request.put("role", role);
    request.put("userUid", mAuth.getUid());
    request.put("userName", mAuth.getCurrentUser().getDisplayName());
    return MuggerDatabase.sendProfTARequest(db, request);
  }
}
