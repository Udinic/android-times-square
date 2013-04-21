package com.squareup.timessquare;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.squareup.timessquare.Utils.betweenDates;
import static java.util.Calendar.*;

/**
 * Created with IntelliJ IDEA.
 * User: Udini
 * Date: 21/04/13
 * Time: 17:01
 */
public class Utils {

    /**
     * Clears out the hours/minutes/seconds/millis of a Calendar.
     */
    public static void setMidnight(Calendar cal) {
        cal.set(HOUR_OF_DAY, 0);
        cal.set(MINUTE, 0);
        cal.set(SECOND, 0);
        cal.set(MILLISECOND, 0);
    }

    public static boolean sameDate(Calendar cal, Calendar selectedDate) {
        return cal.get(MONTH) == selectedDate.get(MONTH)
                && cal.get(YEAR) == selectedDate.get(YEAR)
                && cal.get(DAY_OF_MONTH) == selectedDate.get(DAY_OF_MONTH);
    }

    public static boolean sameMonth(Calendar cal, MonthDescriptor month) {
        return (cal.get(MONTH) == month.getMonth() && cal.get(YEAR) == month.getYear());
    }

    public static boolean containsDate(Iterable<Calendar> selectedCals, Calendar cal) {
        for (Calendar selectedCal : selectedCals) {
            if (sameDate(cal, selectedCal)) {
                return true;
            }
        }
        return false;
    }

    public static boolean betweenDates(Calendar cal, Calendar minCal, Calendar maxCal) {
        final Date date = cal.getTime();
        return betweenDates(date, minCal, maxCal);
    }

    static boolean betweenDates(Date date, Calendar minCal, Calendar maxCal) {
        final Date min = minCal.getTime();
        return (date.equals(min) || date.after(min)) // >= minCal
                && date.before(maxCal.getTime()); // && < maxCal
    }

    /** Returns a string summarizing what the client sent us for init() params. */
    public static String dbg(Iterable<Date> selectedDates, Date minDate, Date maxDate) {
        String dbgString = "minDate: " + minDate + "\nmaxDate: " + maxDate;
        if (selectedDates == null) {
            dbgString += "\nselectedDates: null";
        } else {
            dbgString += "\nselectedDates: ";
            for (Date selectedDate : selectedDates) {
                dbgString += selectedDate + "; ";
            }
        }
        return dbgString;
    }
}
