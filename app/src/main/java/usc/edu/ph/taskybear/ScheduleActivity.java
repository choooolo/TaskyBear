package usc.edu.ph.taskybear;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class ScheduleActivity extends AppCompatActivity {
    private TableLayout tableLayout;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        tableLayout = findViewById(R.id.timetable);
        dbHelper = new DatabaseHelper(this);

        int userId = getSharedPreferences("TaskyPrefs", MODE_PRIVATE).getInt("userId", -1);
        populateTimetable(userId);
    }

    private void populateTimetable(int userId) {
        // Clear existing views
        tableLayout.removeAllViews();

        // Create header row
        TableRow headerRow = new TableRow(this);
        addHeaderCell(headerRow, "Time");
        addHeaderCell(headerRow, "Monday");
        addHeaderCell(headerRow, "Tuesday");
        addHeaderCell(headerRow, "Wednesday");
        addHeaderCell(headerRow, "Thursday");
        addHeaderCell(headerRow, "Friday");
        addHeaderCell(headerRow, "Saturday");
        tableLayout.addView(headerRow);

        // Define time slots
        String[] timeSlots = {
                "07:30 AM - 08:00 AM",
                "08:00 AM - 08:30 AM",
                "08:30 AM - 09:00 AM",
                "09:00 AM - 09:30 AM",
                "09:30 AM - 10:00 AM",
                "10:00 AM - 10:30 AM",
                "10:30 AM - 11:00 AM",
                "11:00 AM - 11:30 AM",
                "11:30 AM - 12:00 PM",
                "12:00 PM - 12:30 PM",
                "12:30 PM - 01:00 PM",
                "01:00 PM - 01:30 PM",
                "01:30 PM - 02:00 PM",
                "02:00 PM - 02:30 PM",
                "02:30 PM - 03:00 PM",
                "03:00 PM - 03:30 PM",
                "03:30 PM - 04:00 PM",
                "04:00 PM - 04:30 PM"
        };

        // Get all timetable entries
        List<TimetableEntry> entries = dbHelper.getTimetableEntries(userId);

        // Create rows for each time slot
        for (String timeSlot : timeSlots) {
            TableRow row = new TableRow(this);
            addCell(row, timeSlot, false);

            // Add cells for each day (Monday to Saturday)
            for (int day = 0; day < 6; day++) {
                String dayName = getDayName(day);
                String classInfo = findClassForTimeSlot(entries, dayName, timeSlot);

                TextView cell = new TextView(this);
                cell.setText(classInfo);
                cell.setPadding(8, 8, 8, 8);

                // Make the cell clickable if it has a class
                if (!classInfo.isEmpty()) {
                    String type = getTypeForClass(entries, dayName, timeSlot);
                    cell.setOnClickListener(v -> {
                        Intent intent = new Intent(ScheduleActivity.this, ToDoActivity.class);
                        intent.putExtra("filterType", type);
                        startActivity(intent);
                    });
                }

                row.addView(cell);
            }

            tableLayout.addView(row);
        }
    }

    private String getDayName(int index) {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        return days[index];
    }

    private String findClassForTimeSlot(List<TimetableEntry> entries, String day, String timeSlot) {
        for (TimetableEntry entry : entries) {
            if (entry.getDay().equalsIgnoreCase(day) &&
                    entry.getStartTime().equals(timeSlot.split(" - ")[0])) {
                return entry.getClassName() + "\n" + entry.getLocation();
            }
        }
        return "";
    }

    private String getTypeForClass(List<TimetableEntry> entries, String day, String timeSlot) {
        for (TimetableEntry entry : entries) {
            if (entry.getDay().equalsIgnoreCase(day) &&
                    entry.getStartTime().equals(timeSlot.split(" - ")[0])) {
                return entry.getType();
            }
        }
        return "";
    }

    private void addHeaderCell(TableRow row, String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        textView.setBackgroundColor(getResources().getColor(R.color.table_header));
        row.addView(textView);
    }

    private void addCell(TableRow row, String text, boolean withBorder) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        if (withBorder) {
            textView.setBackgroundResource(R.drawable.cell_border);
        }
        row.addView(textView);
    }
}