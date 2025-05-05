package usc.edu.ph.taskybear;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ToDoActivity extends AppCompatActivity {
    private ImageButton openDialogButton;
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private ArrayList<Task> taskList;
    private ImageView homebtn, shelfbtn, profilebtn, schedbtn;
    private Button completeBtn, reviewBtn, progressBtn, onHoldBtn;
    private DatabaseHelper dbHelper;
    private String currentCategory = "Progress";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do);

        // Initialize views and buttons
        completeBtn = findViewById(R.id.completetodolist);
        reviewBtn = findViewById(R.id.reviewtodolist);
        progressBtn = findViewById(R.id.progresstodolist);
        onHoldBtn = findViewById(R.id.onholdtodolist);
        homebtn = findViewById(R.id.homebtn);
        shelfbtn = findViewById(R.id.shelfbtn);
        profilebtn = findViewById(R.id.profilebtn);
        schedbtn = findViewById(R.id.schedbtn);
        openDialogButton = findViewById(R.id.addTaskButton);
        recyclerView = findViewById(R.id.taskRecyclerView);

        dbHelper = new DatabaseHelper(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();
        adapter = new TaskAdapter(this, taskList, new TaskAdapter.TaskActionListener() {
            @Override
            public void onEdit(Task task) {
                showEditTaskDialog(task);
            }

            @Override
            public void onDelete(Task task) {
                int userId = getSharedPreferences("TaskyPrefs", MODE_PRIVATE).getInt("userId", -1);
                dbHelper.deleteTask(task.getTitle(), userId);
                reloadTasks(userId);
            }

            @Override
            public void onCategoryChange(Task task, String newCategory) {
                task.setCategory(newCategory);
                int userId = getSharedPreferences("TaskyPrefs", MODE_PRIVATE).getInt("userId", -1);
                dbHelper.updateTaskCategory(task.getTitle(), newCategory, userId);
                adapter.notifyDataSetChanged();
            }
        });
        recyclerView.setAdapter(adapter);

        int userId = getSharedPreferences("TaskyPrefs", MODE_PRIVATE).getInt("userId", -1);

        if (getIntent().hasExtra("filterCategory")) {
            currentCategory = getIntent().getStringExtra("filterCategory");
        }

        reloadTasks(userId);
        updateButtonStates(currentCategory);
        openDialogButton.setOnClickListener(v -> showAddTaskDialog());

        homebtn.setOnClickListener(v -> startActivity(new Intent(ToDoActivity.this, HomeActivity.class)));
        shelfbtn.setOnClickListener(v -> startActivity(new Intent(ToDoActivity.this, ShelfActivity.class)));
        profilebtn.setOnClickListener(v -> startActivity(new Intent(ToDoActivity.this, ProfileActivity.class)));
        schedbtn.setOnClickListener(v -> startActivity(new Intent(ToDoActivity.this, ScheduleActivity.class)));

        completeBtn.setOnClickListener(v -> setCategoryFilter("Complete"));
        reviewBtn.setOnClickListener(v -> setCategoryFilter("Review"));
        progressBtn.setOnClickListener(v -> setCategoryFilter("Progress"));
        onHoldBtn.setOnClickListener(v -> setCategoryFilter("On Hold"));
    }

    private void setCategoryFilter(String category) {
        currentCategory = category;
        int userId = getSharedPreferences("TaskyPrefs", MODE_PRIVATE).getInt("userId", -1);
        reloadTasks(userId);
        updateButtonStates(category);
    }

    private void updateButtonStates(String selectedCategory) {
        resetButtonStyles();

        switch(selectedCategory) {
            case "Complete":
                completeBtn.setBackgroundColor(getResources().getColor(R.color.active_category_bg));
                completeBtn.setTextColor(getResources().getColor(R.color.active_category_text));
                break;
            case "Review":
                reviewBtn.setBackgroundColor(getResources().getColor(R.color.active_category_bg));
                reviewBtn.setTextColor(getResources().getColor(R.color.active_category_text));
                break;
            case "Progress":
                progressBtn.setBackgroundColor(getResources().getColor(R.color.active_category_bg));
                progressBtn.setTextColor(getResources().getColor(R.color.active_category_text));
                break;
            case "On Hold":
                onHoldBtn.setBackgroundColor(getResources().getColor(R.color.active_category_bg));
                onHoldBtn.setTextColor(getResources().getColor(R.color.active_category_text));
                break;
        }
    }

    private void resetButtonStyles() {
        int defaultBg = getResources().getColor(R.color.default_category_bg);
        int defaultText = getResources().getColor(R.color.default_category_text);

        completeBtn.setBackgroundColor(defaultBg);
        completeBtn.setTextColor(defaultText);
        reviewBtn.setBackgroundColor(defaultBg);
        reviewBtn.setTextColor(defaultText);
        progressBtn.setBackgroundColor(defaultBg);
        progressBtn.setTextColor(defaultText);
        onHoldBtn.setBackgroundColor(defaultBg);
        onHoldBtn.setTextColor(defaultText);
    }

    private void showAddTaskDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();
        dialog.show();

        EditText taskNameInput = dialogView.findViewById(R.id.taskNameInput);
        Spinner resourceSpinner = dialogView.findViewById(R.id.resourceSpinner);
        EditText taskDetails = dialogView.findViewById(R.id.taskDetails);
        Button dueDateButton = dialogView.findViewById(R.id.dueDateButton);
        Button backButton = dialogView.findViewById(R.id.backButton);
        Button doneButton = dialogView.findViewById(R.id.doneButton);

        ArrayList<String> resources = new ArrayList<>();
        resources.add("None");
        resources.add("Resource 1");
        resources.add("Resource 2");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, resources);
        resourceSpinner.setAdapter(adapter);

        final Calendar calendar = Calendar.getInstance();
        final String[] selectedDate = {""};
        dueDateButton.setOnClickListener(view -> {
            DatePickerDialog datePicker = new DatePickerDialog(ToDoActivity.this,
                    (view1, year, month, dayOfMonth) -> {
                        selectedDate[0] = dayOfMonth + " " + getMonthName(month) + " " + year;
                        dueDateButton.setText(selectedDate[0]);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        backButton.setOnClickListener(view -> dialog.dismiss());

        doneButton.setOnClickListener(view -> {
            String taskName = taskNameInput.getText().toString().trim();
            String taskDetailText = taskDetails.getText().toString().trim();
            String date = selectedDate[0];

            // Validation checks
            if (taskName.isEmpty()) {
                Toast.makeText(this, "Please enter a task title", Toast.LENGTH_SHORT).show();
                return;
            }
            if (taskDetailText.isEmpty()) {
                Toast.makeText(this, "Please enter task details", Toast.LENGTH_SHORT).show();
                return;
            }
            if (date.isEmpty()) {
                Toast.makeText(this, "Please select a due date", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedResource = resourceSpinner.getSelectedItem().toString();
            int userId = getSharedPreferences("TaskyPrefs", MODE_PRIVATE).getInt("userId", -1);

            Task newTask = new Task(taskName, taskDetailText, formatDateForStorage(date), selectedResource, "Progress");
            dbHelper.insertTask(newTask, userId);

            reloadTasks(userId);
            dialog.dismiss();
        });
    }

    private void showEditTaskDialog(Task task) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).create();
        dialog.show();

        EditText taskNameInput = dialogView.findViewById(R.id.taskNameInput);
        Spinner resourceSpinner = dialogView.findViewById(R.id.resourceSpinner);
        EditText taskDetailsInput = dialogView.findViewById(R.id.taskDetails);
        Button dueDateButton = dialogView.findViewById(R.id.dueDateButton);
        Button backButton = dialogView.findViewById(R.id.backButton);
        Button doneButton = dialogView.findViewById(R.id.doneButton);

        taskNameInput.setText(task.getTitle());
        taskDetailsInput.setText(task.getDetails());
        dueDateButton.setText(task.getDate());

        ArrayList<String> resources = new ArrayList<>();
        resources.add("None");
        resources.add("Resource 1");
        resources.add("Resource 2");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, resources);
        resourceSpinner.setAdapter(adapter);

        int resourceIndex = resources.indexOf(task.getResource());
        if (resourceIndex >= 0) {
            resourceSpinner.setSelection(resourceIndex);
        }

        final Calendar calendar = Calendar.getInstance();
        final String[] selectedDate = {task.getDate()};

        dueDateButton.setOnClickListener(view -> {
            DatePickerDialog datePicker = new DatePickerDialog(ToDoActivity.this,
                    (view1, year, month, dayOfMonth) -> {
                        selectedDate[0] = dayOfMonth + " " + getMonthName(month) + " " + year;
                        dueDateButton.setText(selectedDate[0]);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        backButton.setOnClickListener(view -> dialog.dismiss());

        doneButton.setOnClickListener(view -> {
            String taskName = taskNameInput.getText().toString().trim();
            String taskDetailText = taskDetailsInput.getText().toString().trim();
            String date = selectedDate[0];

            // Validation checks
            if (taskName.isEmpty()) {
                Toast.makeText(this, "Please enter a task title", Toast.LENGTH_SHORT).show();
                return;
            }
            if (taskDetailText.isEmpty()) {
                Toast.makeText(this, "Please enter task details", Toast.LENGTH_SHORT).show();
                return;
            }
            if (date.isEmpty()) {
                Toast.makeText(this, "Please select a due date", Toast.LENGTH_SHORT).show();
                return;
            }

            task.setTitle(taskName);
            task.setDetails(taskDetailText);
            task.setDate(formatDateForStorage(date));
            task.setResource(resourceSpinner.getSelectedItem().toString());

            int userId = getSharedPreferences("TaskyPrefs", MODE_PRIVATE).getInt("userId", -1);
            dbHelper.updateTask(task, userId);

            reloadTasks(userId);
            dialog.dismiss();
        });
    }

    private String formatDateForStorage(String date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMM yyyy");
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
            return outputFormat.format(inputFormat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getMonthName(int month) {
        String[] months = new String[]{
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        };
        return months[month];
    }

    private void reloadTasks(int userId) {
        ArrayList<Task> tasks = dbHelper.getTasksByCategory(userId, currentCategory);
        taskList.clear();
        taskList.addAll(tasks);
        adapter.notifyDataSetChanged();
    }
}