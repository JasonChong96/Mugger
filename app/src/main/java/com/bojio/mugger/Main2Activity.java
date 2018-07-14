package com.bojio.mugger;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
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
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bojio.mugger.administration.feedback.MakeFeedbackActivity;
import com.bojio.mugger.administration.feedback.ViewAllFeedbackActivity;
import com.bojio.mugger.administration.reports.ViewAllReportsActivity;
import com.bojio.mugger.administration.requests.MakeProfTARequestActivity;
import com.bojio.mugger.administration.requests.ViewAllProfTARequestActivity;
import com.bojio.mugger.authentication.IvleLoginActivity;
import com.bojio.mugger.authentication.LoggedInActivity;
import com.bojio.mugger.constants.MuggerConstants;
import com.bojio.mugger.database.MuggerDatabase;
import com.bojio.mugger.introduction.MuggerIntroActivity;
import com.bojio.mugger.lifecycle.LifecycleUtils;
import com.bojio.mugger.listings.CreateEditListingActivity;
import com.bojio.mugger.listings.Listing;
import com.bojio.mugger.listings.fragments.AttendingListingsFragments;
import com.bojio.mugger.listings.fragments.AvailableListingsFragments;
import com.bojio.mugger.listings.fragments.CustomFilterListingsFragments;
import com.bojio.mugger.listings.fragments.ListingsFragments;
import com.bojio.mugger.listings.fragments.MyListingsFragments;
import com.bojio.mugger.profile.ProfileActivity;
import com.bojio.mugger.profile.ProfileFragment;
import com.bojio.mugger.settings.SettingsActivity2;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;
import needle.Needle;

