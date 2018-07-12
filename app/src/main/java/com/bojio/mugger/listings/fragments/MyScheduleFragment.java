package com.bojio.mugger.listings.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import com.bojio.mugger.R;
import com.bojio.mugger.listings.ListingUtils;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.mateware.snacky.Snacky;
import needle.Needle;

public class MyScheduleFragment extends Fragment {

  private MyScheduleViewModel mViewModel;
  @BindView(R.id.my_schedule_calendar)
  MaterialCalendarView calendarView;
  @BindView(R.id.my_schedule_recycler)
  RecyclerView mRecyclerView;

  public static MyScheduleFragment newInstance() {
    return new MyScheduleFragment();
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    View view =  inflater.inflate(R.layout.my_schedule_fragment, container, false);
    ButterKnife.bind(this, view);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    return view;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    mViewModel = ViewModelProviders.of(this).get(MyScheduleViewModel.class);
    Needle.onBackgroundThread()
        .execute(() -> {
          HashSet<CalendarDay> days = mViewModel.onMonthChanged(CalendarDay.from(new Date
              (ListingUtils.getMonthTimestamp(System.currentTimeMillis()))));
          Needle.onMainThread().execute(() -> {
            calendarView.addDecorator(new EventDecorator(Color
                .RED, days));
          });
        });

  }

  private class EventDecorator implements DayViewDecorator {

    private final int color;
    private final HashSet<CalendarDay> dates;

    public EventDecorator(int color, Collection<CalendarDay> dates) {
      this.color = color;
      this.dates = new HashSet<>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
      return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
      view.addSpan(new DotSpan(5, color));
    }
  }
}
