package usc.edu.ph.taskybear;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScheduleActivity extends AppCompatActivity {
    private TableLayout tableLayout;
    private DatabaseHelper dbHelper;
    private Button addClassButton;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        tableLayout = findViewById(R.id.timetable);
        dbHelper = new DatabaseHelper(this);
        addClassButton = findViewById(R.id.addClassButton);

        userId = getSharedPreferences("TaskyPrefs", MODE_PRIVATE).getInt("userId", -1);
        populateTimetable(userId);

        addClassButton.setOnClickListener(v -> showAddClassDialog());
    }

    private void populateTimetable(int userId) {
        tableLayout.removeAllViews();
        tableLayout.setStretchAllColumns(true);

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

        // Define time slots (30-minute intervals)
        String[] timeSlots = {
                "07:00 AM", "07:30 AM", "08:00 AM", "08:30 AM", "09:00 AM", "09:30 AM",
                "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM", "12:00 PM", "12:30 PM",
                "01:00 PM", "01:30 PM", "02:00 PM", "02:30 PM", "03:00 PM", "03:30 PM",
                "04:00 PM", "04:30 PM", "05:00 PM", "05:30 PM", "06:00 PM", "06:30 PM",
                "07:00 PM", "07:30 PM", "08:00 PM", "08:30 PM", "09:00 PM"
        };

        List<TimetableEntry> entries = dbHelper.getTimetableEntries(userId);
        Map<String, Integer> timeSlotIndices = new HashMap<>();
        for (int i = 0; i < timeSlots.length; i++) {
            timeSlotIndices.put(timeSlots[i], i);
        }

        // Create a grid to track which cells should be merged
        boolean[][] merged = new boolean[timeSlots.length][6]; // 6 days

        for (int i = 0; i < timeSlots.length; i++) {
            String timeSlot = timeSlots[i];
            TableRow row = new TableRow(this);
            addTimeCell(row, timeSlot);

            for (int day = 0; day < 6; day++) {
                String dayName = getDayName(day);
                TextView cell = new TextView(this);

                // Skip if this cell is part of a merged class
                if (merged[i][day]) {
                    cell.setVisibility(View.GONE);
                    row.addView(cell);
                    continue;
                }

                // Find all entries for this time slot and day
                List<TimetableEntry> dayEntries = findEntriesForTimeSlot(entries, dayName, timeSlot);

                if (!dayEntries.isEmpty()) {
                    // For simplicity, we'll just show the first entry if there are multiple
                    // (you might want to handle overlapping classes differently)
                    TimetableEntry entry = dayEntries.get(0);

                    // Safely get indices
                    Integer startIndex = timeSlotIndices.get(entry.getStartTime());
                    Integer endIndex = timeSlotIndices.get(entry.getEndTime());

                    if (startIndex != null && endIndex != null) {
                        // Calculate how many time slots this class spans
                        int span = endIndex - startIndex;

                        // Mark all cells in this span as merged (except the first one)
                        for (int j = startIndex + 1; j < endIndex; j++) {
                            if (j < merged.length) {
                                merged[j][day] = true;
                            }
                        }

                        // Only create cell for the first time slot of the class
                        if (i == startIndex) {
                            cell.setText(entry.getClassName() + "\n" + entry.getLocation());
                            cell.setBackgroundColor(getColorForCourse(entry.getClassName()));
                            cell.setGravity(Gravity.CENTER);
                            cell.setPadding(8, 8, 8, 8);
                            cell.setTextColor(Color.WHITE);

                            TableRow.LayoutParams params = new TableRow.LayoutParams(
                                    TableRow.LayoutParams.MATCH_PARENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                            );
                            params.height = span * 100; // Approximate height for each time slot
                            cell.setLayoutParams(params);

                            // Set click listener
                            final String finalDayName = dayName;
                            final String finalTimeSlot = timeSlot;
                            cell.setOnClickListener(v ->
                                    showClassOptionsDialog(finalDayName, finalTimeSlot, entry));
                        } else {
                            cell.setVisibility(View.GONE);
                        }
                    }
                } else {
                    // Empty cell
                    cell.setText("");
                    cell.setBackgroundResource(R.drawable.cell_border);
                    cell.setPadding(8, 8, 8, 8);

                    // Set click listener for adding new class
                    final String finalDayName = dayName;
                    final String finalTimeSlot = timeSlot;
                    cell.setOnClickListener(v ->
                            showAddClassDialog(finalDayName, finalTimeSlot));
                }

                row.addView(cell);
            }
            tableLayout.addView(row);
        }
    }

    // Helper method to find all entries for a time slot (might return multiple if overlapping)
    private List<TimetableEntry> findEntriesForTimeSlot(List<TimetableEntry> entries, String day, String timeSlot) {
        List<TimetableEntry> result = new ArrayList<>();
        for (TimetableEntry entry : entries) {
            if (entry.getDay().equalsIgnoreCase(day) &&
                    timeSlot.compareTo(entry.getStartTime()) >= 0 &&
                    timeSlot.compareTo(entry.getEndTime()) < 0) {
                result.add(entry);
            }
        }
        return result;
    }

    private int getColorForCourse(String courseName) {
        // Generate consistent color based on course name
        int hash = courseName.hashCode();
        int color = Color.HSVToColor(new float[]{
                Math.abs(hash % 360),
                0.7f, // More saturation for better visibility
                0.6f  // Slightly darker for better text contrast
        });
        return color;
    }

    private void addTimeCell(TableRow row, String time) {
        TextView timeCell = new TextView(this);
        timeCell.setText(time);
        timeCell.setPadding(8, 8, 8, 8);
        timeCell.setBackgroundResource(R.drawable.cell_border);
        timeCell.setGravity(Gravity.CENTER);
        row.addView(timeCell);
    }

    private TimetableEntry findEntryForTimeSlot(List<TimetableEntry> entries, String day, String timeSlot) {
        for (TimetableEntry entry : entries) {
            if (entry.getDay().equalsIgnoreCase(day) &&
                    timeSlot.compareTo(entry.getStartTime()) >= 0 &&
                    timeSlot.compareTo(entry.getEndTime()) < 0) {
                return entry;
            }
        }
        return null;
    }

    private void showAddClassDialog() {
        showAddClassDialog("", "");
    }

    private void showAddClassDialog(String day, String time) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Class");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_class_multi_day, null);
        builder.setView(dialogView);

        EditText classNameInput = dialogView.findViewById(R.id.classNameInput);
        Spinner typeSpinner = dialogView.findViewById(R.id.typeSpinner);
        EditText locationInput = dialogView.findViewById(R.id.locationInput);
        EditText startTimeInput = dialogView.findViewById(R.id.startTimeInput);
        EditText endTimeInput = dialogView.findViewById(R.id.endTimeInput);

        CheckBox mondayCheck = dialogView.findViewById(R.id.mondayCheck);
        CheckBox tuesdayCheck = dialogView.findViewById(R.id.tuesdayCheck);
        CheckBox wednesdayCheck = dialogView.findViewById(R.id.wednesdayCheck);
        CheckBox thursdayCheck = dialogView.findViewById(R.id.thursdayCheck);
        CheckBox fridayCheck = dialogView.findViewById(R.id.fridayCheck);
        CheckBox saturdayCheck = dialogView.findViewById(R.id.saturdayCheck);

        // Set up type spinner
        ArrayList<String> allTypes = dbHelper.getCustomTaskTypes(userId);
        allTypes.add(0, "None");
        allTypes.add(1, "Mobile Dev");
        allTypes.add(2, "Web Dev");
        allTypes.add(3, "Design");

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, allTypes);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        // Pre-select if coming from a specific day click
        if (!day.isEmpty()) {
            switch (day) {
                case "Monday": mondayCheck.setChecked(true); break;
                case "Tuesday": tuesdayCheck.setChecked(true); break;
                case "Wednesday": wednesdayCheck.setChecked(true); break;
                case "Thursday": thursdayCheck.setChecked(true); break;
                case "Friday": fridayCheck.setChecked(true); break;
                case "Saturday": saturdayCheck.setChecked(true); break;
            }
        }

        if (!time.isEmpty()) startTimeInput.setText(time);

        startTimeInput.setOnClickListener(v -> showTimePicker(startTimeInput));
        endTimeInput.setOnClickListener(v -> showTimePicker(endTimeInput));

        builder.setPositiveButton("Add", (dialog, which) -> {
            String className = classNameInput.getText().toString().trim();
            String type = typeSpinner.getSelectedItem().toString();
            String location = locationInput.getText().toString().trim();
            String startTime = startTimeInput.getText().toString().trim();
            String endTime = endTimeInput.getText().toString().trim();

            if (className.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
                Toast.makeText(ScheduleActivity.this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get selected days
            List<String> selectedDays = new ArrayList<>();
            if (mondayCheck.isChecked()) selectedDays.add("Monday");
            if (tuesdayCheck.isChecked()) selectedDays.add("Tuesday");
            if (wednesdayCheck.isChecked()) selectedDays.add("Wednesday");
            if (thursdayCheck.isChecked()) selectedDays.add("Thursday");
            if (fridayCheck.isChecked()) selectedDays.add("Friday");
            if (saturdayCheck.isChecked()) selectedDays.add("Saturday");

            if (selectedDays.isEmpty()) {
                Toast.makeText(ScheduleActivity.this, "Please select at least one day", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add entry for each selected day
            for (String selectedDay : selectedDays) {
                dbHelper.addTimetableEntry(className, type, selectedDay, startTime, endTime, location, userId);
            }

            populateTimetable(userId);
            Toast.makeText(ScheduleActivity.this, "Class added", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void showClassOptionsDialog(String day, String time, TimetableEntry entry) {
        String[] options = {"View Tasks", "Edit", "Delete", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Class Options");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    viewTasksForClass(entry);
                    break;
                case 1:
                    editClass(day, time, entry);
                    break;
                case 2:
                    deleteClass(day, time);
                    break;
                case 3:
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    private void viewTasksForClass(TimetableEntry entry) {
        Intent intent = new Intent(this, ToDoActivity.class);
        intent.putExtra("filterType", entry.getType());
        startActivity(intent);
    }

    private void editClass(String day, String time, TimetableEntry entry) {
        List<TimetableEntry> entries = dbHelper.getTimetableEntries(userId);
        List<TimetableEntry> relatedEntries = new ArrayList<>();

        // Find all entries with the same class name and time (different days)
        for (TimetableEntry e : entries) {
            if (e.getClassName().equals(entry.getClassName()) &&
                    e.getStartTime().equals(entry.getStartTime()) &&
                    e.getEndTime().equals(entry.getEndTime())) {
                relatedEntries.add(e);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Class");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_class_multi_day, null);
        builder.setView(dialogView);

        EditText classNameInput = dialogView.findViewById(R.id.classNameInput);
        Spinner typeSpinner = dialogView.findViewById(R.id.typeSpinner);
        EditText locationInput = dialogView.findViewById(R.id.locationInput);
        EditText startTimeInput = dialogView.findViewById(R.id.startTimeInput);
        EditText endTimeInput = dialogView.findViewById(R.id.endTimeInput);

        CheckBox mondayCheck = dialogView.findViewById(R.id.mondayCheck);
        CheckBox tuesdayCheck = dialogView.findViewById(R.id.tuesdayCheck);
        CheckBox wednesdayCheck = dialogView.findViewById(R.id.wednesdayCheck);
        CheckBox thursdayCheck = dialogView.findViewById(R.id.thursdayCheck);
        CheckBox fridayCheck = dialogView.findViewById(R.id.fridayCheck);
        CheckBox saturdayCheck = dialogView.findViewById(R.id.saturdayCheck);

        // Set current values
        classNameInput.setText(entry.getClassName());
        locationInput.setText(entry.getLocation());
        startTimeInput.setText(entry.getStartTime());
        endTimeInput.setText(entry.getEndTime());

        // Set up type spinner with current type selected
        ArrayList<String> allTypes = dbHelper.getCustomTaskTypes(userId);
        allTypes.add(0, "None");
        allTypes.add(1, "Mobile Dev");
        allTypes.add(2, "Web Dev");
        allTypes.add(3, "Design");

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, allTypes);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        // Set the current type
        int typePosition = typeAdapter.getPosition(entry.getType());
        typeSpinner.setSelection(Math.max(typePosition, 0));

        // Check the days that are currently selected
        for (TimetableEntry e : relatedEntries) {
            switch (e.getDay()) {
                case "Monday": mondayCheck.setChecked(true); break;
                case "Tuesday": tuesdayCheck.setChecked(true); break;
                case "Wednesday": wednesdayCheck.setChecked(true); break;
                case "Thursday": thursdayCheck.setChecked(true); break;
                case "Friday": fridayCheck.setChecked(true); break;
                case "Saturday": saturdayCheck.setChecked(true); break;
            }
        }

        startTimeInput.setOnClickListener(v -> showTimePicker(startTimeInput));
        endTimeInput.setOnClickListener(v -> showTimePicker(endTimeInput));

        builder.setPositiveButton("Save", (dialog, which) -> {
            String className = classNameInput.getText().toString().trim();
            String type = typeSpinner.getSelectedItem().toString();
            String location = locationInput.getText().toString().trim();
            String startTime = startTimeInput.getText().toString().trim();
            String endTime = endTimeInput.getText().toString().trim();

            if (className.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
                Toast.makeText(ScheduleActivity.this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get selected days
            List<String> selectedDays = new ArrayList<>();
            if (mondayCheck.isChecked()) selectedDays.add("Monday");
            if (tuesdayCheck.isChecked()) selectedDays.add("Tuesday");
            if (wednesdayCheck.isChecked()) selectedDays.add("Wednesday");
            if (thursdayCheck.isChecked()) selectedDays.add("Thursday");
            if (fridayCheck.isChecked()) selectedDays.add("Friday");
            if (saturdayCheck.isChecked()) selectedDays.add("Saturday");

            if (selectedDays.isEmpty()) {
                Toast.makeText(ScheduleActivity.this, "Please select at least one day", Toast.LENGTH_SHORT).show();
                return;
            }

            // First delete all related entries
            for (TimetableEntry e : relatedEntries) {
                dbHelper.deleteTimetableEntry(e.getId());
            }

            // Then add new entries for selected days
            for (String selectedDay : selectedDays) {
                dbHelper.addTimetableEntry(className, type, selectedDay, startTime, endTime, location, userId);
            }

            populateTimetable(userId);
            Toast.makeText(ScheduleActivity.this, "Class updated", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void deleteClass(String day, String time) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Class")
                .setMessage("Are you sure you want to delete this class?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    List<TimetableEntry> entries = dbHelper.getTimetableEntries(userId);
                    // Find all entries with the same class name and time (different days)
                    String className = "";
                    for (TimetableEntry entry : entries) {
                        if (entry.getDay().equalsIgnoreCase(day) && entry.getStartTime().equals(time)) {
                            className = entry.getClassName();
                            break;
                        }
                    }

                    if (!className.isEmpty()) {
                        for (TimetableEntry entry : entries) {
                            if (entry.getClassName().equals(className) && entry.getStartTime().equals(time)) {
                                dbHelper.deleteTimetableEntry(entry.getId());
                            }
                        }
                    }

                    populateTimetable(userId);
                    Toast.makeText(ScheduleActivity.this, "Class deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showTimePicker(EditText timeInput) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d %s",
                            hourOfDay > 12 ? hourOfDay - 12 : hourOfDay,
                            minute,
                            hourOfDay >= 12 ? "PM" : "AM");
                    timeInput.setText(time);
                },
                8, 0, false
        );
        timePickerDialog.show();
    }

    private String getDayName(int index) {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        return days[index];
    }

    private String findClassForTimeSlot(List<TimetableEntry> entries, String day, String timeSlot) {
        for (TimetableEntry entry : entries) {
            if (entry.getDay().equalsIgnoreCase(day) && entry.getStartTime().equals(timeSlot)) {
                return entry.getClassName() + "\n" + entry.getLocation();
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