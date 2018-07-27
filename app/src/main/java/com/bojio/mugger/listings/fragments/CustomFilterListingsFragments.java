package com.bojio.mugger.listings.fragments;

import android.app.DatePickerDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bojio.mugger.R;
import com.bojio.mugger.lifecycle.LifecycleUtils;
import com.bojio.mugger.listings.viewmodels.CustomFilterListingsViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class CustomFilterListingsFragments extends ListingsFragments {
  ChipGroup chipGroupRoles;
  Chip chipProfessor;
  Chip chipStudent;
  Chip chipTA;
  TextInputEditText editTextCreator;
  TextInputEditText editTextVenue;
  TextInputEditText editTextDescription;
  TextInputEditText editTextFromDate;
  TextInputEditText editTextToDate;
  RadioGroup radioGroupCategories;
  RadioButton radioButtonAllListings;
  RadioButton radioButtonJoiningListings;
  RadioButton radioButtonMyListings;
  Spinner spinnerModules;
  private MaterialDialog dialog;
  private ArrayList<String> modules;
  private String selected;
  private boolean dialogInitialized;
  private CustomFilterListingsViewModel mViewModelCustom;

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    delayInitListings = true;
    dialogInitialized = false;
    dialog = new MaterialDialog.Builder(this.getActivity())
        .title("Filter Settings")
        .customView(R.layout.dialog_custom_filter, true)
        .positiveText("Apply Filters")
        .negativeText("Discard Changes")
        .onPositive((dialog, which) -> updateFilter(true))
        .onNegative((dialog, which) -> initDialog())
        .build();
    bindViews(dialog.getCustomView());
    View view = super.onCreateView(inflater, container, savedInstanceState);
    filterSettingsButton.setVisibility(View.VISIBLE);
    filterSettingsButton.setOnClickListener(v -> dialog.show());
    return view;
  }

  /**
   * Bind view references with the actual views in the settings dialog.
   * @param view the View of the settings dialog
   */
  private void bindViews(View view) {
    chipGroupRoles = view.findViewById(R.id.custom_filter_chip_group_roles);
    chipProfessor = view.findViewById(R.id.custom_filter_chip_professor);
    chipStudent = view.findViewById(R.id.custom_filter_chip_student);
    chipTA = view.findViewById(R.id.custom_filter_chip_ta);
    editTextCreator = view.findViewById(R.id.custom_filter_edit_text_creator);
    editTextVenue = view.findViewById(R.id.custom_filter_edit_text_venue);
    editTextDescription = view.findViewById(R.id.custom_filter_edit_text_description);
    radioGroupCategories = view.findViewById(R.id.custom_filter_radio_group_categories);
    radioButtonAllListings = view.findViewById(R.id.custom_filter_radio_button_all_listings);
    radioButtonJoiningListings = view.findViewById(R.id
        .custom_filter_radio_button_joining_listings);
    radioButtonMyListings = view.findViewById(R.id.custom_filter_radio_button_my_listings);
    spinnerModules = view.findViewById(R.id.custom_filter_spinner_modules);
    editTextFromDate = view.findViewById(R.id.custom_filter_from_input);
    editTextToDate = view.findViewById(R.id.custom_filter_to_input);
  }

  /**
   * Initializes the custom filter settings dialog with previously saved settings.
   */
  private void initDialog() {
    if (mViewModelCustom.hasPreviouslySetFlags()) {
      int flags = mViewModelCustom.getCustomFilterFlags();
      int category = flags & CustomFilterListingsViewModel.MASK_CATEGORY;
      switch (category) {
        case CustomFilterListingsViewModel.FLAG_ALL_LISTINGS:
          radioButtonAllListings.setChecked(true);
          break;
        case CustomFilterListingsViewModel.FLAG_JOINING_LISTINGS:
          radioButtonJoiningListings.setChecked(true);
          break;
        case CustomFilterListingsViewModel.FLAG_MY_LISTINGS:
          radioButtonMyListings.setChecked(true);
          break;
        default:
          throw new UnsupportedOperationException("Unknown category flag : " + Integer.toString
              (category));
      }
      boolean student = (flags & CustomFilterListingsViewModel.FLAG_STUDENT) != 0;
      boolean ta = (flags & CustomFilterListingsViewModel.FLAG_TEACHING_ASSISTANT) != 0;
      boolean prof = (flags & CustomFilterListingsViewModel.FLAG_PROFESSOR) != 0;
      chipStudent.setChecked(student);
      chipTA.setChecked(ta);
      chipProfessor.setChecked(prof);
    }
    String filterModule = mViewModelCustom.getFilterModule();
    if (filterModule != null) {
      int pos = modules.indexOf(filterModule);
      if (pos > 0) {
        spinnerModules.setSelection(pos);
      }
    }
    String filterCreator = mViewModelCustom.getFilterCreator();
    if (filterCreator != null) {
      editTextCreator.setText(filterCreator);
    }
    String filterVenue = mViewModelCustom.getFilterVenue();
    if (filterVenue != null) {
      editTextVenue.setText(filterVenue);
    }
    String filterDesc = mViewModelCustom.getFilterDesc();
    if (filterDesc != null) {
      editTextDescription.setText(filterDesc);
    }
    editTextFromDate.setText(mViewModelCustom.getStringFilterFromDate());
    editTextToDate.setText(mViewModelCustom.getStringFilterToDate());
    DatePickerDialog.OnDateSetListener fromDateListener = (datePicker, year, month, day) ->
        editTextFromDate.setText(mViewModelCustom.getDateString(year, month, day));
    DatePickerDialog.OnDateSetListener toDateListener = (datePicker, year, month, day) ->
        editTextToDate.setText(mViewModelCustom.getDateString(year, month, day));
    editTextFromDate.setOnClickListener(view -> {
      Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Asia/Singapore"));
      DatePickerDialog dpg = new DatePickerDialog(getActivity(this), fromDateListener,
          c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
      dpg.show();
    });
    editTextToDate.setOnClickListener(view -> {
      Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Asia/Singapore"));
      DatePickerDialog dpg = new DatePickerDialog(getActivity(this), toDateListener,
          c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
      dpg.show();
    });
  }

  /**
   * Update filter based on user's input settings.
   * @param changed true if the filter has been changed from previously saved settings
   */
  private void updateFilter(boolean changed) {
    int flag = 0;
    predicateFilter = x -> true;
    switch (radioGroupCategories.getCheckedRadioButtonId()) {
      case R.id.custom_filter_radio_button_all_listings:
        flag |= CustomFilterListingsViewModel.FLAG_ALL_LISTINGS;
        break;
      case R.id.custom_filter_radio_button_joining_listings:
        flag |= CustomFilterListingsViewModel.FLAG_JOINING_LISTINGS;
        break;
      case R.id.custom_filter_radio_button_my_listings:
        flag |= CustomFilterListingsViewModel.FLAG_MY_LISTINGS;
        break;
      default:
        throw new IllegalStateException("No valid category selected");
    }
    String moduleFilter = "";
    if (spinnerModules.getSelectedItemPosition() != 0) {
      moduleFilter = modules.get(spinnerModules.getSelectedItemPosition());
    }
    if (chipStudent.isChecked()) {
      flag |= CustomFilterListingsViewModel.FLAG_STUDENT;
    }
    if (chipTA.isChecked()) {
      flag |= CustomFilterListingsViewModel.FLAG_TEACHING_ASSISTANT;
    }
    if (chipProfessor.isChecked()) {
      flag |= CustomFilterListingsViewModel.FLAG_PROFESSOR;
    }
    String creatorFilter = editTextCreator.getText().toString();
    String venueFilter = editTextVenue.getText().toString();
    String descFilter = editTextDescription.getText().toString();
    String fromDateFilter = editTextFromDate.getText().toString();
    String toDateFilter = editTextToDate.getText().toString();
    mViewModelCustom.updateFilter(flag, moduleFilter, creatorFilter, venueFilter, descFilter,
        fromDateFilter, toDateFilter, changed);
    initListings();
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    mViewModelCustom = ViewModelProviders.of(this, LifecycleUtils.getAndroidViewModelFactory
        (getActivity().getApplication())).get(CustomFilterListingsViewModel.class);
    mViewModel = mViewModelCustom;
    super.onActivityCreated(savedInstanceState);
    mViewModelCustom.getShowUnrelatedModules().observe(this, show -> {
      modules = mViewModelCustom.getModuleFilters();
      spinnerModules.setAdapter(new ArrayAdapter<>(this.getActivity(), android.R.layout
          .simple_dropdown_item_1line, modules));
      if (!dialogInitialized) {
        initDialog();
        updateFilter(false);
      }
      if (selected == null || modules.indexOf(selected) < 0) {
        selected = modules.get(0);
      }
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout
          .simple_dropdown_item_1line, modules);
      spinner.setAdapter(adapter);
      spinner.setSelection(modules.indexOf(selected));
    });
  }
}
