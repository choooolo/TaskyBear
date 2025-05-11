package usc.edu.ph.taskybear;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;

public class EditTaskActivity extends AppCompatActivity {

    private EditText titleEditText, detailsEditText, dateEditText;
    private Spinner typeSpinner;
    private Button saveButton;
    private Task task;
    private DatabaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        dbHelper = new DatabaseHelper(this);
        userId = getSharedPreferences("TaskyPrefs", MODE_PRIVATE).getInt("userId", -1);

        // Initialize UI elements
        titleEditText = findViewById(R.id.editTitle);
        detailsEditText = findViewById(R.id.editDetails);
        dateEditText = findViewById(R.id.editDate);
        typeSpinner = findViewById(R.id.editType);
        saveButton = findViewById(R.id.saveButton);

        // Get task ID from intent
        int taskId = getIntent().getIntExtra("TASK_ID", -1);
        if (taskId == -1 || userId == -1) {
            Toast.makeText(this, "Task or User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch the task from the database using taskId and userId
        task = dbHelper.getTaskById(taskId, userId);
        if (task == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Populate the fields with the current task data
        titleEditText.setText(task.getTitle());
        detailsEditText.setText(task.getDetails());
        dateEditText.setText(formatDateForDisplay(task.getDate()));

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.task_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        // Set the selected type in the spinner
        if (task.getType() != null) {
            int typePosition = typeAdapter.getPosition(task.getType());
            typeSpinner.setSelection(Math.max(typePosition, 0));
        }

        // Save button click listener to update task
        saveButton.setOnClickListener(v -> {
            String newTitle = titleEditText.getText().toString().trim();
            String newDetails = detailsEditText.getText().toString().trim();
            String newDate = dateEditText.getText().toString().trim();
            String newType = typeSpinner.getSelectedItem().toString();

            if (newTitle.isEmpty()) {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Set the task details with the new values
            task.setTitle(newTitle);
            task.setDetails(newDetails);
            task.setDate(formatDateForStorage(newDate));
            task.setType(newType);
            task.setCategory(null); // Clear category if removed

            // Update task in the database
            boolean isUpdated = dbHelper.updateTask(task, userId);

            // Show appropriate message based on the result
            if (isUpdated) {
                Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show();
                finish();  // Close the activity and go back to the previous screen
            } else {
                Toast.makeText(this, "Failed to update task", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Format the date for storage (in database format)
    private String formatDateForStorage(String displayDate) {
        try {
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy");
            SimpleDateFormat storageFormat = new SimpleDateFormat("yyyy-MM-dd");
            return storageFormat.format(displayFormat.parse(displayDate));
        } catch (Exception e) {
            e.printStackTrace();  // Log the error if date parsing fails
            return displayDate;
        }
    }

    // Format the date for display in the EditText field
    private String formatDateForDisplay(String storageDate) {
        try {
            SimpleDateFormat storageFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy");
            return displayFormat.format(storageFormat.parse(storageDate));
        } catch (Exception e) {
            e.printStackTrace();  // Log the error if date parsing fails
            return storageDate;
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}
