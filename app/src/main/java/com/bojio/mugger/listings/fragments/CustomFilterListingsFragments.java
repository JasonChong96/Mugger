package com.bojio.mugger.listings.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.annimon.stream.function.Predicate;
import com.bojio.mugger.R;
import com.bojio.mugger.authentication.MuggerUserCache;
import com.bojio.mugger.constants.ModuleRole;
import com.bojio.mugger.database.MuggerDatabase;
import com.bojio.mugger.listings.ListingUtils;
import com.google.firebase.firestore.FieldValue;

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
  private MuggerUserCache cache;
  private ArrayList<String> modules;

  private static <T> Predicate<T> composePredicates(Predicate<T> predicate1, Predicate<T>
      predicate2) {
    return x -> predicate1.test(x) && predicate2.test(x);
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    delayInitListings = true;
    cache = MuggerUserCache.getInstance();
    dialog = new MaterialDialog.Builder(this.getActivity())
        .title("Filter Settings")
        .customView(R.layout.dialog_custom_filter, true)
        .positiveText("Apply Filters")
        .negativeText("Discard Changes")
        .onPositive((dialog, which) -> updateFilter())
        .onNegative((dialog, which) -> initDialog())
        .build();
    bindViews(dialog.getCustomView());
    initDialog();
    View view = super.onCreateView(inflater, container, savedInstanceState);
    updateFilter();
    filterSettingsButton.setVisibility(View.VISIBLE);
    filterSettingsButton.setOnClickListener(v -> {
      dialog.show();
    });
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
    modules = ListingUtils.getFilterModules(cache);
    spinnerModules.setAdapter(new ArrayAdapter<>(this.getActivity(), android.R.layout
        .simple_dropdown_item_1line, modules));
    if (cache.getData().containsKey("customFilterSettings")) {
      int flags = ((Long) cache.getData().get("customFilterSettings")).intValue();
      int category = flags & 0x3;
      switch (category) {
        case 0:
          radioButtonAllListings.setChecked(true);
          break;
        case 1:
          radioButtonJoiningListings.setChecked(true);
          break;
        case 2:
          radioButtonMyListings.setChecked(true);
          break;
        default:
          throw new UnsupportedOperationException("Unknown category flag : " + Integer.toString
              (category));
      }
      boolean student = (flags & 0x4) != 0;
      boolean ta = (flags & 0x8) != 0;
      boolean prof = (flags & 0x10) != 0;
      chipStudent.setChecked(student);
      chipTA.setChecked(ta);
      chipProfessor.setChecked(prof);
    }
    if (cache.getData().containsKey("customFilterModule")) {
      String mod = (String) cache.getData().get("customFilterModule");
      int pos = modules.indexOf(mod);
      if (pos > 0) {
        spinnerModules.setSelection(pos);
      }
    }
    if (cache.getData().containsKey("customFilterCreator")) {
      String filter = (String) cache.getData().get("customFilterCreator");
      editTextCreator.setText(filter);
    }
    if (cache.getData().containsKey("customFilterVenue")) {
      String filter = (String) cache.getData().get("customFilterVenue");
      editTextVenue.setText(filter);
    }
    if (cache.getData().containsKey("customFilterDesc")) {
      String filter = (String) cache.getData().get("customFilterDesc");
      editTextDescription.setText(filter);
    }
  }

  private void updateFilter() {
    int flag = 0;
    Map<String, Object> data = new HashMap<>();
    predicateFilter = x -> true;
    switch (radioGroupCategories.getCheckedRadioButtonId()) {
      case R.id.custom_filter_radio_button_all_listings:
        mQuery = ListingUtils.getAvailableListingsQuery(db);
        flag |= 0;
        break;
      case R.id.custom_filter_radio_button_joining_listings:
        mQuery = ListingUtils.getAttendingListingsQuery(db, mAuth.getUid());
        flag |= 1;
        break;
      case R.id.custom_filter_radio_button_my_listings:
        mQuery = ListingUtils.getMyListingsQuery(db, mAuth.getUid());
        flag |= 2;
        break;
      default:
        throw new IllegalStateException("No valid category selected");
    }
    if (spinnerModules.getSelectedItemPosition() != 0) {
      String mod = modules.get(spinnerModules.getSelectedItemPosition());
      predicateFilter = composePredicates(predicateFilter, listing -> listing.getModuleCode()
          .equals(mod));
      data.put("customFilterModule", mod);
    } else {
      data.put("customFilterModule", FieldValue.delete());
    }
    if (!chipStudent.isChecked()) {
      predicateFilter = composePredicates(predicateFilter, listing -> listing.getType() !=
          ModuleRole.EMPTY);
      flag ^= 4;
    } else {
      flag |= 4;
    }
    if (!chipTA.isChecked()) {
      predicateFilter = composePredicates(predicateFilter, listing -> listing.getType() !=
          ModuleRole.TEACHING_ASSISTANT);
      flag ^= 8;
    } else {
      flag |= 8;
    }
    if (!chipProfessor.isChecked()) {
      predicateFilter = composePredicates(predicateFilter, listing -> listing.getType() !=
          ModuleRole.PROFESSOR);
      flag ^= 0x10;
    } else {
      flag |= 0x10;
    }
    String creatorFilter = editTextCreator.getText().toString();
    if (!creatorFilter.isEmpty()) {
      predicateFilter = composePredicates(predicateFilter, listing -> listing.getOwnerName()
          .contains(creatorFilter));
      data.put("customFilterCreator", creatorFilter);
    } else {
      data.put("customFilterCreator", FieldValue.delete());
    }
    String venueFilter = editTextVenue.getText().toString();
    if (!venueFilter.isEmpty()) {
      predicateFilter = composePredicates(predicateFilter, listing -> listing.getVenue()
          .contains(venueFilter));
      data.put("customFilterVenue", venueFilter);
    } else {
      data.put("customFilterVenue", FieldValue.delete());
    }
    String descFilter = editTextDescription.getText().toString();
    if (!descFilter.isEmpty()) {
      predicateFilter = composePredicates(predicateFilter, listing -> listing.getDescription()
          .contains(descFilter));
      data.put("customFilterDesc", descFilter);
    } else {
      data.put("customFilterDesc", FieldValue.delete());
    }
    data.put("customFilterSettings", Long.valueOf(flag));
    MuggerDatabase.getUserReference(db, mAuth.getUid()).update(data);
    cache.updateCache(data);
    initListings();
  }
}
