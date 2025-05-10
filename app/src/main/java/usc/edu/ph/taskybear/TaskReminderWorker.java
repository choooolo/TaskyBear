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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    System.out.println("Notification permission not granted. Worker failed.");
                    return Result.failure();
                }
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date today = new Date();

            Calendar todayCal = Calendar.getInstance();
            todayCal.setTime(today);
            todayCal.set(Calendar.HOUR_OF_DAY, 0);
            todayCal.set(Calendar.MINUTE, 0);
            todayCal.set(Calendar.SECOND, 0);
            todayCal.set(Calendar.MILLISECOND, 0);
            today = todayCal.getTime();
            String todayString = sdf.format(today);

            Calendar weekStartCal = Calendar.getInstance();
            weekStartCal.setTime(today);
            weekStartCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            weekStartCal.set(Calendar.HOUR_OF_DAY, 0);
            weekStartCal.set(Calendar.MINUTE, 0);
            weekStartCal.set(Calendar.SECOND, 0);
            weekStartCal.set(Calendar.MILLISECOND, 0);
            Date weekStart = weekStartCal.getTime();

            Calendar weekEndCal = (Calendar) weekStartCal.clone();
            weekEndCal.add(Calendar.DAY_OF_MONTH, 6);
            weekEndCal.set(Calendar.HOUR_OF_DAY, 23);
            weekEndCal.set(Calendar.MINUTE, 59);
            weekEndCal.set(Calendar.SECOND, 59);
            weekEndCal.set(Calendar.MILLISECOND, 999);

            // 🔧 FIX: Extend week end by 1 day if today is Saturday
            if (todayCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                weekEndCal.add(Calendar.DAY_OF_MONTH, 1);
            }

            Date weekEnd = weekEndCal.getTime();

            List<Task> allTasks = dbHelper.getAllTasksForUser(userId);
            if (allTasks == null) {
                System.out.println("No tasks found for user ID: " + userId);
                return Result.failure();
            }

            int todayTasks = 0;
            int todayCompleted = 0;

            int inProgressCount = 0;
            int inReviewCount = 0;
            int onHoldCount = 0;
            int completedCount = 0;
            int missedCount = 0;

            for (Task task : allTasks) {
                try {
                    Date taskDate = sdf.parse(task.getDate());
                    String category = task.getCategory();

                    if (taskDate == null) continue;

                    if (sdf.format(taskDate).equals(todayString)) {
                        todayTasks++;
                        if ("Complete".equalsIgnoreCase(category)) {
                            todayCompleted++;
                        }
                    }

                    if (taskDate.before(weekStart) || taskDate.after(weekEnd)) {
                        continue;
                    }

                    // Always count by category
                    switch (category) {
                        case "Complete":
                            completedCount++;
                            break;
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

                    // Separately check if overdue and incomplete
                    if (!"Complete".equalsIgnoreCase(category) && taskDate.before(today)) {
                        missedCount++;
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            String todayMessage;
            if (todayTasks == todayCompleted && todayTasks > 0) {
                todayMessage = "🎉 Congrats! You have completed all " + todayTasks + " tasks today!";
            } else if (todayTasks > 0) {
                todayMessage = "📅 You have completed " + todayCompleted + " out of " + todayTasks + " tasks today.";
            } else {
                todayMessage = "📝 No tasks scheduled for today. Enjoy your free time!";
            }

            String weeklyMessage = "📊 Weekly Task Summary:\n" +
                    "🔄 In Progress: " + inProgressCount + "\n" +
                    "🔎 In Review: " + inReviewCount + "\n" +
                    "🕒 On Hold: " + onHoldCount + "\n" +
                    "⚠️ Missed: " + missedCount + "\n" +
                    "✅ Completed: " + completedCount;

            createNotificationChannel(context);
            showNotification(context, "Daily Tasks", todayMessage, DAILY_NOTIFICATION_ID);
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    System.out.println("Notification permission not granted. Skipping notification.");
                    return;
                }
            }
            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
            System.out.println("Failed to send notification due to missing permission.");
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
