package com.bojio.mugger.settings;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.bojio.mugger.R;
import com.bojio.mugger.authentication.MuggerUser;
import com.bojio.mugger.fcm.MessagingService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import dmax.dialog.SpotsDialog;

public class SettingsActivity2 extends AppCompatPreferenceActivity {
  private static final String TAG = SettingsActivity2.class.getSimpleName();
  private FirebaseAuth mAuth;
  private FirebaseUser user;
  private static AlertDialog dialog;
  private static Snackbar snackbar;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    mAuth = FirebaseAuth.getInstance();
    // load settings fragment
    PreferenceFragment fragment = new MainPreferenceFragment();
    getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
      dialog = new SpotsDialog
          .Builder()
          .setContext(this)
          .setCancelable(false)
          .build();
      snackbar = Snackbar.make(findViewById(android.R.id.content), "", Snackbar.LENGTH_SHORT);
  }

  public static class MainPreferenceFragment extends PreferenceFragment {
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.pref_main);


      // gallery EditText change listener
     // bindPreferenceSummaryToValue(findPreference(getString(R.string.key_gallery_name)));

      // notification preference change listener
    //  bindPreferenceSummaryToValue(findPreference(getString(R.string
       //   .key_notifications_new_message_ringtone)));

      // feedback preference click listener
   /*   Preference myPref = findPreference(getString(R.string.key_send_feedback));
      myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
          sendFeedback(getActivity());
          return true;
        }
      });*/
      mAuth = FirebaseAuth.getInstance();
      EditTextPreference changeName = (EditTextPreference) findPreference
          (getString(R.string.settings_key_change_display_name));
      user = mAuth.getCurrentUser();
      MuggerUser muggerUser = MuggerUser.getInstance();
      bindPreferenceSummaryToValue(changeName);
      changeName.setText(user.getDisplayName());
      changeName.setSummary(user.getDisplayName());

      SwitchPreference deleteNotifSwitch = (SwitchPreference) findPreference(getString(R.string
          .settings_key_toggle_delete_notifications));
      deleteNotifSwitch.setOnPreferenceChangeListener(sBindSwitchPreferenceListener);
      Long deleteSettings = (Long) muggerUser.getData().get(MessagingService.DELETED_NOTIFICATION);
      if (deleteSettings == null) {
        deleteSettings = 1L;
      }
      deleteNotifSwitch.setChecked(!deleteSettings.equals(Long.valueOf(0)));

      SwitchPreference chatNotifSwitch = (SwitchPreference) findPreference(getString(R.string
          .settings_key_toggle_chat_notifications));
      chatNotifSwitch.setOnPreferenceChangeListener(sBindSwitchPreferenceListener);
      Long chatSettings = (Long) muggerUser.getData().get(MessagingService.CHAT_NOTIFICATION);
      if (chatSettings == null) {
        chatSettings = 1L;
      }
      chatNotifSwitch.setChecked(!chatSettings.equals(Long.valueOf(0)));

      SwitchPreference createNotifSwitch = (SwitchPreference) findPreference(getString(R.string
          .settings_key_toggle_create_notifications));
      createNotifSwitch.setOnPreferenceChangeListener(sBindSwitchPreferenceListener);
      Long createSettings = (Long) muggerUser.getData().get(MessagingService.CREATED_NOTIFICATION);
      if (createSettings == null) {
        createSettings = 1L;
      }
      chatNotifSwitch.setChecked(!createSettings.equals(Long.valueOf(0)));
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
    }
    return super.onOptionsItemSelected(item);
  }

  private static void bindPreferenceSummaryToValue(Preference preference) {
    preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

  }

  private static Preference.OnPreferenceChangeListener sBindSwitchPreferenceListener = new Preference.OnPreferenceChangeListener() {
    private FirebaseUser user;
    private FirebaseFirestore db;
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
      dialog.setMessage("Updating Settings...");
      dialog.show();
      if (user == null) {
        user = FirebaseAuth.getInstance().getCurrentUser();
      }
      if (db == null) {
        db = FirebaseFirestore.getInstance();
      }
      boolean isChecked = (Boolean) newValue;
      String type;
      if (preference.getKey().equals("toggle_delete_notifications")) {
        type = MessagingService.DELETED_NOTIFICATION;
      } else if (preference.getKey().equals("toggle_create_notifications")) {
        type = MessagingService.CREATED_NOTIFICATION;
      } else if (preference.getKey().equals("toggle_chat_notifications")) {
        type = MessagingService.CHAT_NOTIFICATION;
      } else {
        return false;
      }
      db.collection("users").document(user.getUid()).update(type, isChecked ? 1L : 0L)
          .addOnCompleteListener(task -> {
            MuggerUser.getInstance().getData().put(type, isChecked ? 1L : 0L);
            dialog.dismiss();
          });
      return true;
    }
  };

  /**
   * A preference value change listener that updates the preference's summary
   * to reflect its new value.
   */
  private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
    private FirebaseUser user;
    private FirebaseFirestore db;

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
      String stringValue = newValue.toString();
      if (user == null) {
        user = FirebaseAuth.getInstance().getCurrentUser();
      }
      if (db == null) {
        db = FirebaseFirestore.getInstance();
      }

      if (preference instanceof ListPreference) {
        // For list preferences, look up the correct display value in
        // the preference's 'entries' list.
        ListPreference listPreference = (ListPreference) preference;
        int index = listPreference.findIndexOfValue(stringValue);

        // Set the summary to reflect the new value.
        preference.setSummary(
            index >= 0
                ? listPreference.getEntries()[index]
                : null);

      } else if (preference instanceof RingtonePreference) {
        // For ringtone preferences, look up the correct display value
        // using RingtoneManager.
        if (TextUtils.isEmpty(stringValue)) {
          // Empty values correspond to 'silent' (no ringtone).
          preference.setSummary(R.string.pref_ringtone_silent);

        } else {
          Ringtone ringtone = RingtoneManager.getRingtone(
              preference.getContext(), Uri.parse(stringValue));

          if (ringtone == null) {
            // Clear the summary if there was a lookup error.
          //  preference.setSummary(R.string.summary_choose_ringtone);
          } else {
            // Set the summary to reflect the new ringtone display
            // name.
            String name = ringtone.getTitle(preference.getContext());
            preference.setSummary(name);
          }
        }

      } else if (preference instanceof EditTextPreference) {
        if (preference.getKey().equals("key_gallery_name")) {
          // update the changed gallery name to summary filed
          preference.setSummary(stringValue);
        } else if (preference.getKey().equals("change_display_name")) {
          dialog.setMessage("Changing display name...");
          preference.setSummary(stringValue);
          dialog.show();
          user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(stringValue)
              .build()).addOnCompleteListener((task) -> {
            if (!task.isSuccessful()) {
              dialog.dismiss();
              snackbar.setText("Failed to change display name, please try again later");
              snackbar.show();
              preference.setSummary(user.getDisplayName());
            } else {
              db.collection("users").document(user.getUid()).update("displayName", stringValue)
                  .addOnCompleteListener(task2 -> {
                    if (!task2.isSuccessful()) {
                      snackbar.setText("Failed to change display name, please try again later")
                          .show();
                      dialog.dismiss();
                      preference.setSummary(user.getDisplayName());
                    } else {
                      Query mQuery = db.collection("listings")
                          .orderBy(user.getUid());
                      mQuery.get().addOnCompleteListener(taskk -> {
                        dialog.dismiss();
                        if (!taskk.isSuccessful()) {
                          snackbar.setText("Failed to change display name, please try again later")
                              .show();
                        } else {
                          WriteBatch batch = db.batch();
                          for (DocumentSnapshot snap : taskk.getResult().getDocuments()) {
                            batch.update(snap.getReference(), "ownerName", stringValue);
                          }

                          batch.commit().addOnCompleteListener(taskkk -> {
                            if (!taskk.isSuccessful()) {
                              snackbar.setText("Failed to change display name, please try again later")
                                  .show();
                            } else {
                              snackbar.setText("Your display name has been changed successfully").show();
                            }
                          });
                        }
                      });
                      preference.setSummary(user.getDisplayName());
                    }
                  });
            }
          });

        }
      } else {
        preference.setSummary(stringValue);
      }
      return true;
    }
  };

  /**
   * Email client intent to send support mail
   * Appends the necessary device information to email body
   * useful when providing support
   */
  public static void sendFeedback(Context context) {
    String body = null;
    try {
      body = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
      body = "\n\n-----------------------------\nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
          Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
          "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
    } catch (PackageManager.NameNotFoundException e) {
    }
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("message/rfc822");
    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"contact@androidhive.info"});
    intent.putExtra(Intent.EXTRA_SUBJECT, "Query from android app");
    intent.putExtra(Intent.EXTRA_TEXT, body);
    //context.startActivity(Intent.createChooser(intent, context.getString(R.string
    //    .choose_email_client)));
  }
}