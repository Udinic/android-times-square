package com.squareup.timessquare;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.squareup.timessquare.Utils.*;
import static java.util.Calendar.*;

/**
 * Created with IntelliJ IDEA.
 * User: Udini
 * Date: 18/04/13
 * Time: 18:29
 */
public class CalendarViewPager extends ViewPager {

    private final Calendar minCal = Calendar.getInstance();
    private final Calendar maxCal = Calendar.getInstance();
    private final MonthView.Listener listener = new CellClickedListener();
    final Calendar today = Calendar.getInstance();
    private boolean multiSelect;
    private final List<MonthCellDescriptor> selectedCells = new ArrayList<MonthCellDescriptor>();
    private DateFormat weekdayNameFormat;
    private DateFormat fullDateFormat;
    private final List<Calendar> selectedCals = new ArrayList<Calendar>();
    private OnDateSelectedListener dateListener;
    private final Calendar monthCounter = Calendar.getInstance();
    private DateFormat monthNameFormat;

    final List<MonthDescriptor> months = new ArrayList<MonthDescriptor>();
    final List<List<List<MonthCellDescriptor>>> cells = new ArrayList<List<List<MonthCellDescriptor>>>();

    public CalendarViewPager(Context context) {
        super(context);
        initComponent(context);
    }

