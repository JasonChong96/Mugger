package com.bojio.mugger.administration.feedback;

import android.arch.lifecycle.ViewModel;

import com.bojio.mugger.database.MuggerDatabase;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MakeFeedbackViewModel extends ViewModel {
  private FirebaseFirestore db;
  private FirebaseAuth mAuth;

  public MakeFeedbackViewModel() {
    db = FirebaseFirestore.getInstance();
    mAuth = FirebaseAuth.getInstance();
  }

  /**
   * Submits the feedback to database.
   * @param title The feedback title
   * @param description The feedback description
   * @return a Task containing a DocumentReference for the submitting of feedback
   */
  public Task<DocumentReference> submitFeedback(String title, String description) {
    Map<String, Object> feedback = new HashMap<>();
    feedback.put("title", title);
    feedback.put("description", description);
    feedback.put("userUid", mAuth.getUid());
    feedback.put("userName", mAuth.getCurrentUser().getDisplayName());
    feedback.put("time", System.currentTimeMillis());
    return MuggerDatabase.sendFeedback(db, feedback);
  }
}