public class Main2Activity extends LoggedInActivity
    implements NavigationView.OnNavigationItemSelectedListener,
    ListingsFragments.OnListingsFragmentInteractionListener,
    ProfileFragment.OnProfileFragmentInteractionListener {

  private static int REQUEST_CODE_INTRO = 0;

  @BindView(R.id.fab)
  FloatingActionButton fab;
  @BindView(android.R.id.content)
  View activityView;
  private Main2ActivityViewModel mViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (stopActivity) {
      finish();
      return;
    }
    mViewModel = ViewModelProviders.of(this, LifecycleUtils.getAndroidViewModelFactory
        (getApplication())).get(Main2ActivityViewModel.class);
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    MuggerDatabase.getOtherDataReference(db).get().addOnCompleteListener(task -> {
      if (task.isSuccessful()) {
        long min = (Long) task.getResult().getData().get("minVersion");
        if (MuggerConstants.APP_VERSION < min) {
          mViewModel.signOut();
          Toasty.error(this, "This version is outdated, please update the app through Google " +
              "Play", Toast.LENGTH_LONG).show();
        }
      } else {
        mViewModel.signOut();
        Toasty.error(this, "Error loading application version", Toast.LENGTH_LONG).show();
      }
    });
    // Set behavior when logged in state changes
    setContentView(R.layout.activity_main2);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryAdmin));
    ButterKnife.bind(this);
    DrawerLayout drawer = findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();
    mViewModel.getLiveTitle().observe(this, this::setTitle);
    if (!mViewModel.isModulesLoaded()) {
      AlertDialog dialog = new SpotsDialog
          .Builder()
          .setContext(this)
          .setMessage("Loading module data and notifications...")
          .setCancelable(false)
          .setTheme(R.style.SpotsDialog)
          .build();
      dialog.show();
      Needle.onBackgroundThread().execute(() -> {
        if (mViewModel.loadModuleData()) {
          Needle.onMainThread().execute(() -> {
            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            if (savedInstanceState == null) {
              navigationView.setCheckedItem(R.id.nav_available_listings);
              onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_available_listings));
            }
            if (mViewModel.shouldShowIntro()) {
              startIntroActivity(true);
            }
            dialog.dismiss();
          });
        } else {
          Toasty.error(this, "Unable to load module data, please log in again.")
              .show();
          mViewModel.signOut();
        }
      });
    } else {
      NavigationView navigationView = findViewById(R.id.nav_view);
      navigationView.setNavigationItemSelectedListener(this);
      if (savedInstanceState == null) {
        navigationView.setCheckedItem(R.id.nav_available_listings);
        onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_available_listings));
      }
    }


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
   *
   * @param menu the menu
   * @return true
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main2, menu);
    // ((TextView) findViewById(R.id.username)).setText(mViewModel.getUserName());
    mViewModel.getLiveDisplayName().observe(this, name -> {
      ((TextView) findViewById(R.id.username)).setText(name);
    });
    ((TextView) findViewById(R.id.email)).setText(mViewModel.getEmail());
    if (mViewModel.isModeratorToolsVisible()) {
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
    } else if (id == R.id.action_refresh_modules) {
      startActivity(new Intent(this, IvleLoginActivity.class));
      finish();
    } else if (id == R.id.action_view_introduction) {
      startIntroActivity(false);
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
        mViewModel.signOut();
        break;
      case R.id.nav_available_listings:
        Fragment fragment = new AvailableListingsFragments();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, fragment);
        ft.commit();
        mViewModel.updateTitle("Study Sessions");
        break;
      case R.id.nav_my_listings:
        fragment = new MyListingsFragments();
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, fragment);
        ft.commit();
        mViewModel.updateTitle("My Listings");
        break;
      case R.id.nav_joining_listings:
        fragment = new AttendingListingsFragments();
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, fragment);
        ft.commit();
        mViewModel.updateTitle("Sessions I'm Joining");
        break;
      case R.id.nav_custom_filters:
        fragment = new CustomFilterListingsFragments();
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, fragment);
        ft.commit();
        mViewModel.updateTitle("Filtered Sessions");
        break;
      case R.id.nav_profile:
        Intent intent = new Intent(this, ProfileActivity.class);
        Bundle b = new Bundle();
        b.putString("profileUid", mViewModel.getUserUid());
        intent.putExtras(b);
        startActivity(intent);
        break;
      case R.id.nav_submit_feedback:
        intent = new Intent(this, MakeFeedbackActivity.class);
        startActivity(intent);
        break;
      case R.id.nav_request_role:
        intent = new Intent(this, MakeProfTARequestActivity.class);
        startActivity(intent);
        break;
      case R.id.nav_admin_tools:
        if (mViewModel.isModeratorToolsVisible()) {
          String[] moderator = {"View Reports"};
          String[] admin = {"View Reports", "View Feedback", "View Prof/TA Requests"};
          new MaterialDialog.Builder(this).title("Which Administrative Tool would you like to " +
              "access?").items(mViewModel.isAdminToolsVisible() ? admin
              : moderator).itemsCallback((dialog, itemView, position, text) -> {
            switch (position) {
              case 0:
                if (mViewModel.isModeratorToolsVisible()) {
                  startActivity(new Intent(this, ViewAllReportsActivity.class));
                }
                break;
              case 1:
                if (mViewModel.isAdminToolsVisible()) {
                  startActivity(new Intent(this, ViewAllFeedbackActivity.class));
                }
                break;
              case 2:
                if (mViewModel.isAdminToolsVisible()) {
                  startActivity(new Intent(this, ViewAllProfTARequestActivity.class));
                }
                break;
              default:
                Snackbar.make(activityView, "Not implemented yet.", Snackbar.LENGTH_SHORT).show();
                break;
            }
          }).build().show();
        }
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

  /**
   * Starts the Introduction Slides Activity
   */
  private void startIntroActivity(boolean callback) {
    Intent intent = new Intent(this, MuggerIntroActivity.class);
    if (callback) {
      startActivityForResult(intent, REQUEST_CODE_INTRO);
    } else {
      startActivity(intent);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CODE_INTRO) {
      if (resultCode == RESULT_OK) {
        mViewModel.onIntroComplete();
      }
    }
  }
}
