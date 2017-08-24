package com.joelzhu.calendar;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        JZCalendar calendar = (JZCalendar) findViewById(R.id.calendar_calendar);
        calendar.setOnDateClickListener(new JZCalendar.OnDateClickListener() {
            @Override
            public void OnDateClick(int clickYear, int clickMonth, int clickDate) {
                String date = String.format("%s年%s月%s日", clickYear, clickMonth, clickDate);
                Toast.makeText(MainActivity.this, date, Toast.LENGTH_SHORT).show();
            }
        });
    }
}