package com.squareup.timessquare.sample;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.squareup.timessquare.CalendarPickerView;
import com.squareup.timessquare.CalenderMonthPager;

import java.util.Calendar;
import java.util.Date;

import static android.widget.Toast.LENGTH_SHORT;

public class SamplePagerTimesSquareActivity extends FragmentActivity {
  private static final String TAG = "SamplePagerTimesSquareActivity";

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.calendar_pager);

    Calendar nextYear = Calendar.getInstance();
    nextYear.add(Calendar.YEAR, 1);

    final CalenderMonthPager calendar = (CalenderMonthPager) findViewById(R.id.calendar_view);
    calendar.init(new Date(), new Date(), nextYear.getTime(), getSupportFragmentManager());

//    findViewById(R.id.done_button).setOnClickListener(new OnClickListener() {
//      @Override public void onClick(View view) {
//        Log.d(TAG, "Selected time in millis: " + calendar.getSelectedDate().getTime());
//        String toast = "Selected: " + calendar.getSelectedDate().getTime();
//        Toast.makeText(SamplePagerTimesSquareActivity.this, toast, LENGTH_SHORT).show();
//      }
//    });
  }
}
