package usc.edu.ph.taskybear;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskReminderWorker extends Worker {

    private static final String CHANNEL_ID = "task_reminder_channel";
    private static final int NOTIFICATION_ID = 101;

    public TaskReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Context context = getApplicationContext();
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            if (dbHelper == null) {
                System.out.println("DatabaseHelper is null. Worker failed.");
                return Result.failure();
            }

            int userId = context.getSharedPreferences("TaskyPrefs", Context.MODE_PRIVATE).getInt("userId", -1);
            if (userId == -1) {
                System.out.println("No valid user ID found. Worker failed.");
                return Result.failure();
            }

            // Check for notification permissions (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    System.out.println("Notification permission not granted. Worker failed.");
                    return Result.failure();
                }
            }

            // Counters
            int inProgressCount = 0;
            int inReviewCount = 0;
            int onHoldCount = 0;
            int completedCount = 0;
            int missedCount = 0;

            // Get today's date
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            // Get all tasks for the user
            List<Task> allTasks = dbHelper.getAllTasksForUser(userId);
            if (allTasks == null) {
                System.out.println("No tasks found for user ID: " + userId);
                return Result.failure();
            }

            for (Task task : allTasks) {
                String taskDate = task.getDate();
                String category = task.getCategory();

                // Count completed tasks separately
                if ("Complete".equalsIgnoreCase(category)) {
                    completedCount++;
                    continue;
                }

                // Count missed tasks
                if (taskDate.compareTo(today) < 0 && !"Complete".equalsIgnoreCase(category)) {
                    missedCount++;
                    continue;
                }

                // Count tasks based on category
                switch (category) {
                    case "Progress":
                        inProgressCount++;
                        break;
                    case "Review":
                        inReviewCount++;
                        break;
                    case "On Hold":
                        onHoldCount++;
                        break;
                }
            }

            // Build the notification content
            String notificationContent = "You have " + inProgressCount + " In Progress, " +
                    inReviewCount + " In Review, " +
                    onHoldCount + " On Hold, and " +
                    missedCount + " missed tasks.";

            // Create the notification channel (required for Android 8.0 and higher)
            createNotificationChannel(context);

            // Create an intent to open the app when the notification is clicked
            Intent intent = new Intent(context, HomeActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // Build the notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Task Reminder")
                    .setContentText(notificationContent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            // Show the notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NOTIFICATION_ID, builder.build());

            // Log the successful notification
            System.out.println("Notification sent successfully: " + notificationContent);

            return Result.success();


        } catch (Exception e) {
            // Log the exception for better debugging
            e.printStackTrace();
            return Result.failure();
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Reminder Channel";
            String description = "Channel for task reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
