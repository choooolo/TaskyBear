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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskReminderWorker extends Worker {

    private static final String CHANNEL_ID = "task_reminder_channel";
    private static final int DAILY_NOTIFICATION_ID = 101;
    private static final int WEEKLY_NOTIFICATION_ID = 102;

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
                try {
                    if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        System.out.println("Notification permission not granted. Worker failed.");
                        return Result.failure();
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();
                    return Result.failure();
                }
            }

            // Initialize date formatter
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date today = new Date();
            String todayString = sdf.format(today);

            // Get all tasks for the user
            List<Task> allTasks = dbHelper.getAllTasksForUser(userId);
            if (allTasks == null) {
                System.out.println("No tasks found for user ID: " + userId);
                return Result.failure();
            }

            // Daily task counters
            int todayTasks = 0;
            int todayCompleted = 0;

            // Weekly task counters
            int inProgressCount = 0;
            int inReviewCount = 0;
            int onHoldCount = 0;
            int completedCount = 0;
            int missedCount = 0;

            // Get the start and end dates of the current week
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            Date weekStart = calendar.getTime();
            calendar.add(Calendar.DAY_OF_WEEK, 6);
            Date weekEnd = calendar.getTime();

            // Count tasks
            for (Task task : allTasks) {
                try {
                    Date taskDate = sdf.parse(task.getDate());
                    String category = task.getCategory();

                    // Daily task check
                    if (taskDate != null && sdf.format(taskDate).equals(todayString)) {
                        todayTasks++;
                        if ("Complete".equalsIgnoreCase(category)) {
                            todayCompleted++;
                        }
                    }

                    // Weekly task check
                    if (taskDate == null || taskDate.before(weekStart) || taskDate.after(weekEnd)) {
                        continue;
                    }

                    if ("Complete".equalsIgnoreCase(category)) {
                        completedCount++;
                        continue;
                    }

                    if (taskDate.before(today) && !"Complete".equalsIgnoreCase(category)) {
                        missedCount++;
                        continue;
                    }

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

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            // Generate the daily notification message
            String todayMessage;
            if (todayTasks == todayCompleted && todayTasks > 0) {
                todayMessage = "🎉 Congrats! You have completed all " + todayTasks + " tasks today!";
            } else if (todayTasks > 0) {
                todayMessage = "📅 You have completed " + todayCompleted + " out of " + todayTasks + " tasks today.";
            } else {
                todayMessage = "📝 No tasks scheduled for today. Enjoy your free time!";
            }

            // Generate the weekly notification message
            String weeklyMessage = "📊 Weekly Task Summary:\n" +
                    "🔄 In Progress: " + inProgressCount + "\n" +
                    "🔎 In Review: " + inReviewCount + "\n" +
                    "🕒 On Hold: " + onHoldCount + "\n" +
                    "⚠️ Missed: " + missedCount + "\n" +
                    "✅ Completed: " + completedCount;

            // Create and show the daily notification
            createNotificationChannel(context);
            showNotification(context, "Daily Tasks", todayMessage, DAILY_NOTIFICATION_ID);

            // Create and show the weekly notification
            showNotification(context, "Weekly Tasks", weeklyMessage, WEEKLY_NOTIFICATION_ID);

            return Result.success();

        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

    private void showNotification(Context context, String title, String message, int notificationId) {
        Intent intent = new Intent(context, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            // Check for notification permissions (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    System.out.println("Notification permission not granted. Skipping notification.");
                    return;
                }
            }

            // Send the notification
            notificationManager.notify(notificationId, builder.build());

        } catch (SecurityException e) {
            e.printStackTrace();
            System.out.println("Failed to send notification due to missing permission.");
            return;
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
