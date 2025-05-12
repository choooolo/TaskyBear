package usc.edu.ph.taskybear;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.CalendarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TaskDetails extends AppCompatActivity {

    private Task task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        if (getIntent() == null || getIntent().getSerializableExtra("task") == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            task = (Task) getIntent().getSerializableExtra("task");
            if (task == null) {
                throw new Exception("Task is null");
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading task", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView title = findViewById(R.id.taskTitleDetail);
        TextView details = findViewById(R.id.taskDetailsDetail);
        TextView date = findViewById(R.id.taskDateDetail);
        Button backButton = findViewById(R.id.backButton);
        Button editButton = findViewById(R.id.editButton);
        TextView type = findViewById(R.id.taskTypeDetail);

        title.setText(task.getTitle());
        details.setText(task.getDetails());
        date.setText(task.getDate());
        type.setText(task.getType());

        backButton.setOnClickListener(v -> finish());

        editButton.setOnClickListener(v -> {
            if (task.getId() <= 0) {
                Toast.makeText(TaskDetails.this,
                        "Cannot edit task: Invalid task ID", Toast.LENGTH_SHORT).show();
                return;
            }

            // Use the same dialog approach as ToDoActivity
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(false)
                    .create();
            
            // Remove the dialog background
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.show();

            EditText taskNameInput = dialogView.findViewById(R.id.taskNameInput);
            EditText taskDetailsInput = dialogView.findViewById(R.id.taskDetails);
            Button dueDateButton = dialogView.findViewById(R.id.dueDateButton);
            Button dialogBackButton = dialogView.findViewById(R.id.backButton);
            Button doneButton = dialogView.findViewById(R.id.doneButton);
            Spinner typeSpinner = dialogView.findViewById(R.id.typeSpinner);

            // Set initial values
            taskNameInput.setText(task.getTitle());
            taskDetailsInput.setText(task.getDetails());
            dueDateButton.setText(formatDateForDisplay(task.getDate()) + " (Change Date)");
            doneButton.setText("Save");

            // Setup type spinner
            ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                    R.array.task_types, android.R.layout.simple_spinner_item);
            typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            typeSpinner.setAdapter(typeAdapter);
            int typePosition = typeAdapter.getPosition(task.getType());
            typeSpinner.setSelection(typePosition);

            final Calendar calendar = Calendar.getInstance();
            final String[] selectedDate = {task.getDate()};

            dueDateButton.setOnClickListener(view -> {
                DatePickerDialog datePicker = new DatePickerDialog(this,
                        (view1, year, month, dayOfMonth) -> {
                            selectedDate[0] = dayOfMonth + " " + getMonthName(month) + " " + year;
                            dueDateButton.setText(selectedDate[0]);
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                datePicker.show();
            });

            dialogBackButton.setOnClickListener(view -> dialog.dismiss());

            doneButton.setOnClickListener(view -> {
                String taskName = taskNameInput.getText().toString().trim();
                String taskDetailText = taskDetailsInput.getText().toString().trim();
                String selectedDateStr = selectedDate[0];
                String selectedType = typeSpinner.getSelectedItem().toString();

                // Validation checks
                if (taskName.isEmpty()) {
                    Toast.makeText(this, "Please enter a task title", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (taskDetailText.isEmpty()) {
                    Toast.makeText(this, "Please enter task details", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (selectedDateStr.isEmpty()) {
                    Toast.makeText(this, "Please select a due date", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update task
                task.setTitle(taskName);
                task.setDetails(taskDetailText);
                task.setDate(formatDateForStorage(selectedDateStr));
                task.setType(selectedType);

                DatabaseHelper dbHelper = new DatabaseHelper(this);
                int userId = getSharedPreferences("TaskyPrefs", MODE_PRIVATE).getInt("userId", -1);
                boolean success = dbHelper.updateTask(task, userId);
                if (success) {
                    Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show();
                    // Update the UI
                    title.setText(task.getTitle());
                    details.setText(task.getDetails());
                    date.setText(formatDateForDisplay(task.getDate()));
                    type.setText(task.getType());
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Failed to update task", Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    private String formatDateForStorage(String date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMM yyyy");
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
            return outputFormat.format(inputFormat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return date;
        }
    }

    private String formatDateForDisplay(String date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy");
            return outputFormat.format(inputFormat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return date;
        }
    }

    private String getMonthName(int month) {
        String[] months = new String[]{
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        };
        return months[month];
    }

}