    public CalendarViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initComponent(context);
    }

    protected void initComponent(Context context) {

        monthNameFormat = new SimpleDateFormat(context.getString(R.string.month_name_format));
        weekdayNameFormat = new SimpleDateFormat(context.getString(com.squareup.timessquare.R.string.day_name_format));
        fullDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
    }

    public void init(Date selectedDate, Date minDate, Date maxDate, FragmentManager fragmentManager) {

        FragmentStatePagerAdapter adapter = new MonthAdapter(fragmentManager);
        setAdapter(adapter);

        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        init(selectedDate, minDate, maxDate);
    }

    /**
     * Gets a value indicating whether the user can select several dates or only
     * a single one.
     *
     * @return true to select mutiple dates, false to select only one date
     */
    public boolean getMultiSelect() {
        return multiSelect;
    }

    /**
     * Sets a value indicating whether the user can select several dates or only
     * a single one.
     *
     * @param value true to select mutiple dates, false to select only one date
     */
    public void setMultiSelect(boolean value) {
        multiSelect = value;
    }

    /**
     * All date parameters must be non-null and their {@link java.util.Date#getTime()} must not
     * return 0.  Time of day will be ignored.  For instance, if you pass in {@code minDate} as
     * 11/16/2012 5:15pm and {@code maxDate} as 11/16/2013 4:30am, 11/16/2012 will be the first
     * selectable date and 11/15/2013 will be the last selectable date ({@code maxDate} is
     * exclusive).
     *
     * @param minDate Earliest selectable date, inclusive.  Must be earlier than {@code maxDate}.
     * @param maxDate Latest selectable date, exclusive.  Must be later than {@code minDate}.
     */
    public void init(Date minDate, Date maxDate) {
        initialize(null, minDate, maxDate);
    }

    /**
     * All date parameters must be non-null and their {@link java.util.Date#getTime()} must not
     * return 0.  Time of day will be ignored.  For instance, if you pass in {@code minDate} as
     * 11/16/2012 5:15pm and {@code maxDate} as 11/16/2013 4:30am, 11/16/2012 will be the first
     * selectable date and 11/15/2013 will be the last selectable date ({@code maxDate} is
     * exclusive).
     *
     * @param selectedDate Initially selected date.  Must be between {@code minDate} and {@code
     * maxDate}.
     * @param minDate Earliest selectable date, inclusive.  Must be earlier than {@code maxDate}.
     * @param maxDate Latest selectable date, exclusive.  Must be later than {@code minDate}.
     */
    public void init(Date selectedDate, Date minDate, Date maxDate) {
        setMultiSelect(false);
        initialize(Arrays.asList(selectedDate), minDate, maxDate);
    }

    /**
     * All date parameters must be non-null and their {@link java.util.Date#getTime()} must not
     * return 0.  Time of day will be ignored.  For instance, if you pass in {@code minDate} as
     * 11/16/2012 5:15pm and {@code maxDate} as 11/16/2013 4:30am, 11/16/2012 will be the first
     * selectable date and 11/15/2013 will be the last selectable date ({@code maxDate} is
     * exclusive).
     *
     * @param selectedDates Initially selected dates.  Must be between {@code minDate} and {@code
     * maxDate}.
     * @param minDate Earliest selectable date, inclusive.  Must be earlier than {@code maxDate}.
     * @param maxDate Latest selectable date, exclusive.  Must be later than {@code minDate}.
     */
    public void init(Iterable<Date> selectedDates, Date minDate, Date maxDate) {
        setMultiSelect(true);
        initialize(selectedDates, minDate, maxDate);
    }

    private void initialize(Iterable<Date> selectedDates, Date minDate, Date maxDate) {
        if (minDate == null || maxDate == null) {
            throw new IllegalArgumentException(
                    "minDate and maxDate must be non-null.  " + dbg(selectedDates, minDate, maxDate));
        }
        if (minDate.after(maxDate)) {
            throw new IllegalArgumentException(
                    "Min date must be before max date.  " + dbg(selectedDates, minDate, maxDate));
        }
        if (minDate.getTime() == 0 || maxDate.getTime() == 0) {
            throw new IllegalArgumentException(
                    "minDate and maxDate must be non-zero.  " + dbg(selectedDates, minDate, maxDate));
        }

        selectedCals.clear();
        selectedCells.clear();
        if (selectedDates != null) {
            for (Date selectedDate : selectedDates) {
                if (selectedDate.getTime() == 0) {
                    throw new IllegalArgumentException(
                            "Selected date must be non-zero.  " + dbg(selectedDates, minDate, maxDate));
                }

                if (selectedDate.before(minDate) || selectedDate.after(maxDate)) {
                    throw new IllegalArgumentException(
                            "selectedDate must be between minDate and maxDate.  " + dbg(selectedDates, minDate,
                                    maxDate));
                }

                Calendar selectedCal = Calendar.getInstance();
                selectedCals.add(selectedCal);
                // Sanitize input: clear out the hours/minutes/seconds/millis.
                selectedCal.setTime(selectedDate);
                setMidnight(selectedCal);
            }
        }

        // Clear previous state.
        cells.clear();
        months.clear();
        minCal.setTime(minDate);
        maxCal.setTime(maxDate);
        setMidnight(minCal);
        setMidnight(maxCal);

        // maxDate is exclusive: bump back to the previous day so if maxDate is the first of a month,
        // we don't accidentally include that month in the view.
        maxCal.add(MINUTE, -1);

        // Now iterate between minCal and maxCal and build up our list of months to show.
        monthCounter.setTime(minCal.getTime());
        final int maxMonth = maxCal.get(MONTH);
        final int maxYear = maxCal.get(YEAR);
        int selectedIndex = 0;
        int todayIndex = 0;
        Calendar today = Calendar.getInstance();
        while ((monthCounter.get(MONTH) <= maxMonth // Up to, including the month.
                || monthCounter.get(YEAR) < maxYear) // Up to the year.
                && monthCounter.get(YEAR) < maxYear + 1) { // But not > next yr.
            MonthDescriptor month = new MonthDescriptor(monthCounter.get(MONTH), monthCounter.get(YEAR),
                    monthNameFormat.format(monthCounter.getTime()));
            cells.add(getMonthCells(month, monthCounter));
            Logr.d("Adding month %s", month);
            if (selectedIndex == 0) {
                for (Calendar selectedCal : selectedCals) {
                    if (sameMonth(selectedCal, month)) {
                        selectedIndex = months.size();
                        break;
                    }
                }
                if (selectedIndex == 0 && todayIndex == 0 && sameMonth(today, month)) {
                    todayIndex = months.size();
                }
            }
            months.add(month);
            monthCounter.add(MONTH, 1);
        }

        getAdapter().notifyDataSetChanged();
        if (selectedIndex != 0 || todayIndex != 0) {
            scrollToSelectedMonth(selectedIndex != 0 ? selectedIndex : todayIndex);
        }
    }

    List<List<MonthCellDescriptor>> getMonthCells(MonthDescriptor month, Calendar startCal) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startCal.getTime());
        List<List<MonthCellDescriptor>> cells = new ArrayList<List<MonthCellDescriptor>>();
        cal.set(DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(DAY_OF_WEEK);
        cal.add(DATE, cal.getFirstDayOfWeek() - firstDayOfWeek);
        while ((cal.get(MONTH) < month.getMonth() + 1 || cal.get(YEAR) < month.getYear()) //
                && cal.get(YEAR) <= month.getYear()) {
            Logr.d("Building week row starting at %s", cal.getTime());
            List<MonthCellDescriptor> weekCells = new ArrayList<MonthCellDescriptor>();
            cells.add(weekCells);
            for (int c = 0; c < 7; c++) {
                Date date = cal.getTime();
                boolean isCurrentMonth = cal.get(MONTH) == month.getMonth();
                boolean isSelected = isCurrentMonth && containsDate(selectedCals, cal);
                boolean isSelectable = isCurrentMonth && betweenDates(cal, minCal, maxCal);
                boolean isToday = sameDate(cal, today);
                int value = cal.get(DAY_OF_MONTH);
                MonthCellDescriptor cell =
                        new MonthCellDescriptor(date, isCurrentMonth, isSelectable, isSelected, isToday, value);
                if (isSelected) {
                    selectedCells.add(cell);
                }
                weekCells.add(cell);
                cal.add(DATE, 1);
            }
        }
        return cells;
    }

    public Date getSelectedDate() {
        return (selectedCals.size() > 0 ? selectedCals.get(0).getTime() : null);
    }

    public Iterable<Date> getSelectedDates() {
        List<Date> selectedDates = new ArrayList<Date>();
        for (Calendar cal : selectedCals) {
            selectedDates.add(cal.getTime());
        }
        Collections.sort(selectedDates);
        return selectedDates;
    }

    private void scrollToSelectedMonth(final int selectedIndex) {
        post(new Runnable() {
            @Override public void run() {
                setCurrentItem(selectedIndex);
            }
        });
    }

    private class MonthAdapter extends FragmentStatePagerAdapter {

        public MonthAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return new MonthFragment(i);
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();

            // Updating the current fragments
            int start = getCurrentItem() - getOffscreenPageLimit();
            int end = getCurrentItem() + getOffscreenPageLimit() + 1;

            // Normalizing the positions, in case we're going out of bounds.
            if (start < 0) start = 0;
            if (end > getCount()) end = getCount();

            for (int position = start; position < end; position++) {

                // The instantiateItem will return the fragments since they already created
                Fragment fragment = (Fragment) instantiateItem(CalendarViewPager.this, position);
                View mainView = fragment.getView();
                if (mainView == null) {
                    Log.d("udini", "mainView ["+position+"] null. Nothing to do here");
                    continue;
                }
                MonthView view = ((MonthView) mainView.findViewById(R.id.monthView));
                view.init(months.get(position), cells.get(position));
            }
        }

        @Override
        public int getCount() {
            return months.size();
        }
    }

    private class CellClickedListener implements MonthView.Listener {
        @Override public void handleClick(MonthCellDescriptor cell) {
            if (!betweenDates(cell.getDate(), minCal, maxCal)) {
                String errMessage =
                        getResources().getString(R.string.invalid_date, fullDateFormat.format(minCal.getTime()),
                                fullDateFormat.format(maxCal.getTime()));
                Toast.makeText(getContext(), errMessage, Toast.LENGTH_SHORT).show();
            } else {
                Date selectedDate = cell.getDate();
                Calendar selectedCal = Calendar.getInstance();
                selectedCal.setTime(selectedDate);

                if (getMultiSelect()) {
                    for (MonthCellDescriptor selectedCell : selectedCells) {
                        if (selectedCell.getDate().equals(selectedDate)) {
                            // De-select the currently-selected cell.
                            selectedCell.setSelected(false);
                            selectedCells.remove(selectedCell);
                            selectedDate = null;
                            break;
                        }
                    }
                    for (Calendar cal : selectedCals) {
                        if (sameDate(cal, selectedCal)) {
                            selectedCals.remove(cal);
                            break;
                        }
                    }
                } else {
                    for (MonthCellDescriptor selectedCell : selectedCells) {
                        // De-select the currently-selected cell.
                        selectedCell.setSelected(false);
                    }
                    selectedCells.clear();
                    selectedCals.clear();
                }

                if (selectedDate != null) {
                    // Select a new cell.
                    selectedCells.add(cell);
                    cell.setSelected(true);
                    selectedCals.add(selectedCal);
                }

                // Update the adapter.
                getAdapter().notifyDataSetChanged();

                if (selectedDate != null && dateListener != null) {
                    dateListener.onDateSelected(selectedDate);
                }
            }
        }
    }


    /**
     * Month Fragment - a single "page" in the months view pager
     */
    private class MonthFragment extends android.support.v4.app.Fragment {

        int position;

        public MonthFragment(int position) {
            super();
            this.position = position;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            MonthView monthView = MonthView.create(container, inflater, weekdayNameFormat, listener, today);
            monthView.init(months.get(position), cells.get(position));
            return monthView;
        }
    }

    public interface OnDateSelectedListener {
        void onDateSelected(Date date);
    }

}
