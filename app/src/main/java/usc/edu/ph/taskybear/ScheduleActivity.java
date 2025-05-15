package usc.edu.ph.taskybear;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ScheduleActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DatabaseHelper dbHelper;
    private Button addClassButton;
    private int userId;
    private ScheduleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_recyclerview);

        recyclerView = findViewById(R.id.timetableRecyclerView);
        dbHelper = new DatabaseHelper(this);
        addClassButton = findViewById(R.id.addClassButton);

        userId = getSharedPreferences("TaskyPrefs", MODE_PRIVATE).getInt("userId", -1);
        setupRecyclerView();
        loadScheduleData();

        addClassButton.setOnClickListener(v -> showAddClassDialog());
    }

    private void setupRecyclerView() {
        // 7 columns (time + 6 days)
        GridLayoutManager layoutManager = new GridLayoutManager(this, 7);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ScheduleAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void loadScheduleData() {
        List<TimetableEntry> entries = dbHelper.getTimetableEntries(userId);
        adapter.setData(createScheduleGrid(entries));
    }

    private List<ScheduleCell> createScheduleGrid(List<TimetableEntry> entries) {
        List<ScheduleCell> cells = new ArrayList<>();

        // Define time slots (30-minute intervals)
        String[] timeSlots = {
                "07:00 AM", "07:30 AM", "08:00 AM", "08:30 AM", "09:00 AM", "09:30 AM",
                "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM", "12:00 PM", "12:30 PM",
                "01:00 PM", "01:30 PM", "02:00 PM", "02:30 PM", "03:00 PM", "03:30 PM",
                "04:00 PM", "04:30 PM", "05:00 PM", "05:30 PM", "06:00 PM", "06:30 PM",
                "07:00 PM", "07:30 PM", "08:00 PM", "08:30 PM", "09:00 PM"
        };

        // Add header row
        cells.add(new ScheduleCell("Time", true, -1, -1, null));
        cells.add(new ScheduleCell("Monday", true, -1, -1, null));
        cells.add(new ScheduleCell("Tuesday", true, -1, -1, null));
        cells.add(new ScheduleCell("Wednesday", true, -1, -1, null));
        cells.add(new ScheduleCell("Thursday", true, -1, -1, null));
        cells.add(new ScheduleCell("Friday", true, -1, -1, null));
        cells.add(new ScheduleCell("Saturday", true, -1, -1, null));

        for (int timeIndex = 0; timeIndex < timeSlots.length; timeIndex++) {
            // Add time column cell
            cells.add(new ScheduleCell(timeSlots[timeIndex], false, -1, -1, null));

            for (int dayIndex = 0; dayIndex < 6; dayIndex++) {
                String dayName = getDayName(dayIndex);
                String currentTime = timeSlots[timeIndex];

                // Find if this cell is part of any class
                TimetableEntry entry = findEntryForTimeSlot(entries, dayName, currentTime);

                if (entry != null) {
                    // Check if this is the FIRST cell of the class
                    boolean isFirstCell = entry.getStartTime().equals(currentTime);

                    // Text to display
                    String cellText = isFirstCell
                            ? entry.getClassName() + "\n" + entry.getLocation()
                            : ""; // Continuation marker

                    // Calculate color based on class name
                    int color = getColorForCourse(entry.getClassName());

                    cells.add(new ScheduleCell(
                            cellText,
                            false,
                            1, // Each cell has span=1
                            color,
                            isFirstCell ? entry : null // Only store entry in first cell
                    ));
                } else {
                    // Empty cell
                    cells.add(new ScheduleCell("", false, -1, -1, null));
                }
            }
        }
        return cells;
    }

    private String getDayName(int index) {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        return days[index];
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

    private int getColorForCourse(String courseName) {
        int hash = courseName.hashCode();
        return Color.HSVToColor(new float[]{
                Math.abs(hash % 360),
                0.7f,
                0.6f
        });
    }

    private void populateTimetable(int userId) {
        loadScheduleData();
    }

    // Rest of your methods (showAddClassDialog, showClassOptionsDialog, etc.) remain the same
    // Only change the references from tableLayout to adapter and call loadScheduleData() after modifications
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


    private class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {
        private List<ScheduleCell> cells = new ArrayList<>();

        public void setData(List<ScheduleCell> cells) {
            this.cells = cells;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_schedule_cell, parent, false);
            return new ScheduleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
            ScheduleCell cell = cells.get(position);
            holder.bind(cell);
        }

        @Override
        public int getItemCount() {
            return cells.size();
        }

        class ScheduleViewHolder extends RecyclerView.ViewHolder {
            TextView cellText;

            public ScheduleViewHolder(@NonNull View itemView) {
                super(itemView);
                cellText = itemView.findViewById(R.id.cellText);

                // Set fixed dimensions for cells
                int cellWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.28f);
                int cellHeight = (int) (getResources().getDisplayMetrics().heightPixels * 0.08f);
                itemView.setLayoutParams(new ViewGroup.LayoutParams(cellWidth, cellHeight));
            }

            public void bind(ScheduleCell cell) {
                cellText.setText(cell.text);
                cellText.setPadding(4, 4, 4, 4);

                if (cell.isHeader) {
                    cellText.setBackgroundColor(getResources().getColor(R.color.table_header));
                    cellText.setTextColor(Color.BLACK);
                } else if (cell.color != -1) {
                    cellText.setBackgroundColor(cell.color);
                    cellText.setTextColor(Color.WHITE);

                    // Add click listener only for cells with entries
                    if (cell.entry != null) {
                        itemView.setOnClickListener(v -> {
                            showClassOptionsDialog(cell.entry.getDay(),
                                    cell.entry.getStartTime(),
                                    cell.entry);
                        });
                    }
                } else {
                    cellText.setBackgroundResource(R.drawable.cell_border);
                    cellText.setTextColor(Color.BLACK);
                }
            }
        }
    }

    private static class ScheduleCell {
        String text;
        boolean isHeader;
        int span;
        int color;
        TimetableEntry entry;

        public ScheduleCell(String text, boolean isHeader, int span, int color, TimetableEntry entry) {
            this.text = text;
            this.isHeader = isHeader;
            this.span = span;
            this.color = color;
            this.entry = entry;
        }
    }
}