package usc.edu.ph.taskybear;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CalendarViewActivity extends AppCompatActivity {

    private TextView calendarInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_view);

        calendarInfo = findViewById(R.id.calendarInfo);

        // Get data passed from ScheduleActivity
        String link = getIntent().getStringExtra("link");
        String date = getIntent().getStringExtra("date");
        boolean everyday = getIntent().getBooleanExtra("everyday", false);
        boolean duration = getIntent().getBooleanExtra("duration", false);

        String displayText = "Link: " + link + "\nDate: " + date +
                "\nEveryday: " + everyday + "\nDuration: " + duration;

        calendarInfo.setText(displayText);
    }
}
