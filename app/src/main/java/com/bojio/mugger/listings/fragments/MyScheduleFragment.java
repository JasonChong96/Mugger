package com.bojio.mugger.listings.fragments;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bojio.mugger.R;
import com.bojio.mugger.lifecycle.LifecycleUtils;
import com.bojio.mugger.listings.Listing;
import com.bojio.mugger.listings.ListingUtils;
import com.bojio.mugger.listings.ListingsFirestoreAdapter;
import com.bojio.mugger.listings.viewmodels.MyScheduleViewModel;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Collection;

import butterknife.BindView;
import butterknife.ButterKnife;
import needle.Needle;

public class MyScheduleFragment extends Fragment {

  private static ViewModelProvider.AndroidViewModelFactory factory = null;
  @BindView(R.id.my_schedule_calendar)
  MaterialCalendarView calendarView;
  @BindView(R.id.my_schedule_recycler)
  RecyclerView mRecyclerView;
  @BindView(R.id.my_schedule_swipe_layout)
  SwipeRefreshLayout swipeLayout;
  @BindView(R.id.my_schedule_empty_text_view)
  TextView emptyTextView;
  private MyScheduleViewModel mViewModel;

  public static MyScheduleFragment newInstance() {
    return new MyScheduleFragment();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.my_schedule_fragment, container, false);
    ButterKnife.bind(this, view);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    swipeLayout.setOnRefreshListener(() -> updateCalendarUI(true));
    return view;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    mViewModel = ViewModelProviders.of(this, LifecycleUtils.getAndroidViewModelFactory
        (getActivity().getApplication())).get(MyScheduleViewModel.class);
    updateCalendarUI(false);
    calendarView.setOnDateChangedListener(this::onDateChanged);
    CalendarDay selectedDay = mViewModel.getSelectedDay();
    if (selectedDay != null) {
      calendarView.setSelectedDate(selectedDay);
      onDateChanged(calendarView, selectedDay, true);
    }
  }

  /**
   * Called when selected date is changed in calendarView. Loads
   *
   * @param calendar    the calendar view that the selected date has been changed in
   * @param selectedDay the selected day
   * @param selected    the day's selected state
   */
  private void onDateChanged(@NonNull MaterialCalendarView calendar, @NonNull CalendarDay
      selectedDay, boolean selected) {
    if (selected) {
      FirestoreRecyclerOptions<Listing> options = ListingUtils.getRecyclerOptions(mViewModel.getQuery(),
          this);
      ListingsFirestoreAdapter adapter = new ListingsFirestoreAdapter(options, getActivity(),
          FirebaseAuth.getInstance(), FirebaseFirestore.getInstance(), FirebaseMessaging
          .getInstance(), mViewModel.getFilter(selectedDay), emptyTextView);
      mRecyclerView.setAdapter(adapter);
      adapter.startListening();
    }
  }

  /**
   * Called when the page is first loaded or when the user triggers a refresh. Updates the
   * calendar UI with the dates that the user has scheduled study sessions.
   *
   * @param refresh if true, reloads data from the Firestore database, or else from the cache
   */
  private void updateCalendarUI(boolean refresh) {
    swipeLayout.setRefreshing(true);
    Needle.onBackgroundThread()
        .execute(() -> {
          Collection<CalendarDay> days = mViewModel.getMarkedDays(refresh);
          Needle.onMainThread().execute(() -> {
            calendarView.removeDecorators();
            calendarView.addDecorator(new EventDecorator(Color
                .RED, days));
            swipeLayout.setRefreshing(false);
          });
        });
  }

  /**
   * A Decorator class which sets which dates to decorate and with what it should be decorated
   * with. This decorator adds a colored dot under dates that are to be decorated.
   */
  private class EventDecorator implements DayViewDecorator {

    private final int color;
    private final Collection<CalendarDay> dates;

    /**
     * Constructor for EventDecorator.
     *
     * @param color the color of the dots
     * @param dates dates to be decorated with dots
     */
    public EventDecorator(int color, Collection<CalendarDay> dates) {
      this.color = color;
      this.dates = dates;
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
