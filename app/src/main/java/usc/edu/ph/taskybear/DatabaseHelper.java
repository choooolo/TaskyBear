package usc.edu.ph.taskybear;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TaskyBear.db";
    private static final int DATABASE_VERSION = 4; // New version number

    // User table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_PROFILE_IMAGE = "profile_image";

    // Task table
    public static final String TABLE_TASKS = "tasks";
    public static final String COLUMN_TASK_ID = "task_id";
    public static final String COLUMN_TASK_TITLE = "title";
    public static final String COLUMN_TASK_DETAILS = "details";
    public static final String COLUMN_TASK_DATE = "date";
    public static final String COLUMN_TASK_RESOURCE = "resource";
    public static final String COLUMN_TASK_CATEGORY = "category";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_TASK_TYPE = "type";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "Creating users table");
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USERNAME + " TEXT, "
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_EMAIL + " TEXT, "
                + COLUMN_PHONE + " TEXT, "
                + COLUMN_PROFILE_IMAGE + " TEXT)";
        db.execSQL(CREATE_USERS_TABLE);

        Log.d("DatabaseHelper", "Creating tasks table");
        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + "("
                + COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TASK_TITLE + " TEXT, "
                + COLUMN_TASK_DETAILS + " TEXT, "
                + COLUMN_TASK_DATE + " TEXT, "
                + COLUMN_TASK_RESOURCE + " TEXT, "
                + COLUMN_TASK_CATEGORY + " TEXT, "
                + COLUMN_TASK_TYPE + " TEXT, "
                + COLUMN_USER_ID + " INTEGER, "
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "))";
        db.execSQL(CREATE_TASKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For existing users, you need to alter the table to add the new column
        if (oldVersion < 4) {  // Assuming version 3 is when you added the 'type' column
            db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + COLUMN_TASK_TYPE + " TEXT DEFAULT 'None'");
        }
    }

    public boolean insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_EMAIL, "");
        values.put(COLUMN_PHONE, "");
        values.put(COLUMN_PROFILE_IMAGE, "");
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public boolean checkUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_USERNAME + "=?",
                new String[]{username},
                null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public boolean updateUserProfile(int userId, String username, String email, String phone, String profileImageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PHONE, phone);
        if (profileImageUri != null) {
            values.put(COLUMN_PROFILE_IMAGE, profileImageUri);
        }

        int rowsAffected = db.update(TABLE_USERS, values, COLUMN_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
        return rowsAffected > 0;
    }

    public boolean updateProfileImage(int userId, String imageUriString) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROFILE_IMAGE, imageUriString);

        int rowsAffected = db.update(TABLE_USERS, values, COLUMN_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
        return rowsAffected > 0;
    }

    public UserProfile getUserProfile(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        UserProfile userProfile = null;

        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID, COLUMN_USERNAME, COLUMN_EMAIL, COLUMN_PHONE, COLUMN_PASSWORD, COLUMN_PROFILE_IMAGE},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            userProfile = new UserProfile();
            userProfile.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            userProfile.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)));
            userProfile.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
            userProfile.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)));
            userProfile.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)));
            userProfile.setProfileImageUri(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_IMAGE)));
            cursor.close();
        }
        db.close();
        return userProfile;
    }

    public boolean updatePassword(int userId, String currentPassword, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean result = false;

        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_PASSWORD},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String storedPassword = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
            if (storedPassword.equals(currentPassword)) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_PASSWORD, newPassword);
                int rowsAffected = db.update(TABLE_USERS, values, COLUMN_ID + "=?", new String[]{String.valueOf(userId)});
                result = rowsAffected > 0;
            }
            cursor.close();
        }
        db.close();
        return result;
    }

    public void insertTask(Task task, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_TITLE, task.getTitle());
        values.put(COLUMN_TASK_DETAILS, task.getDetails());
        values.put(COLUMN_TASK_DATE, task.getDate());
        values.put(COLUMN_TASK_RESOURCE, task.getResource());
        values.put(COLUMN_TASK_CATEGORY, task.getCategory());
        values.put(COLUMN_TASK_TYPE, task.getType());
        values.put(COLUMN_USER_ID, userId);
        db.insert(TABLE_TASKS, null, values);
        db.close();
    }

    public List<Task> getTasksForUser(int userId) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS,
                new String[]{COLUMN_TASK_ID, COLUMN_TASK_TITLE, COLUMN_TASK_DETAILS, COLUMN_TASK_DATE,
                        COLUMN_TASK_RESOURCE, COLUMN_TASK_CATEGORY, COLUMN_TASK_TYPE},
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Task task = new Task(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DETAILS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_RESOURCE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_CATEGORY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TYPE))
                );
                // Set the ID
                task.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_ID)));
                tasks.add(task);
            }
            cursor.close();
        }
        db.close();
        return tasks;
    }

    public boolean deleteTask(String title, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_TASKS,
                COLUMN_TASK_TITLE + "=? AND " + COLUMN_USER_ID + "=?",
                new String[]{title, String.valueOf(userId)});
        db.close();
        return result > 0;
    }

    // In DatabaseHelper.java
    public boolean updateTask(Task task, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_TITLE, task.getTitle());
        values.put(COLUMN_TASK_DETAILS, task.getDetails());
        values.put(COLUMN_TASK_DATE, task.getDate());
        values.put(COLUMN_TASK_RESOURCE, task.getResource());
        values.put(COLUMN_TASK_CATEGORY, task.getCategory());
        values.put(COLUMN_TASK_TYPE, task.getType());

        int rows = db.update(TABLE_TASKS,
                values,
                COLUMN_TASK_ID + "=? AND " + COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(task.getId()), String.valueOf(userId)});
        db.close();
        return rows > 0;
    }

    // Add this method to get a task by ID
    public Task getTaskById(int taskId, int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS,
                new String[]{COLUMN_TASK_ID, COLUMN_TASK_TITLE, COLUMN_TASK_DETAILS,
                        COLUMN_TASK_DATE, COLUMN_TASK_RESOURCE, COLUMN_TASK_CATEGORY, COLUMN_TASK_TYPE},
                COLUMN_TASK_ID + "=? AND " + COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(taskId), String.valueOf(userId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Task task = new Task(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DETAILS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_RESOURCE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_CATEGORY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TYPE))
            );
            task.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_ID)));
            cursor.close();
            return task;
        }
        return null;
    }

    public boolean updateTaskCategory(String taskTitle, String newCategory, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_CATEGORY, newCategory);

        int rows = db.update(TABLE_TASKS,
                values,
                COLUMN_TASK_TITLE + "=? AND " + COLUMN_USER_ID + "=?",
                new String[]{taskTitle, String.valueOf(userId)});
        db.close();
        return rows > 0;
    }

    public ArrayList<Task> getTasksForUserByCategory(int userId, String category) {
        ArrayList<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_TASKS,
                new String[]{COLUMN_TASK_TITLE, COLUMN_TASK_DETAILS, COLUMN_TASK_DATE, COLUMN_TASK_RESOURCE, COLUMN_TASK_CATEGORY, COLUMN_TASK_TYPE},
                COLUMN_USER_ID + " = ? AND " + COLUMN_TASK_CATEGORY + " = ?",
                new String[]{String.valueOf(userId), category},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Task task = new Task(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DETAILS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_RESOURCE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_CATEGORY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TYPE))
                );
                tasks.add(task);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return tasks;
    }

    public int getTaskCountByCategory(int userId, String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM tasks WHERE user_id = ? AND category = ?",
                new String[]{String.valueOf(userId), category});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public List<Task> getTasksForDate(int userId, String date) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM tasks WHERE user_id = ? AND " + COLUMN_TASK_DATE + " = ?",
                new String[]{String.valueOf(userId), date}
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Task task = new Task(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DETAILS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_RESOURCE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_CATEGORY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TYPE))
                );
                taskList.add(task);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return taskList;
    }

    public List<Task> getTasksForDateAndCategory(int userId, String date, String category) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM tasks WHERE user_id = ? AND " + COLUMN_TASK_DATE + " = ? AND " + COLUMN_TASK_CATEGORY + " = ?",
                new String[]{String.valueOf(userId), date, category}
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Task task = new Task(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DETAILS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_RESOURCE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_CATEGORY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TYPE))
                );
                taskList.add(task);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return taskList;
    }

    public ArrayList<Task> getTasksByCategory(int userId, String category) {
        ArrayList<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TASKS,
                new String[]{COLUMN_TASK_TITLE, COLUMN_TASK_DETAILS, COLUMN_TASK_DATE,
                        COLUMN_TASK_RESOURCE, COLUMN_TASK_CATEGORY, COLUMN_TASK_TYPE},
                COLUMN_USER_ID + "=? AND " + COLUMN_TASK_CATEGORY + "=?",
                new String[]{String.valueOf(userId), category},
                null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Task task = new Task(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DETAILS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_RESOURCE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_CATEGORY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TYPE))
                );
                tasks.add(task);
            }
            cursor.close();
        }
        db.close();
        return tasks;
    }

    public List<Task> getAllTasksForUser(int userId) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM tasks WHERE user_id = ?", new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DETAILS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_RESOURCE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_CATEGORY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TYPE))
                );
                taskList.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return taskList;
    }

    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE username = ?", new String[]{username});
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return userId;
    }

    public int getOverdueTaskCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        String query = "SELECT COUNT(*) FROM tasks WHERE user_id = ? AND date < ? AND category != 'Complete'";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), currentDate});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    public ArrayList<Task> getTasksByType(int userId, String type) {
        ArrayList<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TASKS,
                new String[]{COLUMN_TASK_TITLE, COLUMN_TASK_DETAILS, COLUMN_TASK_DATE,
                        COLUMN_TASK_RESOURCE, COLUMN_TASK_CATEGORY, COLUMN_TASK_TYPE},
                COLUMN_USER_ID + "=? AND " + COLUMN_TASK_TYPE + "=?",
                new String[]{String.valueOf(userId), type},
                null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Task task = new Task(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DETAILS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_RESOURCE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_CATEGORY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TYPE))
                );
                tasks.add(task);
            }
            cursor.close();
        }
        db.close();
        return tasks;
    }
}