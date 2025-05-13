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
    private static final int DATABASE_VERSION = 7; // New version number

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

    public static final String TABLE_TIMETABLE = "timetable";
    public static final String COLUMN_TIMETABLE_ID = "timetable_id";
    public static final String COLUMN_CLASS_NAME = "class_name";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_DAY = "day";
    public static final String COLUMN_START_TIME = "start_time";
    public static final String COLUMN_END_TIME = "end_time";
    public static final String COLUMN_LOCATION = "location";


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

        String CREATE_TIMETABLE_TABLE = "CREATE TABLE " + TABLE_TIMETABLE + "("
                + COLUMN_TIMETABLE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_CLASS_NAME + " TEXT, "
                + COLUMN_TYPE + " TEXT, "  // This will match task types
                + COLUMN_DAY + " TEXT, "
                + COLUMN_START_TIME + " TEXT, "
                + COLUMN_END_TIME + " TEXT, "
                + COLUMN_LOCATION + " TEXT, "
                + COLUMN_USER_ID + " INTEGER, "
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "))";
        db.execSQL(CREATE_TIMETABLE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 6) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIMETABLE);
            onCreate(db);
        }
    }
    // Add new timetable methods
    public long addTimetableEntry(String className, String type, String day,
                                  String startTime, String endTime, String location, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CLASS_NAME, className);
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_DAY, day);
        values.put(COLUMN_START_TIME, startTime);
        values.put(COLUMN_END_TIME, endTime);
        values.put(COLUMN_LOCATION, location);
        values.put(COLUMN_USER_ID, userId);

        return db.insert(TABLE_TIMETABLE, null, values);
    }

    public List<TimetableEntry> getTimetableEntries(int userId) {
        List<TimetableEntry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TIMETABLE,
                new String[]{COLUMN_TIMETABLE_ID, COLUMN_CLASS_NAME, COLUMN_TYPE,
                        COLUMN_DAY, COLUMN_START_TIME, COLUMN_END_TIME, COLUMN_LOCATION},
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, COLUMN_DAY + ", " + COLUMN_START_TIME);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                TimetableEntry entry = new TimetableEntry(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASS_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION))
                );
                entry.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TIMETABLE_ID)));
                entries.add(entry);
            }
            cursor.close();
        }
        db.close();
        return entries;
    }

    public List<TimetableEntry> getTimetableEntriesByDay(int userId, String day) {
        List<TimetableEntry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TIMETABLE,
                new String[]{COLUMN_TIMETABLE_ID, COLUMN_CLASS_NAME, COLUMN_TYPE,
                        COLUMN_DAY, COLUMN_START_TIME, COLUMN_END_TIME, COLUMN_LOCATION},
                COLUMN_USER_ID + "=? AND " + COLUMN_DAY + "=?",
                new String[]{String.valueOf(userId), day},
                null, null, COLUMN_START_TIME);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                TimetableEntry entry = new TimetableEntry(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASS_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION))
                );
                entry.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TIMETABLE_ID)));
                entries.add(entry);
            }
            cursor.close();
        }
        db.close();
        return entries;
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

    public long insertTask(Task task, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_TITLE, task.getTitle());
        values.put(COLUMN_TASK_DETAILS, task.getDetails());
        values.put(COLUMN_TASK_DATE, task.getDate());
        values.put(COLUMN_TASK_RESOURCE, task.getResource());
        values.put(COLUMN_TASK_CATEGORY, task.getCategory());
        values.put(COLUMN_TASK_TYPE, task.getType());
        values.put(COLUMN_USER_ID, userId);
        long taskId = db.insert(TABLE_TASKS, null, values);
        db.close();
        return taskId;
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

        String whereClause = COLUMN_TASK_ID + "=? AND " + COLUMN_USER_ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(task.getId()), String.valueOf(userId)};
        
        int rows = db.update(TABLE_TASKS, values, whereClause, whereArgs);
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
                new String[]{COLUMN_TASK_ID, COLUMN_TASK_TITLE, COLUMN_TASK_DETAILS, COLUMN_TASK_DATE,
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
                task.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_ID)));
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