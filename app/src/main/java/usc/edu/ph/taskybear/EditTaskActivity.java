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

        titleEditText = findViewById(R.id.editTitle);
        detailsEditText = findViewById(R.id.editDetails);
        dateEditText = findViewById(R.id.editDate);
        typeSpinner = findViewById(R.id.editType);
        saveButton = findViewById(R.id.saveButton);

        int taskId = getIntent().getIntExtra("TASK_ID", -1);
        if (taskId == -1 || userId == -1) {
            Toast.makeText(this, "Task or User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        task = dbHelper.getTaskById(taskId, userId);
        if (task == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        titleEditText.setText(task.getTitle());
        detailsEditText.setText(task.getDetails());
        dateEditText.setText(formatDateForDisplay(task.getDate()));

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.task_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        if (task.getType() != null) {
            int typePosition = typeAdapter.getPosition(task.getType());
            typeSpinner.setSelection(Math.max(typePosition, 0));
        }

        saveButton.setOnClickListener(v -> {
            String newTitle = titleEditText.getText().toString().trim();
            String newDetails = detailsEditText.getText().toString().trim();
            String newDate = dateEditText.getText().toString().trim();
            String newType = typeSpinner.getSelectedItem().toString();

            if (newTitle.isEmpty()) {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            task.setTitle(newTitle);
            task.setDetails(newDetails);
            task.setDate(formatDateForStorage(newDate));
            task.setType(newType);
            task.setCategory(null); // Optional

            boolean isUpdated = dbHelper.updateTask(task, userId);

            if (isUpdated) {
                Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK); // Tell TaskDetails to refresh
                finish();
            } else {
                Toast.makeText(this, "Failed to update task", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatDateForStorage(String displayDate) {
        try {
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy");
            SimpleDateFormat storageFormat = new SimpleDateFormat("yyyy-MM-dd");
            return storageFormat.format(displayFormat.parse(displayDate));
        } catch (Exception e) {
            e.printStackTrace();
            return displayDate;
        }
    }

    private String formatDateForDisplay(String storageDate) {
        try {
            SimpleDateFormat storageFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy");
            return displayFormat.format(storageFormat.parse(storageDate));
        } catch (Exception e) {
            e.printStackTrace();
            return storageDate;
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}
