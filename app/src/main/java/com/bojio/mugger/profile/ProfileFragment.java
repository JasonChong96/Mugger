package com.bojio.mugger.profile;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bojio.mugger.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnProfileFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
  private String profileUid;
  private FirebaseFirestore db;
  private FirebaseAuth mAuth;
  private List<List<String>> modulesBySem;
  private List<String> semesters;
  private Map<String, Object> moduleTitles;

  @BindView(R.id.profile_text_view_name)
  TextView nameView;

  @BindView(R.id.profile_text_view_email)
  TextView emailView;

  @BindView(R.id.profile_text_view_sex)
  TextView sexView;

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

  @BindView(R.id.profile_button_report)
  Button reportButton;

  @BindView(R.id.profile_button_view_reports)
  Button viewReportsButton;

  @BindView(R.id.profile_button_ban)
  Button ban;

  @BindView(R.id.progressBar7)
  ProgressBar progressBar;

  @BindView(R.id.divider4)
  View divider4;

  @BindView(R.id.profile_button_update_status)
  Button updateStatusButton;


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

    Tasks.whenAll(profileTask, modulesTask, moduleTitlesTask).addOnCompleteListener
        (task -> {
      ProfileFragment.this.moduleTitles = moduleTitlesTask.getResult().getData();
      ProfileFragment.this.loadProfile(profileTask.getResult(), modulesTask.getResult().getDocuments
          ());
    });
    return view;
  }

  private void loadProfile(DocumentSnapshot profile, List<DocumentSnapshot> moduless) {
    Map<String, Object> profileData = profile.getData();
    nameView.setText((String) profileData.get("displayName"));

    emailView.setText((String) profileData.get("email"));
    sexView.setText((String) profileData.get("sex"));
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
    for (DocumentSnapshot snapshot : moduless) {
      semesters.add(snapshot.getId().replace(".", "/"));
      modulesBySem.add((List<String>) snapshot.get("moduleCodes"));
    }
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout
        .simple_dropdown_item_1line,
        semesters);
    semesterSpinner.setAdapter(adapter);
    semesterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        StringBuilder sb = new StringBuilder();
        List<String> mods = modulesBySem.get(position);
        for (int i = 0; i < mods.size(); i++) {
          String mod = mods.get(i);
          sb.append(mod).append(" ").append(moduleTitles.get(mod));
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
      editStatusView.setVisibility(View.VISIBLE);
      statusView.setVisibility(View.GONE);
      ConstraintLayout layout;
      layout = (ConstraintLayout) getActivity().findViewById(R.id.profile_constraint_layout);
      ConstraintSet constraintSet = new ConstraintSet();
      constraintSet.clone(layout);
      constraintSet.connect(R.id.divider4,ConstraintSet.TOP,R.id.profile_plain_text_status,ConstraintSet
          .BOTTOM,8);
      constraintSet.applyTo(layout);
      editStatusView.setText((String) profileData.get("status"));
    } else {
      statusView.setText((String) profileData.get("status"));
    }
    progressBar.setVisibility(View.GONE);
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
