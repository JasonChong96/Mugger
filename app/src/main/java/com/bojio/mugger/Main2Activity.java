package com.bojio.mugger;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
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

import com.bojio.mugger.authentication.MuggerUser;
import com.bojio.mugger.listings.CreateEditListingActivity;
import com.bojio.mugger.listings.Listing;
import com.bojio.mugger.listings.fragments.AttendingListingsFragments;
import com.bojio.mugger.listings.fragments.AvailableListingsFragments;
import com.bojio.mugger.listings.fragments.ListingsFragments;
import com.bojio.mugger.listings.fragments.MyListingsFragments;
import com.bojio.mugger.profile.ProfileFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Main2Activity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener,
    ListingsFragments.OnListingsFragmentInteractionListener,
    ProfileFragment.OnProfileFragmentInteractionListener {

  private FirebaseAuth mAuth;
  private FirebaseFirestore db;

  @BindView(R.id.fab)
  FloatingActionButton fab;

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

    NavigationView navigationView = findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);
    setTitle("Listings");
    navigationView.setCheckedItem(R.id.nav_available_listings);
    onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_available_listings));
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
        Intent intent = new Intent(Main2Activity.this, MainActivity
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
    ((TextView) findViewById(R.id.username)).setText(user.getDisplayName());
    ((TextView) findViewById(R.id.email)).setText(user.getEmail());
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
      // If settings clicked
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
        fragment = ProfileFragment.newInstance(mAuth.getCurrentUser().getUid());
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, fragment);
        fab.setVisibility(View.GONE);
        ft.commit();
        setTitle("My Profile");
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
