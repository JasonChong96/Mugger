package com.bojio.mugger.listings;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bojio.mugger.R;
import com.bojio.mugger.authentication.LoggedInActivity;
import com.bojio.mugger.lifecycle.LifecycleUtils;
import com.bojio.mugger.listings.viewmodels.CreateEditListingViewModel;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;

public class CreateEditListingActivity extends LoggedInActivity {

  @BindView(R.id.module_code)
  Spinner moduleCode;

  @BindView(R.id.venue)
  TextInputEditText venue;

  @BindView(R.id.start_date_time)
  TextView startDateTimeInput;

  @BindView(R.id.end_date_time)
  TextView endDateTimeInput;

  @BindView(R.id.submit_button)
  Button submitButton;

  @BindView(R.id.description)
  EditText description;

  @BindView(R.id.progressBar2)
  ProgressBar progressBar;

  @BindView(R.id.publish_listing_start_date_time)
  TextInputLayout startDateTimeWrapper;

  private Bundle b;
  private Listing toEdit;
  private List<String> moduleCodes;
  private CreateEditListingViewModel mViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (stopActivity) {
      finish();
      return;
    }
    mViewModel = ViewModelProviders.of(this, LifecycleUtils.getAndroidViewModelFactory
        (getApplication())).get(CreateEditListingViewModel.class);
    if (mViewModel.getMutedTimeLeft() > 0) {
      Toasty.error(this, String.format(Locale.ENGLISH,
          "You cannot do this while muted. Time left: %.2f hours",
          mViewModel.getMutedTimeLeft())).show();
      finish();
      return;
    }
    b = this.getIntent().getExtras();
    setContentView(R.layout.activity_make_listing);
    ButterKnife.bind(this);
    startDateTimeWrapper.setHelperText("Today's date: " + mViewModel.getDateString(System.currentTimeMillis()));
    if (b != null) {
      toEdit = b.getParcelable("listing");
      if (toEdit == null) {
        showShortToast("Unable to fetch listing, please try again later.");
        finish();
        return;
      }
      mViewModel.init(toEdit);
    } else {
      mViewModel.init(null);
    }
    moduleCodes = mViewModel.getModuleSelections();
    if (b != null) {
      setTitle("Edit Listing");
      submitButton.setText("Submit Changes");
      moduleCode.setSelection(Math.max(0, moduleCodes.indexOf(mViewModel.getModuleCode())));
      moduleCode.setEnabled(false);
      venue.setText(mViewModel.getOriginalVenue());
      description.setText(mViewModel.getOriginalDescription());
    } else {
      setTitle("Add Listing");
    }
    mViewModel.getStartTimeDateString().observe(this, startDateTimeInput::setText);
    mViewModel.getEndDateTimeString().observe(this, endDateTimeInput::setText);
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
        moduleCodes);
    moduleCode.setAdapter(adapter);
    if (mViewModel.getModuleCode() != null) {
      moduleCode.setSelection(Math.max(0, moduleCodes.indexOf(mViewModel.getModuleCode())), true);
    } else {
      moduleCode.setSelection(0, true);
    }
    moduleCode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        mViewModel.setModuleCode(moduleCodes.get(i));
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {

      }
    });
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    initStartDateTimePicker();
    initEndDateTimePicker();
    submitButton.setOnClickListener(this::publishListing);
  }

  private void initEndDateTimePicker() {
    TimePickerDialog.OnTimeSetListener endTimeListener = (timePicker, hour, minute) -> mViewModel.updateEndTime(hour, minute);
    DatePickerDialog.OnDateSetListener endDateListener = (datePicker, year, month, day) -> {
      Calendar c = mViewModel.getEndDateTime();
      mViewModel.updateEndDate(year, month, day);
      TimePickerDialog tpg = new TimePickerDialog(CreateEditListingActivity.this, endTimeListener,
          c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), DateFormat.is24HourFormat(CreateEditListingActivity.this));
      tpg.show();
    };
    endDateTimeInput.setOnClickListener(view -> {
      Calendar c = mViewModel.getEndDateTime();
      DatePickerDialog dpg = new DatePickerDialog(CreateEditListingActivity.this, endDateListener,
          c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
      dpg.show();
    });
  }

  private void initStartDateTimePicker() {
    TimePickerDialog.OnTimeSetListener startTimeListener = (timePicker, hour, minute) -> mViewModel.updateStartTime(hour, minute);
    DatePickerDialog.OnDateSetListener startDateListener = (datePicker, year, month, day) -> {
      Calendar c = mViewModel.getStartDateTime();
      mViewModel.updateStartDate(year, month, day);
      TimePickerDialog tpg = new TimePickerDialog(CreateEditListingActivity.this, startTimeListener,
          c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), mViewModel.is24HourFormat());
      tpg.show();

    };
    startDateTimeInput.setOnClickListener(view -> {
      Calendar c = mViewModel.getStartDateTime();
      DatePickerDialog dpg = new DatePickerDialog(CreateEditListingActivity.this, startDateListener,
          c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
      dpg.show();
    });
  }

  private void publishListing(View view) {
    //Check if all fields are filled
    submitButton.setClickable(false);
    if (moduleCode.getSelectedItem().toString() == null || moduleCode.getSelectedItem().toString()
        .trim().isEmpty()) {
      showShortToast("Please select the module the study session is for.");
      submitButton.setClickable(true);
      return;
    }
    if (mViewModel.startAfterEnd()) {
      showShortToast("Start time cannot be later than the end time.");
      submitButton.setClickable(true);
      return;
    }
    if (mViewModel.endBeforeNow()) {
      showShortToast("The end time cannot be earlier than the current time.");
      submitButton.setClickable(true);
      return;
    }
    String venueString = venue.getText().toString().trim();
    String descriptionString = description.getText().toString().trim();
    if (venueString.isEmpty()) {
      showShortToast("Please fill in the venue which your study session is/will be held at.");
      submitButton.setClickable(true);
      return;
    }
    if (descriptionString.isEmpty()) {
      showShortToast("Please fill in a short description about your study session.");
      submitButton.setClickable(true);
      return;
    }
    AlertDialog dialog = new SpotsDialog
        .Builder()
        .setContext(this)
        .setMessage(b == null ? "Publishing listing..." : "Submitting changes...")
        .setCancelable(false)
        .setTheme(R.style.SpotsDialog)
        .build();
    dialog.show();
    mViewModel.publish(venueString, descriptionString).addOnCompleteListener(task -> {
      dialog.dismiss();
      if (!task.isSuccessful()) {
        CreateEditListingActivity.this.showShortToast("Failed to publish listing, please try again later.");
        submitButton.setClickable(true);
      } else {
        Toasty.success(CreateEditListingActivity.this, b == null ? "Listing successfully " +
            "created!" : "Listing " +
            "successfully edited!").show();
        finish();
      }
    });
  }

  private void showShortToast(String msg) {
    Toasty.error(this, msg, Toast.LENGTH_SHORT).show();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      // When back button on the top left is clicked
      case android.R.id.home:
        finish();
        return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
