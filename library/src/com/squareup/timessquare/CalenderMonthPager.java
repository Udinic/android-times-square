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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.util.Calendar.*;

/**
 * Created with IntelliJ IDEA.
 * User: Udini
 * Date: 18/04/13
 * Time: 18:29
 */
public class CalenderMonthPager extends ViewPager {

    private final Calendar minCal = Calendar.getInstance();
    private final Calendar maxCal = Calendar.getInstance();
    private final MonthView.Listener listener = new CellClickedListener();
    final Calendar today = Calendar.getInstance();
    private MonthCellDescriptor selectedCell;
    private DateFormat weekdayNameFormat;
    private DateFormat fullDateFormat;
    private final Calendar selectedCal = Calendar.getInstance();
    private OnDateSelectedListener dateListener;
    private final Calendar monthCounter = Calendar.getInstance();
    private DateFormat monthNameFormat;

    final List<MonthDescriptor> months = new ArrayList<MonthDescriptor>();
    final List<List<List<MonthCellDescriptor>>> cells = new ArrayList<List<List<MonthCellDescriptor>>>();

    public CalenderMonthPager(Context context) {
        super(context);
        initComponent(context);
    }

    public CalenderMonthPager(Context context, AttributeSet attrs) {
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
     * All date parameters must be non-null and their {@link java.util.Date#getTime()} must not
     * return 0.  Time of day will be ignored.  For instance, if you pass in {@code minDate} as
     * 11/16/2012 5:15pm and {@code maxDate} as 11/16/2013 4:30am, 11/16/2012 will be the first
     * selectable date and 11/15/2013 will be the last selectable date ({@code maxDate} is exclusive).
     *
     * @param selectedDate Initially selected date.  Must be between {@code minDate} and {@code
     *                     maxDate}.
     * @param minDate      Earliest selectable date, inclusive.  Must be earlier than {@code maxDate}.
     * @param maxDate      Latest selectable date, exclusive.  Must be later than {@code minDate}.
     */
    private void init(Date selectedDate, Date minDate, Date maxDate) {

        // Clear previous state.
        cells.clear();
        months.clear();

        // Sanitize input: clear out the hours/minutes/seconds/millis.
        selectedCal.setTime(selectedDate);
        minCal.setTime(minDate);
        maxCal.setTime(maxDate);
        setMidnight(selectedCal);
        setMidnight(minCal);
        setMidnight(maxCal);
        // maxDate is exclusive: bump back to the previous day so if maxDate is the first of a month,
        // we don't accidentally include that month in the view.
        maxCal.add(MINUTE, -1);

        // Now iterate between minCal and maxCal and build up our list of months to show.
        monthCounter.setTime(minCal.getTime());
        final int maxMonth = maxCal.get(MONTH);
        final int maxYear = maxCal.get(YEAR);
        final int selectedYear = selectedCal.get(YEAR);
        final int selectedMonth = selectedCal.get(MONTH);
        int selectedIndex = 0;
        while ((monthCounter.get(MONTH) <= maxMonth // Up to, including the month.
                || monthCounter.get(YEAR) < maxYear) // Up to the year.
                && monthCounter.get(YEAR) < maxYear + 1) { // But not > next yr.
            MonthDescriptor month = new MonthDescriptor(monthCounter.get(MONTH), monthCounter.get(YEAR),
                    monthNameFormat.format(monthCounter.getTime()));
            cells.add(getMonthCells(month, monthCounter, selectedCal));
            Logr.d("Adding month %s", month);
            if (selectedMonth == month.getMonth() && selectedYear == month.getYear()) {
                selectedIndex = months.size();
            }
            months.add(month);
            monthCounter.add(MONTH, 1);
        }
//        getAdapter().notifyDataSetChanged();
//        if (selectedIndex != 0) {
//            scrollToSelectedMonth(selectedIndex);
//        }
    }

    List<List<MonthCellDescriptor>> getMonthCells(MonthDescriptor month, Calendar startCal,
                                                  Calendar selectedDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(startCal.getTime());
        List<List<MonthCellDescriptor>> cells = new ArrayList<List<MonthCellDescriptor>>();
        cal.set(DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(DAY_OF_WEEK);
        cal.add(DATE, SUNDAY - firstDayOfWeek);
        while ((cal.get(MONTH) < month.getMonth() + 1 || cal.get(YEAR) < month.getYear()) //
                && cal.get(YEAR) <= month.getYear()) {
            Logr.d("Building week row starting at %s", cal.getTime());
            List<MonthCellDescriptor> weekCells = new ArrayList<MonthCellDescriptor>();
            cells.add(weekCells);
            for (int c = 0; c < 7; c++) {
                Date date = cal.getTime();
                boolean isCurrentMonth = cal.get(MONTH) == month.getMonth();
                boolean isSelected = isCurrentMonth && sameDate(cal, selectedDate);
                boolean isSelectable = isCurrentMonth && betweenDates(cal, minCal, maxCal);
                boolean isToday = sameDate(cal, today);
                int value = cal.get(DAY_OF_MONTH);
                MonthCellDescriptor cell =
                        new MonthCellDescriptor(date, isCurrentMonth, isSelectable, isSelected, isToday, value);
                if (isSelected) {
                    selectedCell = cell;
                }
                weekCells.add(cell);
                cal.add(DATE, 1);
            }
        }
        return cells;
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

            int start = getCurrentItem() - getOffscreenPageLimit();
            int end = getCurrentItem() + getOffscreenPageLimit() + 1;

            // Normalizing the positions, in case we're going out of bounds.
            if (start < 0) start = 0;
            if (end > getCount()) end = getCount();

            for (int position = start; position < end; position++) {
//                Log.d("udini", "notifyDataSetChanged[" + position + "]");

                Fragment fragment = (Fragment) instantiateItem(CalenderMonthPager.this, position);
                View mainView = fragment.getView();
//                if (mainView == null) {
//                    Log.d("udini", "view[" + position + "] is null");
//                    return;
//                }

                MonthView view = ((MonthView) mainView.findViewById(R.id.monthView));
                view.init(months.get(position), cells.get(position));
            }
        }

        @Override
        public int getCount() {
            return 12;
        }
    }

    private class CellClickedListener implements MonthView.Listener {
        @Override
        public void handleClick(MonthCellDescriptor cell) {
            if (!betweenDates(cell.getDate(), minCal, maxCal)) {
                String errMessage =
                        getResources().getString(com.squareup.timessquare.R.string.invalid_date, fullDateFormat.format(minCal.getTime()),
                                fullDateFormat.format(maxCal.getTime()));
                Toast.makeText(getContext(), errMessage, Toast.LENGTH_SHORT).show();
            } else {
                // De-select the currently-selected cell.
                selectedCell.setSelected(false);
                // Select the new cell.
                selectedCell = cell;
                selectedCell.setSelected(true);
                // Track the currently selected date value.
                selectedCal.setTime(cell.getDate());

                // Update the adapter.
                getAdapter().notifyDataSetChanged();

                if (dateListener != null) {
                    dateListener.onDateSelected(cell.getDate());
                }
            }
        }
    }

    private class MonthFragment extends android.support.v4.app.Fragment {

        int position;

        public MonthFragment(int position) {
            super();

            this.position = position;

//            Bundle args = new Bundle();
//            args.putInt("pos", position);
//            setArguments(args);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//            MonthView monthView = (MonthView)inflater.inflate(R.layout.month, container, false);

            MonthView monthView = MonthView.create(container, inflater, weekdayNameFormat, listener, today);

            // GET FROM getArguments
            monthView.init(months.get(position), cells.get(position));
            return monthView;
        }


    }

    /**
     * Clears out the hours/minutes/seconds/millis of a Calendar.
     */
    private static void setMidnight(Calendar cal) {
        cal.set(HOUR_OF_DAY, 0);
        cal.set(MINUTE, 0);
        cal.set(SECOND, 0);
        cal.set(MILLISECOND, 0);
    }


    private static boolean sameDate(Calendar cal, Calendar selectedDate) {
        return cal.get(MONTH) == selectedDate.get(MONTH)
                && cal.get(YEAR) == selectedDate.get(YEAR)
                && cal.get(DAY_OF_MONTH) == selectedDate.get(DAY_OF_MONTH);
    }

    private static boolean betweenDates(Calendar cal, Calendar minCal, Calendar maxCal) {
        final Date date = cal.getTime();
        return betweenDates(date, minCal, maxCal);
    }

    static boolean betweenDates(Date date, Calendar minCal, Calendar maxCal) {
        final Date min = minCal.getTime();
        return (date.equals(min) || date.after(min)) // >= minCal
                && date.before(maxCal.getTime()); // && < maxCal
    }

    public interface OnDateSelectedListener {
        void onDateSelected(Date date);
    }

}
