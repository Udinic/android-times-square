package com.squareup.timessquare.sample;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.squareup.timessquare.CalendarPickerView;
import com.squareup.timessquare.CalenderMonthPager;

import java.util.Calendar;
import java.util.Date;

import static android.widget.Toast.LENGTH_SHORT;

public class SampleListTimesSquareActivity extends FragmentActivity {
  private static final String TAG = "SamplePagerTimesSquareActivity";

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.calendar_picker);

    Calendar nextYear = Calendar.getInstance();
    nextYear.add(Calendar.YEAR, 1);

    final CalendarPickerView calendar = (CalendarPickerView) findViewById(R.id.calendar_view);
    calendar.init(new Date(), new Date(), nextYear.getTime());

    findViewById(R.id.done_button).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        Log.d(TAG, "Selected time in millis: " + calendar.getSelectedDate().getTime());
        String toast = "Selected: " + calendar.getSelectedDate().getTime();
        Toast.makeText(SampleListTimesSquareActivity.this, toast, LENGTH_SHORT).show();
      }
    });
  }
}
