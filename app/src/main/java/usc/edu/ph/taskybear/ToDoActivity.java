package usc.edu.ph.taskybear;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.widget.Spinner;

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
    private static final int EDIT_TASK_REQUEST = 1;
    private Handler refreshHandler;
    private static final int REFRESH_INTERVAL = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do);

        // Initialize refresh handler
        refreshHandler = new Handler(Looper.getMainLooper());
        
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

        if (getIntent().hasExtra("filterType")) {
            String type = getIntent().getStringExtra("filterType");
            filterTasksByType(type);
        } else {
            reloadTasks(userId);
        }

        reloadTasks(userId);
        updateButtonStates(currentCategory);
        openDialogButton.setOnClickListener(v -> showAddTaskDialog());

        // Navigation button listeners
        homebtn.setOnClickListener(v -> navigateTo(HomeActivity.class));
        shelfbtn.setOnClickListener(v -> navigateTo(ShelfActivity.class));
        profilebtn.setOnClickListener(v -> navigateTo(ProfileActivity.class));
        schedbtn.setOnClickListener(v -> navigateTo(ScheduleActivity.class));

        // Category filter buttons
        completeBtn.setOnClickListener(v -> setCategoryFilter("Complete"));
        reviewBtn.setOnClickListener(v -> setCategoryFilter("Review"));
        progressBtn.setOnClickListener(v -> setCategoryFilter("Progress"));
        onHoldBtn.setOnClickListener(v -> setCategoryFilter("On Hold"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAutoRefresh();
    }

    private void filterTasksByType(String type) {
        int userId = getSharedPreferences("TaskyPrefs", MODE_PRIVATE).getInt("userId", -1);
        ArrayList<Task> filteredTasks = dbHelper.getTasksByType(userId, type);

        taskList.clear();
        taskList.addAll(filteredTasks);
        adapter.notifyDataSetChanged();

        // Show a message about the current filter
        Toast.makeText(this, "Showing tasks for: " + type, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoRefresh();
    }

    private void startAutoRefresh() {
        refreshHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int userId = getSharedPreferences("TaskyPrefs", MODE_PRIVATE).getInt("userId", -1);
                reloadTasks(userId);
                refreshHandler.postDelayed(this, REFRESH_INTERVAL);
            }
        }, REFRESH_INTERVAL);
    }

    private void stopAutoRefresh() {
        refreshHandler.removeCallbacksAndMessages(null);
    }

    private void navigateTo(Class<?> cls) {
        Intent intent = new Intent(ToDoActivity.this, cls);
        intent.putExtra("USERNAME", getIntent().getStringExtra("USERNAME"));
        startActivity(intent);
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
        EditText taskDetails = dialogView.findViewById(R.id.taskDetails);
        Button dueDateButton = dialogView.findViewById(R.id.dueDateButton);
        Button backButton = dialogView.findViewById(R.id.backButton);
        Button doneButton = dialogView.findViewById(R.id.doneButton);

        Spinner typeSpinner = dialogView.findViewById(R.id.typeSpinner);

        final Calendar calendar = Calendar.getInstance();
        final String[] selectedDate = {""};


        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.task_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

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
            String type = typeSpinner.getSelectedItem().toString();
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

            int userId = getSharedPreferences("TaskyPrefs", MODE_PRIVATE).getInt("userId", -1);
            Task newTask = new Task(taskName, taskDetailText, formatDateForStorage(date), "None", "Progress", type);
            long taskId = dbHelper.insertTask(newTask, userId);
            newTask.setId((int) taskId);

            reloadTasks(userId);
            dialog.dismiss();
        });
    }

    private void showEditTaskDialog(Task task) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
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
        Button backButton = dialogView.findViewById(R.id.backButton);
        Button doneButton = dialogView.findViewById(R.id.doneButton);
        Spinner typeSpinner = dialogView.findViewById(R.id.typeSpinner);

        // Set initial values
        taskNameInput.setText(task.getTitle());
        taskDetailsInput.setText(task.getDetails());
        dueDateButton.setText(formatDateForDisplay(task.getDate()));

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
            String type = typeSpinner.getSelectedItem().toString();

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

            // Update task
            task.setTitle(taskName);
            task.setDetails(taskDetailText);
            task.setDate(formatDateForStorage(date));
            task.setType(type);

            int userId = getSharedPreferences("TaskyPrefs", MODE_PRIVATE).getInt("userId", -1);
            boolean success = dbHelper.updateTask(task, userId);
            if (success) {
                Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show();
                // Update the task in the list
                int position = taskList.indexOf(task);
                if (position != -1) {
                    taskList.set(position, task);
                    adapter.notifyItemChanged(position);
                }
                // Reload tasks to ensure everything is in sync
                reloadTasks(userId);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Failed to update task", Toast.LENGTH_SHORT).show();
            }
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

    private String formatDateForDisplay(String date) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy");
            return outputFormat.format(inputFormat.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void reloadTasks(int userId) {
        ArrayList<Task> tasks = dbHelper.getTasksByCategory(userId, currentCategory);
        taskList.clear();
        for (Task task : tasks) {
            // Ensure each task has its ID set
            if (task.getId() <= 0) {
                // If task doesn't have an ID, try to get it from the database
                Task dbTask = dbHelper.getTaskById(task.getId(), userId);
                if (dbTask != null) {
                    task.setId(dbTask.getId());
                }
            }
            taskList.add(task);
        }
        adapter.notifyDataSetChanged();
    }

    private void showTypeFilterPopupMenu() {
        PopupMenu popup = new PopupMenu(this, findViewById(R.id.menuIcon));
        popup.getMenuInflater().inflate(R.menu.type_filter_menu, popup.getMenu());

        // Get userId from SharedPreferences
        int userId = getSharedPreferences("TaskyPrefs", MODE_PRIVATE).getInt("userId", -1);
        popup.setOnMenuItemClickListener(item -> {
            String type = "All"; // Default
            if (item.getItemId() == R.id.filter_type_none) {
                type = "None";
            } else if (item.getItemId() == R.id.filter_type_mobile) {
                type = "Mobile Dev";
            } // ... other types

            ArrayList<Task> filteredTasks = dbHelper.getTasksByType(userId, type);
            taskList.clear();
            taskList.addAll(filteredTasks);
            adapter.notifyDataSetChanged();
            return true;
        });

        popup.show();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_TASK_REQUEST && resultCode == RESULT_OK && data != null) {
            // Get the updated task data
            int taskId = data.getIntExtra("TASK_ID", -1);
            String taskTitle = data.getStringExtra("TASK_TITLE");
            String taskDetails = data.getStringExtra("TASK_DETAILS");
            String taskDate = data.getStringExtra("TASK_DATE");
            String taskType = data.getStringExtra("TASK_TYPE");

            // Find and update the task in the list
            for (int i = 0; i < taskList.size(); i++) {
                Task task = taskList.get(i);
                if (task.getId() == taskId) {
                    task.setTitle(taskTitle);
                    task.setDetails(taskDetails);
                    task.setDate(taskDate);
                    task.setType(taskType);
                    adapter.notifyItemChanged(i);
                    break;
                }
            }
        }
    }
}