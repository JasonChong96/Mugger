package com.bojio.mugger;

import android.content.Intent;
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
import com.bojio.mugger.fragments.AttendingListingsFragments;
import com.bojio.mugger.fragments.AvailableListingsFragments;
import com.bojio.mugger.fragments.ListingsFragments;
import com.bojio.mugger.fragments.MyListingsFragments;
import com.bojio.mugger.listings.CreateEditListingActivity;
import com.bojio.mugger.listings.Listing;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Main2Activity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener,
    ListingsFragments.OnListingsFragmentInteractionListener {

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
      Intent intent = new Intent(this, MainActivity.class);
      startActivity(intent);
      finish();
      return;
    }
    db.collection("users").document(user.getUid()).update("displayName", user.getDisplayName());
    mAuth.addAuthStateListener(firebaseAuth -> {
      if (firebaseAuth.getCurrentUser() == null) {
        finish();
        Intent intent = new Intent(Main2Activity.this, MainActivity
            .class);
        Toast.makeText(Main2Activity.this, "Logged out " +
            "successfully", Toast.LENGTH_LONG).show();
        startActivity(intent);
      }
    });
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main2);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    ButterKnife.bind(this);
    fab.setOnClickListener(view -> {
      Intent intent = new Intent(this, CreateEditListingActivity.class);
      startActivity(intent);
    });

    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    NavigationView navigationView = findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);

    navigationView.setCheckedItem(R.id.nav_available_listings);
    onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_available_listings));

  }

  @Override
  public void onBackPressed() {
    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main2, menu);
    FirebaseUser user = mAuth.getCurrentUser();
    ((TextView) findViewById(R.id.username)).setText(user.getDisplayName());
    ((TextView) findViewById(R.id.email)).setText(user.getEmail());
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @SuppressWarnings("StatementWithEmptyBody")
  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    // Handle navigation view item clicks here.
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
        break;
      case R.id.nav_my_listings:
        fragment = new MyListingsFragments();
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, fragment);
        fab.setVisibility(View.VISIBLE);
        ft.commit();
        break;
      case R.id.nav_joining_listings:
        fragment = new AttendingListingsFragments();
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, fragment);
        fab.setVisibility(View.GONE);
        ft.commit();
        break;
      default:
        break;
    }

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  @Override
  public void onListingFragmentInteraction(Listing listing) {
    Toast.makeText(this, "Clicked...", Toast.LENGTH_SHORT).show();
  }

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
}
