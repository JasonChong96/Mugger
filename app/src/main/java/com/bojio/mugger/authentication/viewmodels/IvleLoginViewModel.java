package com.bojio.mugger.authentication.viewmodels;

import android.arch.lifecycle.ViewModel;
import android.content.Intent;
import android.os.Bundle;

import com.bojio.mugger.Main2Activity;
import com.bojio.mugger.MainActivity;
import com.bojio.mugger.authentication.IvleLoginActivity;
import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.constants.Modules;
import com.bojio.mugger.database.MuggerDatabase;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.common.hash.Hashing;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class IvleLoginViewModel extends ViewModel {
 /* private FirebaseAuth mAuth;
  private FirebaseFirestore db;
  private MuggerUserCache cache;

  public IvleLoginViewModel() {
    mAuth = FirebaseAuth.getInstance();
    db = FirebaseFirestore.getInstance();
    cache = MuggerUserCache.getInstance();
  }

  public boolean loadDataFromIvle(String token) {
    Map<String, Object> userData = new HashMap<>();
    if (!loadProfile(userData, token) || userData.get("nusNetId") == null) {
      onError();
      return;
    }
    String nusNetId = (String) userData.get("nusNetId");
    String hashedId = Hashing.sha256().hashString(nusNetId, StandardCharsets.UTF_8).toString();
    Task<QuerySnapshot> checkTask = MuggerDatabase.getAllUsersReference(db).whereEqualTo
        ("nusNetId", hashedId)
        .get();
    try {
      Tasks.await(checkTask);
    } catch (ExecutionException | InterruptedException e) {
      onError();
      return;
    }
    if (checkTask.isSuccessful()) {
      List<DocumentSnapshot> snaps = checkTask.getResult().getDocuments();
      if (snaps.size() > 1 || (snaps.size() > 0 && !mAuth.getUid().equals(snaps.get(0).getId()))) {
        Intent intent = new Intent(this, MainActivity.class);
        // Clears back stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle b = new Bundle();
        b.putString("errorMessage", "Your IVLE account is already tagged to another Mugger account. " +
            "You are only allowed one account per person.");
        intent.putExtras(b);
        startActivity(intent);
        finish();
        return;
      }
    } else {
      onError();
      return;
    }
    userData.put("nusNetId", hashedId);
    if (!getModules(nusNetId, userData) && mAuth.getCurrentUser() == null) {
      onError();
      return;
    }
    MuggerDatabase.getUserReference(db, FirebaseAuth.getInstance().getUid()).set(userData,
        SetOptions.merge()).addOnCompleteListener(task -> {
      if (!task.isSuccessful()) {
        onError();
        return;
      } else {
        MuggerDatabase.getUserReference(db, FirebaseAuth.getInstance().getUid()).get()
            .addOnCompleteListener(taskk -> {
              if (!task.isSuccessful()) {
                onError();
                return;
              } else {
                MuggerUserCache.getInstance().setData(taskk.getResult().getData());
                Intent intent = new Intent(this, Main2Activity.class);
                // Clears back stack
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
              }
            });

      }
    });
  }

  private boolean loadProfile(Map<String, Object> userData, String token) {
    String https_url =
        "https://ivle.nus.edu.sg/api/Lapi.svc/Profile_View?APIKey=OEoF4T2bHfpAn85PuAqoN&AuthToken=";
    URL url;
    try {

      url = new URL(https_url + token);
      HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

      Scanner sc = new Scanner(con.getInputStream());
      String[] data = sc.nextLine().split("\"");
      for (int i = 0; i < data.length; i++) {
        switch (data[i]) {
          case "UserID":
            userData.put("nusNetId", data[i + 2]);
            break;
          case "Gender":
            userData.put("sex", data[i + 2]);
            break;
          case "Faculty":
            userData.put("faculty", data[i + 2]);
            break;
          case "FirstMajor":
            userData.put("firstMajor", data[i + 2]);
            break;
          case "SecondMajor":
            if (!data[i + 2].isEmpty())
              userData.put("secondMajor", data[i + 2]);
            break;
          case "MatriculationYear":
            userData.put("matriculationYear", data[i + 2]);
            break;
          default:
            break;
        }
      }
      return true;
    } catch (MalformedURLException e) {
      e.printStackTrace();
      return false;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  private boolean getModules(String nusNetId, Map<String, Object> userData, String token) {
    String https_url_modules =
        new StringBuilder().append("https://ivle.nus.edu.sg/api/Lapi")
            .append(".svc/Modules_Taken?APIKey=OEoF4T2bHfpAn85PuAqoN&StudentID=")
            .append(nusNetId)
            .append("&AuthToken=")
            .toString();
    URL url;
    try {

      url = new URL(https_url_modules + token);
      HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

      Scanner sc = new Scanner(con.getInputStream());
      String s = sc.nextLine();
      s = s.substring(2, s.indexOf("]"));
      String[] dataList = s.split("\"");
      Map<String, List<String>> modulesBySem = new HashMap<>();
      Map<String, String> modules = new TreeMap<>();
      for (int i = 0; i < dataList.length; i++) {
        if (dataList[i].equals("ModuleCode")) {
          String moduleCode = dataList[i + 2];
          String moduleTitle = dataList[i + 6];
          if (!Modules.isRelevantModule(moduleCode, moduleTitle)) {
            continue;
          }
          String year = dataList[i + 10];
          String semesterDisplay = dataList[i + 18];
          String yearAndSem = year + " " + semesterDisplay;
          if (!modulesBySem.containsKey(yearAndSem)) {
            modulesBySem.put(yearAndSem, new ArrayList<>());
          }
          modulesBySem.get(yearAndSem).add(moduleCode);
          modules.put(moduleCode, moduleTitle);
          i += 18;
        }
      }
      for (Map.Entry<String, List<String>> entry : modulesBySem.entrySet()) {
        Map<String, Object> data = new HashMap<>();
        data.put("moduleCodes", entry.getValue());
        data.put("semester", entry.getKey());
        Task<?> task = MuggerDatabase.addUserSemesterData(FirebaseFirestore.getInstance(), mAuth
            .getUid(), entry.getKey(), data);
      }
      String latestSem = Collections.max(modulesBySem.keySet());
      userData.put("latestSem", latestSem);
      Task<?> task = MuggerDatabase.getAllModuleTitlesRef(db).set(modules, SetOptions
          .merge());
      userData.put("moduleCodes", new ArrayList<>(modules.keySet()));
      return true;
    } catch (MalformedURLException e) {
      e.printStackTrace();
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return true;
    }
  }*/
}
