package com.bojio.mugger.profile;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
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
import com.bojio.mugger.authentication.MuggerRole;
import com.bojio.mugger.authentication.MuggerUserCache;

import java.util.ArrayList;
import java.util.List;

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
  private List<String> semesters;
  private OnProfileFragmentInteractionListener mListener;
  private ProfileViewModel mViewModel;

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
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    AlertDialog dialog = new SpotsDialog
        .Builder()
        .setContext(this.getActivity())
        .setTheme(R.style.SpotsDialog)
        .setMessage("Loading profile...")
        .setCancelable(false)
        .build();
    dialog.show();
    mViewModel = ViewModelProviders.of(this).get(ProfileViewModel.class);
    mViewModel.init(profileUid);
    mViewModel.getDisplayName().observe(this, nameView::setText);
    mViewModel.getEmail().observe(this, emailView::setText);
    mViewModel.getFaculty().observe(this, facultyView::setText);
    mViewModel.getFirstMajor().observe(this, firstMajorView::setText);
    mViewModel.getStatus().observe(this, newStatus -> {
      statusView.setText(newStatus);
      editStatusView.setText(newStatus);
    });
    mViewModel.getSecondMajor().observe(this, newSecondMajor -> {
      if (newSecondMajor == null && secondMajorView.getVisibility() == View.VISIBLE) {
        secondMajorView.setVisibility(View.GONE);
      } else if (newSecondMajor != null && secondMajorView.getVisibility() != View.VISIBLE) {
        secondMajorView.setText(newSecondMajor);
        secondMajorView.setText(newSecondMajor);
        secondMajorView.setVisibility(View.VISIBLE);
      }
    });
    mViewModel.getRole().observe(this, newRole -> {
      if (mViewModel.adminControlsVisible()) {
        makeTAProfButton.setVisibility(View.VISIBLE);
        changeRoleButton.setVisibility(View.VISIBLE);
        changeRoleButton.setOnClickListener(view -> {
          if (!mViewModel.adminControlsVisible()) {
            view.setVisibility(View.GONE);
            return;
          }
          Intent intent = new Intent(this.getActivity(), ChangeMuggerRoleActivity.class);
          Bundle b = new Bundle();
          b.putString("uid", profileUid);
          b.putInt("currentRole", mViewModel.getRole().getValue().getRoleId());
          intent.putExtras(b);
          getActivity().startActivity(intent);
        });
      }
      if (mViewModel.moderatorControlsVisible()) {
        adminLabelView.setVisibility(View.VISIBLE);
        muteButton.setVisibility(View.VISIBLE);
        muteButton.setOnClickListener((View v) -> {
          new MaterialDialog.Builder(this.getContext())
              .title("How many hours should " + nameView.getText() + " be muted for? (0 to unmute)")
              .input("Whole numbers only", "", (dialog1, input) -> {
                if (!mViewModel.moderatorControlsVisible()) {
                  v.setVisibility(View.GONE);
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
                int hours;
                try {
                  hours = Integer.parseInt(hoursString);
                } catch (NumberFormatException nfe) {
                  dialogg.dismiss();
                  Snacky.builder().setActivity(ProfileFragment.this.getActivity()).setText
                      ("Please input a valid whole number.").error().show();
                  return;
                }
                if (hours >= 0) {
                  mViewModel.muteUser(hours).addOnCompleteListener(task -> {
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

                } else {
                  Snacky.builder().setActivity(ProfileFragment.this.getActivity()).setText
                      ("Please input a valid positive whole number").error().show();
                }
              }).show();
        });
      }
      if (dialog.isShowing() && mViewModel.isProfileLoaded()) {
        dialog.dismiss();
      }
    });
    mViewModel.getModulesBySem().observe(this, modulesBySem -> {
      semesters = new ArrayList<>(modulesBySem.keySet());
      ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getActivity(), android.R.layout
          .simple_dropdown_item_1line,
          semesters);
      semesterSpinner.setAdapter(adapter);
      semesterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
          mViewModel.setSelectedSemester(semesters.get(position));
          modulesView.setText(mViewModel.getSemesterModulesDisplay(semesters.get(position)));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
          //Shouldn't happen since the selection list never gets altered
        }
      });
      if (mViewModel.getSelectedSemester() != null) {
        semesterSpinner.setSelection(Math.max(0, semesters.indexOf(mViewModel.getSelectedSemester()))
            , true);
      }
      if (dialog.isShowing() && mViewModel.isProfileLoaded()) {
        dialog.dismiss();
      }
    });
    mViewModel.getModuleTitles().observe(this, titles -> {
      if (semesterSpinner.getSelectedItem() != null) {
        modulesView.setText(mViewModel.getSemesterModulesDisplay((String) semesterSpinner
            .getSelectedItem()));
      }
      if (dialog.isShowing() && mViewModel.isProfileLoaded()) {
        dialog.dismiss();
      }
    });
    if (mViewModel.isOwnProfile()) {
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
      updateStatusButton.setVisibility(View.VISIBLE);
    }
    super.onActivityCreated(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    super.onCreate(savedInstanceState);
    View view = inflater.inflate(R.layout.fragment_profile, container, false);
    ButterKnife.bind(this, view);

    return view;
  }

  @OnClick(R.id.profile_button_update_status)
  void onClick_updateStatus() {
    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context
        .INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(this.getView().getWindowToken(), 0);
    mViewModel.updateStatus(editStatusView.getText().toString())
        .addOnCompleteListener(task -> {
          if (!task.isSuccessful()) {
            Snacky.builder()
                .setActivity(requireActivity())
                .setText("Error updating status.")
                .error()
                .show();
          } else {
            Snacky.builder()
                .setActivity(requireActivity())
                .setText("Your status has been updated successfully")
                .success()
                .show();
          }
        });
  }


  @OnClick(R.id.profile_button_make_ta_prof)
  void onClick_makeTAProf() {
    if (!MuggerRole.ADMIN.check(MuggerUserCache.getInstance().getRole())) {
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
