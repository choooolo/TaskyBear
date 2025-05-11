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
import android.widget.ProgressBar;
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
import android.net.Uri;
import java.io.File;
import com.bumptech.glide.Glide;

public class HomeActivity extends AppCompatActivity {

    private TextView userNameTextView;
    private LinearLayout taskList;
    private ImageView todobtn, shelfbtn, profilebtn, schedbtn, profilePic;
    private CalendarView calendarView;
    private DatabaseHelper dbHelper;
    private String currentUsername;

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
        profilePic = findViewById(R.id.profilePic);
        dbHelper = new DatabaseHelper(this);

        // Get username from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("TaskyPrefs", MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", "");

        // Load user profile data
        loadUserProfileData();

        // Set up button click listeners
        todobtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, ToDoActivity.class);
            intent.putExtra("USERNAME", currentUsername);
            startActivity(intent);
        });

        schedbtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScheduleActivity.class);
            intent.putExtra("USERNAME", currentUsername);
            startActivity(intent);
        });

        shelfbtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, ShelfActivity.class);
            intent.putExtra("USERNAME", currentUsername);
            startActivity(intent);
        });

        profilebtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("USERNAME", currentUsername);
            startActivity(intent);
        });

        // Calendar date change listener
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            loadTasksForDate(sdf.format(selectedDate.getTime()));
        });

        // Load initial tasks for current calendar date
        Calendar initialCalendar = Calendar.getInstance();
        initialCalendar.setTimeInMillis(calendarView.getDate());
        loadTasksForDate(new SimpleDateFormat("yyyy-MM-dd").format(initialCalendar.getTime()));

        // Show task summary counts
        showTaskSummaryCounts();

        // Show productivity bar
        showProductivityBar();

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

    private void loadUserProfileData() {
        int userId = dbHelper.getUserId(currentUsername);
        if (userId != -1) {
            UserProfile userProfile = dbHelper.getUserProfile(userId);
            if (userProfile != null) {
                // Update username
                userNameTextView.setText(userProfile.getUsername());

                // Update profile picture
                if (userProfile.getProfileImageUri() != null && !userProfile.getProfileImageUri().isEmpty()) {
                    try {
                        Uri imageUri = Uri.parse(userProfile.getProfileImageUri());
                        if (imageUri.getScheme().equals("file")) {
                            File imageFile = new File(imageUri.getPath());
                            if (!imageFile.exists()) {
                                return;
                            }
                        }
                        Glide.with(this)
                                .load(imageUri)
                                .placeholder(R.drawable.profile_placeholder)
                                .error(R.drawable.profile_placeholder)
                                .into(profilePic);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfileData();
        // Load initial tasks for current calendar date
        Calendar initialCalendar = Calendar.getInstance();
        initialCalendar.setTimeInMillis(calendarView.getDate());
        loadTasksForDate(new SimpleDateFormat("yyyy-MM-dd").format(initialCalendar.getTime()));
        // Show task summary counts
        showTaskSummaryCounts();
    }

    private void loadTasksForDate(String selectedDate) {
        int userId = getSharedPreferences("TaskyPrefs", MODE_PRIVATE).getInt("userId", -1);
        List<Task> tasks = dbHelper.getTasksForDateAndCategory(userId, selectedDate, "Progress");

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

                // Make task clickable
                taskView.setOnClickListener(v -> {
                    Intent intent = new Intent(HomeActivity.this, TaskDetails.class);
                    intent.putExtra("task", task); // Only works if Task implements Serializable
                    // assuming getId() exists
                    startActivity(intent);
                });

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
        progressCountText.setOnClickListener(v -> openToDoWithCategory("Progress"));
        reviewCountText.setOnClickListener(v -> openToDoWithCategory("Review"));
        holdCountText.setOnClickListener(v -> openToDoWithCategory("On Hold"));
        completeCountText.setOnClickListener(v -> openToDoWithCategory("Complete"));
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
                TimeUnit.MINUTES
        ).addTag("TaskReminder").build();

        WorkManager.getInstance(this).enqueue(taskReminderWorkRequest);
    }

    private void showProductivityBar() {
        int userId = getSharedPreferences("TaskyPrefs", MODE_PRIVATE).getInt("userId", -1);
        DatabaseHelper db = new DatabaseHelper(this);

        // Get the total, completed, and overdue task counts
        int totalTasks = db.getTaskCountByCategory(userId, "Progress") +
                db.getTaskCountByCategory(userId, "Review") +
                db.getTaskCountByCategory(userId, "On Hold") +
                db.getTaskCountByCategory(userId, "Complete");

        int completedTasks = db.getTaskCountByCategory(userId, "Complete");
        int overdueTasks = db.getOverdueTaskCount(userId);
        int pendingTasks = totalTasks - completedTasks - overdueTasks;

        // Calculate productivity percentage (excluding overdue tasks from the denominator)
        int effectiveTotalTasks = totalTasks - overdueTasks;
        int productivityPercentage = (effectiveTotalTasks == 0) ? 0 : (completedTasks * 100) / effectiveTotalTasks;

        // Set progress bar, task count, and message
        ProgressBar productivityBar = findViewById(R.id.productivityBar);
        TextView taskCountText = findViewById(R.id.taskCountText);
        TextView productivityMessage = findViewById(R.id.productivityMessage);

        productivityBar.setProgress(productivityPercentage);
        taskCountText.setText(completedTasks + "/" + totalTasks + " Tasks (" + overdueTasks + " Overdue)");

        if (productivityPercentage >= 80) {
            productivityMessage.setText("Excellent! You're on track! Keep it up! 💪");
        } else if (productivityPercentage >= 50) {
            productivityMessage.setText("Good job! You're making progress. Don't stop now! 😊");
        } else if (productivityPercentage > 0) {
            productivityMessage.setText("Keep going! Every small step counts. 🚶‍♂️");
        } else {
            productivityMessage.setText("Let's get started! You can do this! 🌱");
        }
    }


}
