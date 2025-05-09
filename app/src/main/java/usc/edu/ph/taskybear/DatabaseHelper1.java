package usc.edu.ph.taskybear;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.content.ContentResolver;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper1 extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "taskyBearDB";

    // Table name
    private static final String TABLE_USER = "user_profile";

    // User Table Columns
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_PROFILE_IMAGE = "profile_image";

    private Context mContext;

    // Create User Table query
    //
    private static final String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USERNAME + " TEXT,"
            + COLUMN_EMAIL + " TEXT,"
            + COLUMN_PHONE + " TEXT,"
            + COLUMN_PASSWORD + " TEXT,"
            + COLUMN_PROFILE_IMAGE + " TEXT"
            + ")";

    // Constructor
    public DatabaseHelper1(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        // Create tables again
        onCreate(db);
    }

    /**
     * Create a new user profile or update if exists
     */
    public long addOrUpdateUser(String username, String email, String phone, String password, String profileImageUri) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if user exists
        Cursor cursor = db.query(TABLE_USER, new String[]{COLUMN_ID}, null, null, null, null, null);
        boolean userExists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PHONE, phone);

        // Only update password if it's not null or empty
        if (password != null && !password.isEmpty()) {
            values.put(COLUMN_PASSWORD, password);
        }

        // Only update profile image if it's not null
        if (profileImageUri != null) {
            values.put(COLUMN_PROFILE_IMAGE, profileImageUri);
        }

        long id;
        if (userExists) {
            // Update the first user (we're assuming a single-user app)
            id = db.update(TABLE_USER, values, COLUMN_ID + "= ?", new String[]{"1"});
        } else {
            // Insert new user
            values.put(COLUMN_PASSWORD, password); // Make sure password is set for new user
            id = db.insert(TABLE_USER, null, values);
        }

        db.close();
        return id;
    }

    /**
     * Update only the profile image URI
     * Now with persistent storage handling
     */
    public boolean updateProfileImage(String imageUriString) {
        try {
            Uri sourceUri = Uri.parse(imageUriString);

            // Create a permanent copy of the image in the app's private storage
            String permanentImagePath = copyImageToPrivateStorage(sourceUri);

            if (permanentImagePath != null) {
                // Now save this permanent path to the database
                SQLiteDatabase db = this.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(COLUMN_PROFILE_IMAGE, permanentImagePath);

                // Update user's profile image (assuming single user with ID 1)
                int rowsAffected = db.update(TABLE_USER, values, COLUMN_ID + "= ?", new String[]{"1"});
                db.close();

                return rowsAffected > 0;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Copy image from gallery/source to app's private storage
     */
    private String copyImageToPrivateStorage(Uri sourceUri) {
        try {
            ContentResolver resolver = mContext.getContentResolver();
            InputStream inputStream = resolver.openInputStream(sourceUri);

            if (inputStream == null) {
                return null;
            }

            // Create a file in app's private storage directory
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "PROFILE_" + timeStamp + ".jpg";
            File storageDir = mContext.getFilesDir();
            File imageFile = new File(storageDir, imageFileName);

            OutputStream outputStream = new FileOutputStream(imageFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            // Return the file URI as a string that can be used later
            return "file://" + imageFile.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Update user's password
     */
    public boolean updatePassword(String currentPassword, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();

        // First verify the current password
        Cursor cursor = db.query(TABLE_USER, new String[]{COLUMN_PASSWORD}, null, null, null, null, null);
        boolean result = false;

        if (cursor != null && cursor.moveToFirst()) {
            int passwordIndex = cursor.getColumnIndex(COLUMN_PASSWORD);
            if (passwordIndex != -1) {
                String storedPassword = cursor.getString(passwordIndex);

                if (storedPassword.equals(currentPassword)) {
                    ContentValues values = new ContentValues();
                    values.put(COLUMN_PASSWORD, newPassword);

                    int rowsAffected = db.update(TABLE_USER, values, COLUMN_ID + "= ?", new String[]{"1"});
                    result = rowsAffected > 0;
                }
            }
            cursor.close();
        }

        db.close();
        return result;
    }

    /**
     * Get user profile data
     */
    public UserProfile getUserProfile() {
        SQLiteDatabase db = this.getReadableDatabase();
        UserProfile userProfile = null;

        String[] columns = {
                COLUMN_ID,
                COLUMN_USERNAME,
                COLUMN_EMAIL,
                COLUMN_PHONE,
                COLUMN_PASSWORD,
                COLUMN_PROFILE_IMAGE
        };

        Cursor cursor = db.query(TABLE_USER, columns, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            userProfile = new UserProfile();

            int idIndex = cursor.getColumnIndex(COLUMN_ID);
            int usernameIndex = cursor.getColumnIndex(COLUMN_USERNAME);
            int emailIndex = cursor.getColumnIndex(COLUMN_EMAIL);
            int phoneIndex = cursor.getColumnIndex(COLUMN_PHONE);
            int passwordIndex = cursor.getColumnIndex(COLUMN_PASSWORD);
            int profileImageIndex = cursor.getColumnIndex(COLUMN_PROFILE_IMAGE);

            if (idIndex != -1) {
                userProfile.setId(cursor.getInt(idIndex));
            }

            if (usernameIndex != -1) {
                userProfile.setUsername(cursor.getString(usernameIndex));
            }

            if (emailIndex != -1) {
                userProfile.setEmail(cursor.getString(emailIndex));
            }

            if (phoneIndex != -1) {
                userProfile.setPhone(cursor.getString(phoneIndex));
            }

            if (passwordIndex != -1) {
                userProfile.setPassword(cursor.getString(passwordIndex));
            }

            if (profileImageIndex != -1) {
                userProfile.setProfileImageUri(cursor.getString(profileImageIndex));
            }

            cursor.close();
        }

        db.close();
        return userProfile;
    }

    /**
     * Check if user credentials are valid
     */
    public boolean checkUserCredentials(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER,
                new String[]{COLUMN_ID},
                COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{email, password},
                null, null, null);

        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return exists;
    }

    /**
     * Check if database is empty
     */
    public boolean isDatabaseEmpty() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER, new String[]{COLUMN_ID}, null, null, null, null, null);
        boolean isEmpty = cursor == null || cursor.getCount() == 0;
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return isEmpty;
    }

    /**
     * Import legacy data
     */
    public void importLegacyData(String username, String email, String phone, String profileImageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PHONE, phone);
        if (profileImageUri != null) {
            values.put(COLUMN_PROFILE_IMAGE, profileImageUri);
        }
        db.insert(TABLE_USER, null, values);
        db.close();
    }
} 