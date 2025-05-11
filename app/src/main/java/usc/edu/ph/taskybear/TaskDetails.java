package usc.edu.ph.taskybear;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

            Intent intent = new Intent(TaskDetails.this, EditTaskActivity.class);
            intent.putExtra("TASK_ID", task.getId()); // Pass task ID instead of Task object
            startActivity(intent);
        });

    }

}