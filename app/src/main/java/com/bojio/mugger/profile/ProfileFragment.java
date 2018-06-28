package com.bojio.mugger.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bojio.mugger.R;
import com.bojio.mugger.administration.ChangeMuggerRoleActivity;
import com.bojio.mugger.administration.MakeTAProfActivity;
import com.bojio.mugger.authentication.MuggerUser;
import com.bojio.mugger.authentication.MuggerRole;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.mateware.snacky.Snacky;
import dmax.dialog.SpotsDialog;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnProfileFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
  @BindView(R.id.profile_text_view_name)
  TextView nameView;
  @BindView(R.id.profile_text_view_email)
  TextView emailView;
  @BindView(R.id.profile_image_view_sex)
  ImageView sexView;
  @BindView(R.id.profile_text_view_faculty)
  TextView facultyView;
  @BindView(R.id.profile_text_view_first_major)
  TextView firstMajorView;
  @BindView(R.id.profile_text_view_second_major)
  TextView secondMajorView;
  @BindView(R.id.profile_spinner_select_semester)
  Spinner semesterSpinner;
  @BindView(R.id.profile_text_view_modules)
  TextView modulesView;
  @BindView(R.id.profile_text_view_status)
  TextView statusView;
  @BindView(R.id.profile_plain_text_status)
  EditText editStatusView;
  @BindView(R.id.profile_button_mute)
  Button muteButton;
  @BindView(R.id.profile_button_make_ta_prof)
  Button makeTAProfButton;
  @BindView(R.id.profile_button_ban)
  Button banButton;
  @BindView(R.id.profile_button_change_mugger_role)
  Button changeRoleButton;
  @BindView(R.id.divider4)
  View divider4;
  @BindView(R.id.profile_button_update_status)
  Button updateStatusButton;
  @BindView(R.id.profile_text_view_actions_title)
  TextView adminLabelView;
  @BindView(R.id.profile_plain_text_status_wrapper)
  TextInputLayout statusWrapper;
  private String profileUid;
  private FirebaseFirestore db;
  private FirebaseAuth mAuth;
  private List<List<String>> modulesBySem, modulesBySem_ta, modulesBySem_prof;
  private List<String> semesters;
  private Map<String, Object> moduleTitles;
  private OnProfileFragmentInteractionListener mListener;

  public ProfileFragment() {
    // Required empty public constructor
  }

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @return A new instance of fragment ProfileFragment.
   */
  // TODO: Rename and change types and number of parameters
  public static ProfileFragment newInstance(String profileUid) {
    ProfileFragment fragment = new ProfileFragment();
    Bundle args = new Bundle();
    args.putString("profileUid", profileUid);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    if (getArguments() != null) {
      profileUid = getArguments().getString("profileUid");
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    db = FirebaseFirestore.getInstance();
    mAuth = FirebaseAuth.getInstance();
    super.onCreate(savedInstanceState);
    View view = inflater.inflate(R.layout.fragment_profile, container, false);
    ButterKnife.bind(this, view);
    Task<DocumentSnapshot> profileTask = db.collection("users").document(profileUid).get();
    Task<DocumentSnapshot> moduleTitlesTask = db.collection("data").document("moduleTitles").get();
    Task<QuerySnapshot> modulesTask = db.collection("users").document(profileUid)
        .collection("semesters").orderBy("semester", Query.Direction.DESCENDING).get();
    AlertDialog dialog = new SpotsDialog
        .Builder()
        .setContext(this.getActivity())
        .setTheme(R.style.SpotsDialog)
        .setMessage("Loading profile...")
        .setCancelable(false)
        .build();
    dialog.show();
    Tasks.whenAll(profileTask, modulesTask, moduleTitlesTask).addOnCompleteListener
        (task -> {
          ProfileFragment.this.moduleTitles = moduleTitlesTask.getResult().getData();
          ProfileFragment.this.loadProfile(profileTask.getResult(), modulesTask.getResult().getDocuments
              ());
          dialog.dismiss();
        });
    return view;
  }

  private void loadProfile(DocumentSnapshot profile, List<DocumentSnapshot> moduless) {
    if (getActivity() == null) {
      return;
    }
    Map<String, Object> profileData = profile.getData();
    String displayName = (String) profileData.get("displayName");
    nameView.setText(displayName);
    if (profileData.get("muted") != null && (Long) profileData.get("muted") > System
        .currentTimeMillis()) {
      nameView.setText(String.format("%s (Muted)", nameView.getText().toString()));
    }
    getActivity().setTitle(displayName + "'s Profile");
    emailView.setText((String) profileData.get("email"));
    if (profileData.get("sex").equals("Female")) {
      sexView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.gender_female));
    }
    facultyView.setText((String) profileData.get("faculty"));
    firstMajorView.setText((String) profileData.get("firstMajor"));
    String secondMajor = (String) profileData.get("secondMajor");
    if (secondMajor == null) {
      secondMajorView.setVisibility(View.GONE);
    } else {
      secondMajorView.setText((String) profileData.get("secondMajor"));
    }
    semesters = new ArrayList<>();
    modulesBySem = new ArrayList<>();
    modulesBySem_prof = new ArrayList<>();
    modulesBySem_ta = new ArrayList<>();
    for (DocumentSnapshot snapshot : moduless) {
      semesters.add(snapshot.getId().replace(".", "/"));
      modulesBySem.add((List<String>) snapshot.get("moduleCodes"));
      List<String> ta = (List<String>) snapshot.get("ta");
      if (ta == null) {
        ta = new ArrayList<>();
      }
      modulesBySem_ta.add(ta);
      List<String> prof = (List<String>) snapshot.get("professor");
      if (prof == null) {
        prof = new ArrayList<>();
      }
      modulesBySem_prof.add(prof);
    }
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout
        .simple_dropdown_item_1line,
        semesters);
    semesterSpinner.setAdapter(adapter);
    semesterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        StringBuilder sb = new StringBuilder();
        List<String> mods_prof = modulesBySem_prof.get(position);
        for (int i = 0; i < mods_prof.size(); i++) {
          String mod = mods_prof.get(i);
          sb.append("(Prof)").append(mod).append(" ").append(moduleTitles.get(mod) == null ? "" : moduleTitles.get(mod));
          sb.append("\n");
        }
        List<String> mods_ta = modulesBySem_ta.get(position);
        for (int i = 0; i < mods_ta.size(); i++) {
          String mod = mods_ta.get(i);
          sb.append("(TA)").append(mod).append(" ").append(moduleTitles.get(mod) == null ? "" :
              moduleTitles.get(mod));
          sb.append("\n");
        }
        List<String> mods = modulesBySem.get(position);
        for (int i = 0; i < mods.size(); i++) {
          String mod = mods.get(i);
          sb.append(mod).append(" ").append(moduleTitles.get(mod) == null ? "" : moduleTitles.get
              (mod));
          if (i < mods.size() - 1) {
            // Not last make new line
            sb.append("\n");
          }
        }
        modulesView.setText(sb.toString());
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
        //Shouldn't happen since the selection list never gets altered
      }
    });
    semesterSpinner.setSelection(0, true); // true to trigger onItemSelected
    if (mAuth.getCurrentUser().getUid().equals(profileUid)) {
      // Viewing own profile
      statusWrapper.setVisibility(View.VISIBLE);
      statusView.setVisibility(View.GONE);
      ConstraintLayout layout;
      layout = getActivity().findViewById(R.id.profile_constraint_layout);
      ConstraintSet constraintSet = new ConstraintSet();
      constraintSet.clone(layout);
      constraintSet.connect(R.id.divider4, ConstraintSet.TOP, R.id.profile_plain_text_status, ConstraintSet
          .BOTTOM, 8);
      constraintSet.applyTo(layout);
      editStatusView.setText((String) profileData.get("status"));
      updateStatusButton.setVisibility(View.VISIBLE);
    } else {
      statusView.setText((String) profileData.get("status"));
    }
    MuggerRole ownRole = MuggerUser.getInstance().getRole();
    MuggerRole profileRole = MuggerRole.getByRoleId((Long) profileData.get("roleId"));
    if (MuggerRole.MODERATOR.check(MuggerUser.getInstance().getRole())) {
      adminLabelView.setVisibility(View.VISIBLE);
      muteButton.setVisibility(View.VISIBLE);
      muteButton.setOnClickListener((View v) -> {
        new MaterialDialog.Builder(this.getContext())
            .title("How many hours should " + displayName + " be muted for? (0 to unmute)")
            .input("Whole numbers only", "", new MaterialDialog.InputCallback
                () {
              @Override
              public void onInput(MaterialDialog dialog, CharSequence input) {
                if (!MuggerRole.MODERATOR.check(MuggerUser.getInstance().getRole())) {
                  muteButton.setVisibility(View.GONE);
                  return;
                }
                AlertDialog dialogg = new SpotsDialog
                    .Builder()
                    .setContext(ProfileFragment.this.getContext())
                    .setMessage("Muting...")
                    .setCancelable(false)
                    .setTheme(R.style.SpotsDialog)
                    .build();
                dialogg.show();
                String hoursString = input.toString();
                int hours = 0;
                try {
                  hours = Integer.parseInt(hoursString);
                } catch (NumberFormatException nfe) {
                  dialogg.dismiss();
                  Snacky.builder().setActivity(ProfileFragment.this.getActivity()).setText
                      ("Please input a valid whole number.").error().show();
                  return;
                }
                if (hours >= 0) {
                  List<Task<?>> tasks = new ArrayList<>();
                  long until = hours * 3600000 + System.currentTimeMillis();
                  tasks.add(profile.getReference().update("muted", until));
                  Map<String, Object> notificationData = new HashMap<>();
                  if (profileData.get("instanceId") != null) {
                    int duration = Integer.parseInt(hoursString);
                    notificationData.put("instanceId", profileData.get("instanceId"));
                    notificationData.put("duration", Integer.toString(duration));
                    notificationData.put("fromUid", "");
                    notificationData.put("topicUid", "");
                    notificationData.put("type", hours == 0 ? "unmute" : "mute");
                    notificationData.put("until", Long.toString(until));
                    tasks.add(db.collection("notifications").add(notificationData));
                  }
                  Tasks.whenAll(tasks).addOnCompleteListener(task -> {
                    dialogg.dismiss();
                    if (!task.isSuccessful()) {
                      Snacky.builder()
                          .setActivity(ProfileFragment.this.getActivity())
                          .setText("Failed to mute, please try again.")
                          .error()
                          .show();
                    } else {
                      Snacky.builder()
                          .setActivity(ProfileFragment.this.getActivity())
                          .setText("Successfully muted! Thanks for making Mugger a better place.")
                          .success()
                          .show();
                    }
                  });

                } else if (hours < 0) {
                  Snacky.builder().setActivity(ProfileFragment.this.getActivity()).setText
                      ("Please input a valid positive whole number").error().show();
                  return;
                }
              }
            }).show();
      });
      //  banButton.setVisibility(View.VISIBLE);
    }
    if (MuggerRole.ADMIN.check(ownRole)) {
      makeTAProfButton.setVisibility(View.VISIBLE);
      if (true/*!profileUid.equals(mAuth.getUid()) && ownRole.checkSuperiorityTo(profileRole)*/) {
        changeRoleButton.setVisibility(View.VISIBLE);
        changeRoleButton.setOnClickListener(view -> {
          if (!MuggerRole.ADMIN.check(MuggerUser.getInstance().getRole())) {
            view.setVisibility(View.GONE);
            return;
          }
          Intent intent = new Intent(this.getActivity(), ChangeMuggerRoleActivity.class);
          Bundle b = new Bundle();
          b.putString("uid", profileUid);
          b.putInt("currentRole", profileRole.getRoleId());
          intent.putExtras(b);
          getActivity().startActivity(intent);
          getActivity().finish();
        });
      }
    }


  }

  // TODO: Rename method, update argument and hook method into UI event
  public void onButtonPressed(Uri uri) {
    if (mListener != null) {
      mListener.onProfileFragmentInteraction(uri);
    }
  }

  @OnClick(R.id.profile_button_update_status)
  void onClick_updateStatus() {
    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context
        .INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(this.getView().getWindowToken(), 0);
    db.collection("users").document(profileUid).update("status", editStatusView.getText()
        .toString())
        .addOnCompleteListener(task -> {
          if (!task.isSuccessful()) {
            Snackbar.make(this.getView(), "Error updating status.", Snackbar.LENGTH_SHORT).show();
          } else {
            Snackbar.make(this.getView(), "Your status has been updated successfully", Snackbar
                .LENGTH_SHORT).show();
          }
        });
  }


  @OnClick(R.id.profile_button_make_ta_prof)
  void onClick_makeTAProf() {
    if (!MuggerRole.ADMIN.check(MuggerUser.getInstance().getRole())) {
      makeTAProfButton.setVisibility(View.GONE);
      return;
    }
    Intent intent = new Intent(this.getActivity(), MakeTAProfActivity.class);
    Bundle b = new Bundle();
    b.putString("name", nameView.getText().toString());
    b.putString("userUid", profileUid);
    intent.putExtras(b);
    getActivity().startActivity(intent);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnProfileFragmentInteractionListener) {
      mListener = (OnProfileFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString()
          + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  public interface OnProfileFragmentInteractionListener {
    // TODO: Update argument type and name
    void onProfileFragmentInteraction(Uri uri);
  }
}
