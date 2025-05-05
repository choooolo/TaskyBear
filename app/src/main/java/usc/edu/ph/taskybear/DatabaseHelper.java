package usc.edu.ph.taskybear;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import usc.edu.ph.taskybear.Task;

import java.util.ArrayList;
import java.util.List;



public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TaskyBear.db";
    private static final int DATABASE_VERSION = 2;  // Incremented version to trigger onUpgrade()

    // User table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    // Task table
    private static final String TABLE_TASKS = "tasks";
    private static final String COLUMN_TASK_ID = "task_id";
    private static final String COLUMN_TASK_TITLE = "title";
    private static final String COLUMN_TASK_DETAILS = "details";
    private static final String COLUMN_TASK_DATE = "date";
    private static final String COLUMN_TASK_RESOURCE = "resource";
    private static final String COLUMN_TASK_CATEGORY = "category";
    private static final String COLUMN_USER_ID = "user_id"; // Foreign key

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "Creating users table");
        // Create users table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USERNAME + " TEXT, "
                + COLUMN_PASSWORD + " TEXT)";
        db.execSQL(CREATE_USERS_TABLE);

        Log.d("DatabaseHelper", "Creating tasks table");
        // Create tasks table
        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + "("
                + COLUMN_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TASK_TITLE + " TEXT, "
                + COLUMN_TASK_DETAILS + " TEXT, "
                + COLUMN_TASK_DATE + " TEXT, "
                + COLUMN_TASK_RESOURCE + " TEXT, "
                + COLUMN_TASK_CATEGORY + " TEXT, "
                + COLUMN_USER_ID + " INTEGER, "
                + "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "))";
        db.execSQL(CREATE_TASKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop tables if they exist
        Log.d("DatabaseHelper", "Upgrading database, dropping old tables");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        // Recreate tables
        onCreate(db);
    }

    // Insert new user
    public boolean insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    // Check if user exists with matching credentials
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

    // Check if username is already taken
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

    // Update password for a username
    public boolean updatePassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, newPassword);
        int rowsAffected = db.update(TABLE_USERS, values, COLUMN_USERNAME + "=?", new String[]{username});
        db.close();
        return rowsAffected > 0;
    }

    // Insert a task for a user
    // In DatabaseHelper.java
    public void insertTask(Task task, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("title", task.getTitle());
        contentValues.put("details", task.getDetails());
        contentValues.put("date", task.getDate());
        contentValues.put("resource", task.getResource());
        contentValues.put("category", task.getCategory());
        contentValues.put("user_id", userId); // Assuming you associate tasks with a user

        db.insert("tasks", null, contentValues);
        db.close();
    }



    // Get all tasks for a user
    public List<Task> getTasksForUser(int userId) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS,
                null,
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Task task = new Task(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DETAILS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_RESOURCE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_CATEGORY))
                );
                tasks.add(task);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return tasks;
    }

    // Delete a task by title and user
    public boolean deleteTask(String title, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_TASKS,
                COLUMN_TASK_TITLE + "=? AND " + COLUMN_USER_ID + "=?",
                new String[]{title, String.valueOf(userId)});
        db.close();
        return result > 0;
    }

    public boolean updateTask(Task task, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_DETAILS, task.getDetails());
        values.put(COLUMN_TASK_DATE, task.getDate());
        values.put(COLUMN_TASK_RESOURCE, task.getResource());
        values.put(COLUMN_TASK_CATEGORY, task.getCategory());
        values.put(COLUMN_TASK_TITLE, task.getTitle());

        int rows = db.update(TABLE_TASKS,
                values,
                COLUMN_TASK_TITLE + "=? AND " + COLUMN_USER_ID + "=?",
                new String[]{task.getTitle(), String.valueOf(userId)});
        db.close();
        return rows > 0;
    }


    // Update task category for a user
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

    // Get tasks for a user by category
    public ArrayList<Task> getTasksForUserByCategory(int userId, String category) {
        ArrayList<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_TASKS,
                new String[]{COLUMN_TASK_TITLE, COLUMN_TASK_DETAILS, COLUMN_TASK_DATE, COLUMN_TASK_RESOURCE, COLUMN_TASK_CATEGORY},
                COLUMN_USER_ID + " = ? AND " + COLUMN_TASK_CATEGORY + " = ?",
                new String[]{String.valueOf(userId), category},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            // Get column indices
            int titleIndex = cursor.getColumnIndex(COLUMN_TASK_TITLE);
            int detailsIndex = cursor.getColumnIndex(COLUMN_TASK_DETAILS);
            int dateIndex = cursor.getColumnIndex(COLUMN_TASK_DATE);
            int resourceIndex = cursor.getColumnIndex(COLUMN_TASK_RESOURCE);
            int categoryIndex = cursor.getColumnIndex(COLUMN_TASK_CATEGORY);

            // Validate that all indices are valid (>= 0)
            if (titleIndex >= 0 && detailsIndex >= 0 && dateIndex >= 0 && resourceIndex >= 0 && categoryIndex >= 0) {
                do {
                    String title = cursor.getString(titleIndex);
                    String details = cursor.getString(detailsIndex);
                    String date = cursor.getString(dateIndex);
                    String resource = cursor.getString(resourceIndex);
                    String taskCategory = cursor.getString(categoryIndex);

                    tasks.add(new Task(title, details, date, resource, taskCategory)); // Fix constructor call here
                } while (cursor.moveToNext());
            } else {
                Log.e("getTasksForUserByCategory", "Invalid column index");
            }
        }

        cursor.close();
        db.close();
        return tasks;
    }



    // Get task count by category for a user
    public int getTaskCountByCategory(int userId, String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM tasks WHERE user_id = ? AND category = ?",
                new String[]{String.valueOf(userId), category});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // Get tasks for a specific date
    public List<Task> getTasksForDate(int userId, String date) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to get tasks for a specific user on a specific date
        Cursor cursor = db.rawQuery(
                "SELECT * FROM tasks WHERE user_id = ? AND " + COLUMN_TASK_DATE + " = ?",
                new String[] { String.valueOf(userId), date }
        );

        // Check if cursor is not null and contains valid data
        if (cursor != null && cursor.moveToFirst()) {
            // Get column indices
            int titleIndex = cursor.getColumnIndex(COLUMN_TASK_TITLE);
            int detailsIndex = cursor.getColumnIndex(COLUMN_TASK_DETAILS);
            int dateIndex = cursor.getColumnIndex(COLUMN_TASK_DATE);
            int resourceIndex = cursor.getColumnIndex(COLUMN_TASK_RESOURCE);
            int categoryIndex = cursor.getColumnIndex(COLUMN_TASK_CATEGORY);

            // Validate that all indices are valid (>= 0)
            if (titleIndex >= 0 && detailsIndex >= 0 && dateIndex >= 0 && resourceIndex >= 0 && categoryIndex >= 0) {
                do {
                    Task task = new Task(
                            cursor.getString(titleIndex),
                            cursor.getString(detailsIndex),
                            cursor.getString(dateIndex),
                            cursor.getString(resourceIndex),
                            cursor.getString(categoryIndex)
                    );
                    taskList.add(task);
                } while (cursor.moveToNext());
            } else {
                Log.e("getTasksForDate", "Invalid column index");
            }
        }
        cursor.close();
        db.close();
        return taskList;
    }


    // Get tasks for a user on a specific date and category
    public List<Task> getTasksForDateAndCategory(int userId, String date, String category) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to get tasks for a specific user on a specific date and category
        Cursor cursor = db.rawQuery(
                "SELECT * FROM tasks WHERE user_id = ? AND " + COLUMN_TASK_DATE + " = ? AND " + COLUMN_TASK_CATEGORY + " = ?",
                new String[] { String.valueOf(userId), date, category }
        );

        // Check if cursor is not null and contains valid data
        if (cursor != null && cursor.moveToFirst()) {
            // Get column indices
            int titleIndex = cursor.getColumnIndex(COLUMN_TASK_TITLE);
            int detailsIndex = cursor.getColumnIndex(COLUMN_TASK_DETAILS);
            int dateIndex = cursor.getColumnIndex(COLUMN_TASK_DATE);
            int resourceIndex = cursor.getColumnIndex(COLUMN_TASK_RESOURCE);
            int categoryIndex = cursor.getColumnIndex(COLUMN_TASK_CATEGORY);

            // Validate that all indices are valid (>= 0)
            if (titleIndex >= 0 && detailsIndex >= 0 && dateIndex >= 0 && resourceIndex >= 0 && categoryIndex >= 0) {
                do {
                    Task task = new Task(
                            cursor.getString(titleIndex),
                            cursor.getString(detailsIndex),
                            cursor.getString(dateIndex),
                            cursor.getString(resourceIndex),
                            cursor.getString(categoryIndex)
                    );
                    taskList.add(task);
                } while (cursor.moveToNext());
            } else {
                Log.e("getTasksForDateAndCategory", "Invalid column index");
            }
        }
        cursor.close();
        db.close();
        return taskList;
    }
    public ArrayList<Task> getTasksByCategory(int userId, String category) {
        ArrayList<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to fetch tasks for a specific category
        String selection = "user_id = ? AND category = ?";
        String[] selectionArgs = {String.valueOf(userId), category};

        Cursor cursor = db.query(
                "tasks",          // Table name
                null,             // Select all columns
                selection,        // WHERE clause
                selectionArgs,    // WHERE clause arguments
                null,             // GROUP BY
                null,             // HAVING
                null              // ORDER BY
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                // Safely get column indices
                int titleIndex = cursor.getColumnIndex("title");
                int detailsIndex = cursor.getColumnIndex("details");
                int dateIndex = cursor.getColumnIndex("date");
                int resourceIndex = cursor.getColumnIndex("resource");

                // Check if the column indices are valid
                if (titleIndex != -1 && detailsIndex != -1 && dateIndex != -1 && resourceIndex != -1) {
                    String title = cursor.getString(titleIndex);
                    String details = cursor.getString(detailsIndex);
                    String date = cursor.getString(dateIndex);
                    String resource = cursor.getString(resourceIndex);

                    // Assuming Task has a constructor that takes these parameters
                    Task task = new Task(title, details, date, resource, category);
                    tasks.add(task);
                } else {
                    // Handle case where a column is missing (e.g., log an error or skip this row)
                    Log.e("DatabaseHelper", "One or more columns are missing from the cursor.");
                }
            }
            cursor.close();
        }
        db.close();
        return tasks;
    }

}
