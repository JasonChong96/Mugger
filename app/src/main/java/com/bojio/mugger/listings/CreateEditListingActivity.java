package com.bojio.mugger.listings;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bojio.mugger.R;
import com.bojio.mugger.authentication.MuggerUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateEditListingActivity extends AppCompatActivity {

  @BindView(R.id.module_code)
  Spinner moduleCode;

  @BindView(R.id.venue)
  EditText venue;

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

  private Calendar startDateTime;
  private Calendar endDateTime;
  private java.text.DateFormat df;
  private java.text.DateFormat dfTime;
  private FirebaseFirestore db;
  private FirebaseAuth mAuth;
  private Bundle b;
  private Listing toEdit;
  private List<String> moduleCodes;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (MuggerUser.getInstance().getData().get("useAllPastModules") == null) {
      moduleCodes = (List<String>) MuggerUser
          .getInstance().getData().get("moduleCodes");
    } else {
      moduleCodes = (List<String>) MuggerUser
          .getInstance().getData().get("moduleCodes");
    }
    setContentView(R.layout.activity_make_listing);
    db = FirebaseFirestore.getInstance();
    mAuth = FirebaseAuth.getInstance();
    ButterKnife.bind(this);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    df = DateFormat.getDateFormat(this);
    dfTime = DateFormat.getTimeFormat(this);
    b = this.getIntent().getExtras();
    startDateTime = Calendar.getInstance();
    endDateTime = Calendar.getInstance();
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
        moduleCodes);
    moduleCode.setAdapter(adapter);
    if (b != null) {
      setTitle("Edit Listing");
      submitButton.setText("Submit Changes");
      toEdit = b.getParcelable("listing");
      if (toEdit == null) {
        showShortToast("Unable to fetch listing, please try again later.");
        finish();
        return;
      }
      startDateTime.setTimeInMillis(toEdit.getStartTime());
      endDateTime.setTimeInMillis(toEdit.getEndTime());
      moduleCode.setSelection(Math.max(0, moduleCodes.indexOf(toEdit.getModuleCode())));
      venue.setText(toEdit.getVenue());
      description.setText(toEdit.getDescription());
    } else {
      setTitle("Add Listing");
      endDateTime.add(Calendar.HOUR_OF_DAY, 1);
    }

    //Define the AutoComplete Threshold
    updateStartDateTimeDisplay();
    updateEndDateTimeDisplay();
    TimePickerDialog.OnTimeSetListener startTimeListener = new TimePickerDialog.OnTimeSetListener() {
      @Override
      public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        startDateTime.set(Calendar.HOUR_OF_DAY, hour);
        startDateTime.set(Calendar.MINUTE, minute);
        updateStartDateTimeDisplay();
      }
    };
    DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener() {
      @Override
      public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar c = startDateTime;
        startDateTime.set(year, month, day);
        updateStartDateTimeDisplay();
        TimePickerDialog tpg = new TimePickerDialog(CreateEditListingActivity.this, startTimeListener,
            c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), DateFormat.is24HourFormat(CreateEditListingActivity.this));
        tpg.show();

      }
    };
    startDateTimeInput.setOnClickListener(view -> {
      Calendar c = startDateTime;
      DatePickerDialog dpg = new DatePickerDialog(CreateEditListingActivity.this, startDateListener,
          c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
      dpg.show();
    });
    TimePickerDialog.OnTimeSetListener endTimeListener = new TimePickerDialog.OnTimeSetListener() {
      @Override
      public void onTimeSet(TimePicker timePicker, int hour, int minute) {
        endDateTime.set(Calendar.HOUR_OF_DAY, hour);
        endDateTime.set(Calendar.MINUTE, minute);
        updateEndDateTimeDisplay();
      }
    };
    DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener() {
      @Override
      public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar c = endDateTime;
        endDateTime.set(year, month, day);
        updateEndDateTimeDisplay();
        TimePickerDialog tpg = new TimePickerDialog(CreateEditListingActivity.this, endTimeListener,
            c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), DateFormat.is24HourFormat(CreateEditListingActivity.this));
        tpg.show();
      }
    };
    endDateTimeInput.setOnClickListener(view -> {
      Calendar c = endDateTime;
      DatePickerDialog dpg = new DatePickerDialog(CreateEditListingActivity.this, endDateListener,
          c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
      dpg.show();
    });
    submitButton.setOnClickListener(this::publishListing);
  }

  private void updateDateTimeDisplay(TextView view, Calendar cal) {
    view.setText(new StringBuilder()
        .append(df.format(cal.getTime()))
        .append(" ")
        .append(dfTime.format(cal.getTime()))
        .toString());
  }

  private void updateStartDateTimeDisplay() {
    updateDateTimeDisplay(startDateTimeInput, startDateTime);
  }

  private void updateEndDateTimeDisplay() {
    updateDateTimeDisplay(endDateTimeInput, endDateTime);
  }

  private void publishListing(View view) {
    //Check if all fields are filled
    submitButton.setClickable(false);
    if (moduleCode.getSelectedItem().toString() == null || moduleCode.getSelectedItem().toString()
        .isEmpty()) {
      showShortToast("Please select the module the study session is for.");
      submitButton.setClickable(true);
      return;
    }
    if (startDateTime.after(endDateTime)) {
      showShortToast("Start time cannot be later than the end time.");
      submitButton.setClickable(true);
      return;
    }
    if (endDateTime.getTimeInMillis() < System.currentTimeMillis()) {
      showShortToast("The end time cannot be earlier than the current time.");
      submitButton.setClickable(true);
      return;
    }
    if (venue.getText().toString().isEmpty()) {
      showShortToast("Please fill in the venue which your study session is/will be held at.");
      submitButton.setClickable(true);
      return;
    }
    if (description.getText().toString().isEmpty()) {
      showShortToast("Please fill in a short description about your study session.");
      submitButton.setClickable(true);
      return;
    }
    progressBar.setVisibility(View.VISIBLE);
    Map<String, Object> data = new HashMap<>();
    long startTimeMillis = startDateTime.getTimeInMillis();
    data.put("description", description.getText().toString());
    data.put("endTime", endDateTime.getTimeInMillis());
    data.put("startTime", startTimeMillis);
    data.put("moduleCode", moduleCode.getSelectedItem().toString());
    data.put("ownerId", mAuth.getCurrentUser().getUid());
    data.put("venue", venue.getText().toString());
    data.put(mAuth.getUid(), startTimeMillis);
    if (toEdit != null) {
      for (String attendee : toEdit.getAttendees()) {
        data.put(attendee, startTimeMillis);
      }
    }
    Task<?> addedDocRef;
    if (b == null) {
      addedDocRef = db.collection("listings").add(data);
    } else {
      addedDocRef = db.collection("listings").document(toEdit.getUid()).set(data, SetOptions.merge());
    }
    OnCompleteListener listener = new OnCompleteListener<DocumentReference>() {
      @Override
      public void onComplete(@NonNull Task<DocumentReference> task) {
        if (!task.isSuccessful()) {
          CreateEditListingActivity.this.showShortToast("Failed to publish listing, please try again later.");
          submitButton.setClickable(true);
          progressBar.setVisibility(View.GONE);
          return;
        } else {
         /* if (b == null) {
            DocumentReference docRef = task.getResult();
            Map<String, Object> uidToListing = new HashMap<>();
            uidToListing.put(docRef.getId(), "");
            db.collection("joinedListings").document(mAuth.getCurrentUser().getUid()).set
            (uidToListing);

          }*/
         if (task.getResult() != null) {
           FirebaseMessaging.getInstance().subscribeToTopic(task.getResult().getId());
         }
          CreateEditListingActivity.this.finish();
        }
      }
    };
    addedDocRef.addOnCompleteListener(listener);

  }

  private void showShortToast(String msg) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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
