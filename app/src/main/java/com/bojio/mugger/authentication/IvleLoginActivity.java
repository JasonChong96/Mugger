package com.bojio.mugger.authentication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bojio.mugger.Main2Activity;
import com.bojio.mugger.R;
import com.bojio.mugger.constants.Modules;
import com.google.android.gms.tasks.Task;
import com.google.common.hash.Hashing;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
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

import javax.net.ssl.HttpsURLConnection;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import needle.Needle;

public class IvleLoginActivity extends AppCompatActivity {

  @BindView(R.id.ivle_login)
  WebView webView;

  @BindView(R.id.progressBar4)
  ProgressBar progressBar;

  FirebaseFirestore db;
  String token;
  FirebaseAuth mAuth;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    mAuth = FirebaseAuth.getInstance();
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ivle_login);
    db = FirebaseFirestore.getInstance();
    ButterKnife.bind(this);
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    StrictMode.setThreadPolicy(policy);
    webView.setWebViewClient(new WebViewClient() {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url.startsWith("https://ivle.nus.edu.sg/api/login/muggerapp.com?token=")) {
          webView.setVisibility(View.GONE);
          Needle.onMainThread().execute(() -> {
            AlertDialog dialog = new SpotsDialog
                .Builder()
                .setContext(IvleLoginActivity.this)
                .setMessage("Loading data from IVLE...")
                .setCancelable(false)
                .build();
            dialog.show();
           // Snackbar.make(view, "Please wait while Mugger fetches relevant data.", Snackbar
          //      .LENGTH_SHORT).show();
              });
          token = url.substring(54);
          // Run in parallel so the loading screen still shows for the user while data is loading.
          Needle.onBackgroundThread().execute(() -> {
            Looper.prepare();
            loadDataFromIvle();
          });

          return true;
        }
        return false;
      }
    });
    webView.loadUrl("https://ivle.nus.edu.sg/api/login/?apikey=OEoF4T2bHfpAn85PuAqoN&url=muggerapp.com");
  }

  private void loadDataFromIvle() {
    Map<String, Object> userData = new HashMap<>();
    if (!loadProfile(userData) || userData.get("nusNetId") == null) {
      finish();
      Toast.makeText(this, "An error has occurred please try again later", Toast.LENGTH_LONG)
          .show();
      return;
    }
    String nusNetId = (String) userData.get("nusNetId");
    userData.put("nusNetId",
        Hashing.sha256()
            .hashString(nusNetId, StandardCharsets.UTF_8)
            .toString());
    if (!getModules(nusNetId, userData) && mAuth.getCurrentUser() == null) {
      finish();
      Toast.makeText(this, "An error has occurred please try again later", Toast.LENGTH_LONG)
          .show();
      return;
    }
    db.collection("users").document(FirebaseAuth.getInstance().getUid()).set(userData,
        SetOptions.merge()).addOnCompleteListener(task -> {
          if (!task.isSuccessful()) {
            Toast.makeText(IvleLoginActivity.this, "An error has occurred please try again " +
                "later", Toast.LENGTH_LONG)
                .show();
            IvleLoginActivity.this.finish();
            return;
          } else {
            Intent intent = new Intent(this, Main2Activity.class);
            // Clears back stack
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
          }
    });
    MuggerUser.getInstance().setData(userData);
  }

  private boolean loadProfile(Map<String, Object> userData) {
    String https_url =
        "https://ivle.nus.edu.sg/api/Lapi.svc/Profile_View?APIKey=OEoF4T2bHfpAn85PuAqoN&AuthToken=";
    URL url;
    try {

      url = new URL(https_url + token);
      HttpsURLConnection con = (HttpsURLConnection)url.openConnection();

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

  private boolean getModules(String nusNetId, Map<String, Object> userData) {
    String https_url_modules =
        new StringBuilder().append("https://ivle.nus.edu.sg/api/Lapi")
            .append(".svc/Modules_Taken?APIKey=OEoF4T2bHfpAn85PuAqoN&StudentID=")
            .append(nusNetId)
            .append("&AuthToken=")
            .toString();
    URL url;
    try {

      url = new URL(https_url_modules + token);
      HttpsURLConnection con = (HttpsURLConnection)url.openConnection();

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
          year = year.replace("/", ".");
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
        Task<?> task = db.collection("users").document(FirebaseAuth.getInstance().getUid())
            .collection("semesters").document(entry.getKey()).set(data);
      }
      String latestSem = Collections.max(modulesBySem.keySet());
      userData.put("latestSem", latestSem);
      Task<?> task = db.collection("data").document("moduleTitles").set(modules, SetOptions
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
  }
}
