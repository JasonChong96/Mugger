package com.bojio.mugger.listings.fragments;

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
import java.util.HashMap;
import java.util.Map;

public class CustomFilterListingsFragments extends ListingsFragments {
  ChipGroup chipGroupModules;
  ChipGroup chipGroupRoles;
  Chip chipProfessor;
  Chip chipStudent;
  Chip chipTA;
  TextInputEditText editTextCreator;
  TextInputEditText editTextVenue;
  TextInputEditText editTextDescription;
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
        .onPositive((dialog, which) -> updateFilter())
        .onNegative((dialog, which) -> initDialog())
        .build();
    bindViews(dialog.getCustomView());
    View view = super.onCreateView(inflater, container, savedInstanceState);
    filterSettingsButton.setVisibility(View.VISIBLE);
    filterSettingsButton.setOnClickListener(v -> dialog.show());
    return view;
  }

  private void bindViews(View view) {
    chipGroupModules = view.findViewById(R.id.custom_filter_chip_group_modules);
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
  }

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
  }

  private void updateFilter() {
    int flag = 0;
    Map<String, Object> data = new HashMap<>();
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
    mViewModelCustom.updateFilter(flag, moduleFilter, creatorFilter, venueFilter, descFilter);
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
        updateFilter();
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
