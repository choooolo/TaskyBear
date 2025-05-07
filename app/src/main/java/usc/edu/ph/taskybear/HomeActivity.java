package usc.edu.ph.taskybear;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity {

    private TextView userNameTextView;
    private LinearLayout taskList;
    private ImageView todobtn, shelfbtn, profilebtn, schedbtn;
    private CalendarView calendarView;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize views
        calendarView = findViewById(R.id.calendarView);
        todobtn = findViewById(R.id.todobtn);
        shelfbtn = findViewById(R.id.shelfbtn);
        schedbtn = findViewById(R.id.schedbtn);
        profilebtn = findViewById(R.id.profilebtn);
        taskList = findViewById(R.id.taskList);
        userNameTextView = findViewById(R.id.userNameTextView);

        // Get username from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserDetails", MODE_PRIVATE);
        String userName = sharedPreferences.getString("username", "Jane Doe");
        userNameTextView.setText(userName);

        // Set up button click listeners
        todobtn.setOnClickListener(v -> startActivity(new Intent(this, ToDoActivity.class)));
        schedbtn.setOnClickListener(v -> startActivity(new Intent(this, ScheduleActivity.class)));
        shelfbtn.setOnClickListener(v -> startActivity(new Intent(this, ShelfActivity.class)));
        profilebtn.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        // Calendar date change listener
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                loadTasksForDate(sdf.format(selectedDate.getTime()));
            }
        });

        // Load initial tasks for current calendar date
        Calendar initialCalendar = Calendar.getInstance();
        initialCalendar.setTimeInMillis(calendarView.getDate());
        loadTasksForDate(new SimpleDateFormat("yyyy-MM-dd").format(initialCalendar.getTime()));

        // Show task summary counts
        showTaskSummaryCounts();

        // Request POST_NOTIFICATIONS permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        // Schedule task reminders when the user logs in
        scheduleTaskReminderWorker();
    }

    private void loadTasksForDate(String selectedDate) {
        // Get user ID from SharedPreferences
        int userId = getSharedPreferences("TaskyPrefs", MODE_PRIVATE).getInt("userId", -1);

        // Get tasks from database
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        List<Task> tasks = databaseHelper.getTasksForDateAndCategory(userId, selectedDate, "Progress");

        // Update UI /a/sdsadad
        taskList.removeAllViews();

        if (tasks.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("No tasks in progress for " + selectedDate);
            taskList.addView(tv);
        } else {
            for (Task task : tasks) {
                TextView taskView = new TextView(this);
                taskView.setText(task.getTitle());
                taskView.setTextSize(16);
                taskView.setPadding(20, 10, 20, 10);
                taskList.addView(taskView);
            }
        }
    }

    private void showTaskSummaryCounts() {
        int userId = getSharedPreferences("TaskyPrefs", MODE_PRIVATE).getInt("userId", -1);
        DatabaseHelper db = new DatabaseHelper(this);

        // Get task counts by category
        int progressCount = db.getTaskCountByCategory(userId, "Progress");
        int reviewCount = db.getTaskCountByCategory(userId, "Review");
        int holdCount = db.getTaskCountByCategory(userId, "On Hold");
        int completeCount = db.getTaskCountByCategory(userId, "Complete");

        // Set text views for task counts
        TextView progressCountText = findViewById(R.id.progressCount);
        TextView reviewCountText = findViewById(R.id.reviewCount);
        TextView holdCountText = findViewById(R.id.onholdCount);
        TextView completeCountText = findViewById(R.id.completedCount);

        progressCountText.setText(progressCount + " Tasks");
        reviewCountText.setText(reviewCount + " Tasks");
        holdCountText.setText(holdCount + " Tasks");
        completeCountText.setText(completeCount + " Tasks");

        // Set click listeners for category filters
        findViewById(R.id.progressCount).setOnClickListener(v -> openToDoWithCategory("Progress"));
        findViewById(R.id.reviewCount).setOnClickListener(v -> openToDoWithCategory("Review"));
        findViewById(R.id.onholdCount).setOnClickListener(v -> openToDoWithCategory("On Hold"));
        findViewById(R.id.completedCount).setOnClickListener(v -> openToDoWithCategory("Complete"));
    }

    private void openToDoWithCategory(String category) {
        Intent intent = new Intent(this, ToDoActivity.class);
        intent.putExtra("filterCategory", category);
        startActivity(intent);
    }

    private void scheduleTaskReminderWorker() {
        // Cancel any existing work to avoid duplicate notifications
        WorkManager.getInstance(this).cancelAllWorkByTag("TaskReminder");

        WorkRequest taskReminderWorkRequest = new PeriodicWorkRequest.Builder(
                TaskReminderWorker.class,
                15,
                TimeUnit.SECONDS
        ).addTag("TaskReminder").build();

        WorkManager.getInstance(this).enqueue(taskReminderWorkRequest);
    }
}