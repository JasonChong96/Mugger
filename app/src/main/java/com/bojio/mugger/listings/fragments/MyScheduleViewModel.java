package com.bojio.mugger.listings.fragments;

import android.arch.lifecycle.ViewModel;

import com.bojio.mugger.database.MuggerDatabase;
import com.bojio.mugger.listings.ListingUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

public class MyScheduleViewModel extends ViewModel {
  private FirebaseFirestore db;
  private FirebaseAuth mAuth;

  public MyScheduleViewModel() {
    db = FirebaseFirestore.getInstance();
    mAuth = FirebaseAuth.getInstance();

  }

  public HashSet<CalendarDay> onMonthChanged(CalendarDay firstDay) {
    long nextMonthFirstDay = getNextMonth(firstDay);
    long cur = firstDay.getDate().getTime();
    int month = firstDay.getCalendar().get(Calendar.MONTH);
    HashSet<CalendarDay> markedDays = new HashSet<>();
    Task<QuerySnapshot> task = MuggerDatabase.getAllListingsReference(db).orderBy(mAuth.getUid())
        .get();
    try {
      Tasks.await(task);
    } catch (ExecutionException | InterruptedException e) {
      return null;
    }
    if (task.isSuccessful()) {
      for (DocumentSnapshot docSnap : task.getResult().getDocuments()) {
        long start = (Long) docSnap.get("startTime");
        long end = (Long) docSnap.get("endTime");
        Date curDate = new Date(ListingUtils.getDayTimestamp(start));
        while (curDate.getTime() < end) {
          markedDays.add(CalendarDay.from(curDate));
          curDate = new Date(curDate.getTime() + 24 * 60 * 60 * 1000);
        }
      }
    }
    return markedDays;
  }

  private static long getNextMonth(CalendarDay day) {
    Calendar calDay = day.getCalendar();
    int month = calDay.get(Calendar.MONTH);
    if (month >= Calendar.DECEMBER) {
      calDay.set(Calendar.MONTH, Calendar.JANUARY);
      calDay.set(Calendar.YEAR, calDay.get(Calendar.YEAR) + 1);
    } else {
      calDay.set(Calendar.MONTH, calDay.get(Calendar.MONTH) + 1);
    }
    return calDay.getTimeInMillis();
  }
}
