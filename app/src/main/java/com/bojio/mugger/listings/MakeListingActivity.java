package com.bojio.mugger.listings;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bojio.mugger.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MakeListingActivity extends AppCompatActivity {

    @BindView(R.id.module_code)
    EditText moduleCode;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_listing);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        ButterKnife.bind(this);
        setTitle("Add Listing");
        df = DateFormat.getDateFormat(this);
        dfTime = DateFormat.getTimeFormat(this);
        startDateTime = Calendar.getInstance();
        endDateTime = Calendar.getInstance();
        endDateTime.add(Calendar.HOUR_OF_DAY, 1);
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
                TimePickerDialog tpg = new TimePickerDialog(MakeListingActivity.this, startTimeListener,
                        c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), DateFormat.is24HourFormat(MakeListingActivity.this));
                tpg.show();

            }
        };
        startDateTimeInput.setOnClickListener(view -> {
            Calendar c = startDateTime;
            DatePickerDialog dpg = new DatePickerDialog(MakeListingActivity.this, startDateListener,
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
                TimePickerDialog tpg = new TimePickerDialog(MakeListingActivity.this, endTimeListener,
                        c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), DateFormat.is24HourFormat(MakeListingActivity.this));
                tpg.show();
            }
        };
        endDateTimeInput.setOnClickListener(view -> {
            Calendar c = endDateTime;
            DatePickerDialog dpg = new DatePickerDialog(MakeListingActivity.this, endDateListener,
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
        if (moduleCode.getText().toString().isEmpty()) {
            showShortToast("Please fill in the module(s) the study session is for.");
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
        Map<String, Object> listing = new HashMap<>();
        listing.put("description", description.getText().toString());
        listing.put("endTime", endDateTime.getTimeInMillis());
        listing.put("startTime", startDateTime.getTimeInMillis());
        listing.put("moduleCode", moduleCode.getText().toString());
        listing.put("ownerId", mAuth.getCurrentUser().getUid());
        listing.put("venue", venue.getText().toString());
        Task<DocumentReference> addedDocRef = db.collection("listings").add(listing);
        addedDocRef.addOnCompleteListener(task -> {
           if (!task.isSuccessful()) {
               showShortToast("Failed to publish listing, please try again later.");
               submitButton.setClickable(true);
               progressBar.setVisibility(View.GONE);
               return;
           } else {
               DocumentReference docRef = task.getResult();
               Map<String, Object> uidToListing = new HashMap<>();
               uidToListing.put(docRef.getId(), "");
               db.collection("joinedListings").document(mAuth.getCurrentUser().getUid()).set(uidToListing);
               finish();
           }
        });

    }

    private void showShortToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT);
    }
}
