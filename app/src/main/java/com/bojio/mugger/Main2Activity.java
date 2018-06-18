package com.bojio.mugger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bojio.mugger.administration.feedback.MakeFeedbackActivity;
import com.bojio.mugger.administration.feedback.ViewAllFeedbackActivity;
import com.bojio.mugger.authentication.IvleLoginActivity;
import com.bojio.mugger.authentication.MuggerUser;
import com.bojio.mugger.constants.ModuleRole;
import com.bojio.mugger.constants.MuggerRole;
import com.bojio.mugger.listings.CreateEditListingActivity;
import com.bojio.mugger.listings.Listing;
import com.bojio.mugger.listings.fragments.AttendingListingsFragments;
import com.bojio.mugger.listings.fragments.AvailableListingsFragments;
import com.bojio.mugger.listings.fragments.ListingsFragments;
import com.bojio.mugger.listings.fragments.MyListingsFragments;
import com.bojio.mugger.profile.ProfileActivity;
import com.bojio.mugger.profile.ProfileFragment;
import com.bojio.mugger.settings.SettingsActivity;
import com.bojio.mugger.settings.SettingsActivity2;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;

public class Main2Activity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener,
    ListingsFragments.OnListingsFragmentInteractionListener,
    ProfileFragment.OnProfileFragmentInteractionListener {

  private FirebaseAuth mAuth;
  private FirebaseFirestore db;

  @BindView(R.id.fab)
  FloatingActionButton fab;

  @BindView(android.R.id.content)
  View activityView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    mAuth = FirebaseAuth.getInstance();
    db = FirebaseFirestore.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();
    if (user == null) { // Not logged in, go back to login
      backToHome();
      return;
    }
    // Updates cached display name/email
    db.collection("users").document(user.getUid()).update("displayName", user.getDisplayName());
    db.collection("users").document(user.getUid()).update("email", user.getEmail());

    // Set behavior when logged in state changes
    setAuthStateChangeListener();
    if (MuggerUser.getInstance().getModules() == null) {
      AlertDialog dialog = new SpotsDialog
          .Builder()
          .setContext(this)
          .setMessage("Loading module data and notifications...")
          .setCancelable(false)
          .build();
      dialog.show();
      db.collection("users").document(user.getUid()).collection("semesters").get()
          .addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
              Toast.makeText(this, "Unable to load module data, please log in again.", Toast
                  .LENGTH_SHORT).show();
            } else {
              List<DocumentSnapshot> docs = task.getResult().getDocuments();
              TreeMap<String, TreeMap<String, Byte>> modules = new TreeMap<>(Collections.reverseOrder());
              for (DocumentSnapshot doc : docs) {
                TreeMap<String, Byte> mods = new TreeMap<>();
                modules.put(doc.getId().replace(".", "/"), mods);
                for (String mod : (List<String>) doc.get("moduleCodes")) {
                  mods.put(mod, ModuleRole.EMPTY);
                }
                List<String> ta = (List<String>) doc.get("ta");
                if (ta != null) {
                  for (String mod : ta) {
                    mods.put(mod, ModuleRole.TEACHING_ASSISTANT);
                  }
                }
                List<String> prof = (List<String>) doc.get("professor");
                if (prof != null) {
                  for (String mod : (List<String>) doc.get("professor")) {
                    mods.put(mod, ModuleRole.PROFESSOR);
                  }
                }
              }
              MuggerUser.getInstance().setModules(modules);
              for (String mod : modules.firstEntry().getValue().keySet()) {
                FirebaseMessaging.getInstance().subscribeToTopic(mod);
              }
              NavigationView navigationView = findViewById(R.id.nav_view);
              navigationView.setNavigationItemSelectedListener(this);
              setTitle("Listings");
              navigationView.setCheckedItem(R.id.nav_available_listings);
              onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_available_listings));
            }
            dialog.dismiss();
          });
    }
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main2);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    ButterKnife.bind(this);
    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();


    String instanceId = FirebaseInstanceId.getInstance().getToken();
    // Update instance id of this account in database
    if (instanceId != null) {
      db.collection("users")
          .document(user.getUid())
          .update("instanceId", instanceId);
    }
    // Subscribe to chat notifications
    subscribeToTopics();
  }

  /**
   * Sets the function to be invoked when log in stage is changed. i.e when user has signed out,
   * bring him back to the login page and unsubscribe him from notifications
   */
  private void setAuthStateChangeListener() {
    mAuth.addAuthStateListener(firebaseAuth -> {
      if (firebaseAuth.getCurrentUser() == null) {
        finish();
        try {
          FirebaseInstanceId.getInstance().deleteInstanceId();
        } catch (IOException e) {
          e.printStackTrace();
        }
        Intent intent = new Intent(this, MainActivity
            .class);
        Toast.makeText(Main2Activity.this, "Logged out " +
            "successfully", Toast.LENGTH_LONG).show();
        startActivity(intent);
      }
    });
  }
  /**
   * Subscribes this client to the relevant listing notifications.
   */
  private void subscribeToTopics() {
    if (mAuth == null) {
      return;
    }
    Query q  = db.collection("listings")
        .whereGreaterThan(mAuth.getUid(), 0);
    q.get().addOnCompleteListener(snap -> {
      List<DocumentSnapshot> results = snap.getResult().getDocuments();
      for (DocumentSnapshot doc : results) {
        FirebaseMessaging.getInstance().subscribeToTopic(doc.getId());
      }
    });
  }

  /**
   * Goes back to the start page of Mugger
   */
  private void backToHome() {
    Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);
    finish();
    return;
  }

  /**
   * Invoked when back button is pressed. If the navigation drawer is opened, closes it. If not,
   * normal behavior is invoked.
   */
  @Override
  public void onBackPressed() {
    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  /**
   * Creates the options menu (top right). Also sets the notification drawer display name and email.
   * @param menu the menu
   * @return
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main2, menu);
    FirebaseUser user = mAuth.getCurrentUser();
    if (MuggerRole.MODERATOR.check(MuggerUser.getInstance().getRole())) {
      MenuItem menuItem = ((NavigationView) findViewById(R.id.nav_view)).getMenu()
          .findItem(R.id.nav_admin_tools);
      menuItem.setVisible(true);
    }
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle top right menu item clicks
    int id = item.getItemId();

    if (id == R.id.action_settings) {
      Intent intent = new Intent(this, SettingsActivity2.class);
      startActivity(intent);
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("StatementWithEmptyBody")
  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    // Handle navigation drawer item clicks here.
    int id = item.getItemId();
    switch (id) {
      case R.id.logout:
        this.signOut();
        break;
      case R.id.nav_available_listings:
        Fragment fragment = new AvailableListingsFragments();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, fragment);
        fab.setVisibility(View.VISIBLE);
        ft.commit();
        setTitle("Listings");
        break;
      case R.id.nav_my_listings:
        fragment = new MyListingsFragments();
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, fragment);
        fab.setVisibility(View.VISIBLE);
        ft.commit();
        setTitle("My Listings");
        break;
      case R.id.nav_joining_listings:
        fragment = new AttendingListingsFragments();
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, fragment);
        fab.setVisibility(View.GONE);
        ft.commit();
        setTitle("Listings That I'm Joining");
        break;
      case R.id.nav_profile:
        Intent intent = new Intent(this, ProfileActivity.class);
        Bundle b = new Bundle();
        b.putString("profileUid", mAuth.getUid());
        intent.putExtras(b);
        startActivity(intent);
        break;
      case R.id.refresh_logout:
        db.collection("users").document(mAuth.getUid()).update("nusNetId", FieldValue.delete())
            .addOnCompleteListener(task -> {
              signOut();
              Toast.makeText(this, "Refreshed successfully", Toast.LENGTH_SHORT).show();
            });
        break;
      case R.id.submit_feedback:
        intent = new Intent(this, MakeFeedbackActivity.class);
        startActivity(intent);
        break;
      case R.id.nav_admin_tools:
        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Administrative Tools Available");
        builder.setItems(new CharSequence[]
                {"View Reports", "View Feedback", "View Prof/TA Requests"},
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position
                // of the selected item
                switch (which) {
                  case 0:
                    Toast.makeText(Main2Activity.this, "clicked 1", Toast.LENGTH_SHORT).show();
                    break;
                  case 1:
                    Toast.makeText(Main2Activity.this, "clicked 2", Toast.LENGTH_SHORT).show();
                    break;
                  case 2:
                    Toast.makeText(Main2Activity.this, "clicked 3", Toast.LENGTH_SHORT).show();
                    break;
                }
              }
            });
        builder.create().show();*/
        new MaterialDialog.Builder(this).title("Which Administrative Tool would you like to " +
            "access?").items("View Reports", "View Feedback", "View Prof/TA Requests")
            .itemsCallback((dialog, itemView, position, text) -> {
              switch (position) {
                case 1:
                  startActivity(new Intent(this, ViewAllFeedbackActivity.class));
                  break;
                default:
                  Snackbar.make(activityView, "Not implemented yet.", Snackbar.LENGTH_SHORT).show();
                  break;
              }
            }).build().show();
        break;
      default:
        break;
    }

    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  @Override
  public void onListingFragmentInteraction(Listing listing) {
    Toast.makeText(this, "Clicked...", Toast.LENGTH_SHORT).show();
  }

  /**
   * Signs out of the current account.
   */
  private void signOut() {
    // Firebase sign out
    mAuth.signOut();

    // Google sign out
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("292230336625-pa93l9untqrvad2mc6m3i77kckjkk4k1.apps.googleusercontent.com")
        .requestEmail()
        .build();
    GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    mGoogleSignInClient.signOut();

    MuggerUser.clear();
  }

  /**
   * Invoked when Floating Action Button is clicked. Opens the create listing UI.
   */
  @OnClick(R.id.fab)
  void onClickFab() {
    Intent intent = new Intent(this, CreateEditListingActivity.class);
    startActivity(intent);
  }

  @Override
  public void onProfileFragmentInteraction(Uri uri) {

  }
}
