package usc.edu.ph.taskybear;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import de.hdodenhof.circleimageview.CircleImageView;

import java.io.File;

import com.bumptech.glide.Glide;

import android.content.SharedPreferences;

public class ProfileActivity extends AppCompatActivity {
    private ImageView todobtn, shelfbtn, homebtn, schedbtn;
    CircleImageView profileImage;
    Button changePicBtn, logoutBtn, changePasswordBtn;
    EditText usernameField, emailField, phoneField;
    ImageView editUsernameIcon, editEmailIcon, editPhoneIcon;

    // Database helper
    private DatabaseHelper databaseHelper;
    private String currentUsername;

    // Image picker launcher
    ActivityResultLauncher<String> imagePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);
        
        // Get current username from intent
        currentUsername = getIntent().getStringExtra("USERNAME");
        if (currentUsername == null) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initializeViews();
        
        // Load user data from database
        loadUserProfileData();

        // Set up click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        todobtn = findViewById(R.id.todobtn);
        homebtn = findViewById(R.id.homebtn);
        schedbtn = findViewById(R.id.schedbtn);
        shelfbtn = findViewById(R.id.shelfbtn);
        changePasswordBtn = findViewById(R.id.changePasswordBtn);
        profileImage = findViewById(R.id.profileImage);
        changePicBtn = findViewById(R.id.changePicBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        usernameField = findViewById(R.id.usernameField);
        emailField = findViewById(R.id.emailField);
        phoneField = findViewById(R.id.phoneField);
        editUsernameIcon = findViewById(R.id.icon_edit_username);
        editEmailIcon = findViewById(R.id.icon_edit_email);
        editPhoneIcon = findViewById(R.id.icon_edit_phone);
    }

    private void setupClickListeners() {
        // Navigation buttons
        homebtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            intent.putExtra("USERNAME", currentUsername);
            startActivity(intent);
        });

        shelfbtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ShelfActivity.class);
            intent.putExtra("USERNAME", currentUsername);
            startActivity(intent);
        });

        todobtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ToDoActivity.class);
            intent.putExtra("USERNAME", currentUsername);
            startActivity(intent);
        });

        schedbtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ScheduleActivity.class);
            intent.putExtra("USERNAME", currentUsername);
            startActivity(intent);
        });

        // Image picker
        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        profileImage.setImageURI(uri);
                        int userId = databaseHelper.getUserId(currentUsername);
                        if (userId != -1) {
                            boolean success = databaseHelper.updateProfileImage(userId, uri.toString());
                            if (success) {
                                Toast.makeText(ProfileActivity.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ProfileActivity.this, "Failed to save profile picture", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );

        changePicBtn.setOnClickListener(v -> imagePicker.launch("image/*"));

        // Logout button
        logoutBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, LandingPageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Change password button
        changePasswordBtn.setOnClickListener(view -> showChangePasswordDialog());

        // Edit icons
        editUsernameIcon.setOnClickListener(v -> toggleEditSave(usernameField, editUsernameIcon));
        editEmailIcon.setOnClickListener(v -> toggleEditSave(emailField, editEmailIcon));
        editPhoneIcon.setOnClickListener(v -> toggleEditSave(phoneField, editPhoneIcon));
    }

    private void showChangePasswordDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_change_password, null);

        EditText currentPassword = dialogView.findViewById(R.id.currentPassword);
        EditText newPassword = dialogView.findViewById(R.id.newPassword);
        EditText retypePassword = dialogView.findViewById(R.id.retypePassword);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("Change", null) // Set to null initially
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Override the positive button click to validate input
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String current = currentPassword.getText().toString().trim();
            String newPass = newPassword.getText().toString().trim();
            String retype = retypePassword.getText().toString().trim();

            // Validate input
            if (current.isEmpty() || newPass.isEmpty() || retype.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPass.length() < 6) {
                Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(retype)) {
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            int userId = databaseHelper.getUserId(currentUsername);
            if (userId != -1) {
                boolean success = databaseHelper.updatePassword(userId, current, newPass);
                if (success) {
                    Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleEditSave(EditText field, ImageView icon) {
        if (field.isEnabled()) {
            icon.setImageResource(R.drawable.edit_icon);
            field.setEnabled(false);
            saveUserProfileData();
            Toast.makeText(this, field.getHint() + " updated", Toast.LENGTH_SHORT).show();
        } else {
            icon.setImageResource(R.drawable.check_icon);
            field.setEnabled(true);
            field.requestFocus();
        }
    }

    private void loadUserProfileData() {
        int userId = databaseHelper.getUserId(currentUsername);
        if (userId != -1) {
            UserProfile userProfile = databaseHelper.getUserProfile(userId);
            if (userProfile != null) {
                usernameField.setText(userProfile.getUsername());
                emailField.setText(userProfile.getEmail());
                phoneField.setText(userProfile.getPhone());

                if (userProfile.getProfileImageUri() != null && !userProfile.getProfileImageUri().isEmpty()) {
                    try {
                        Uri imageUri = Uri.parse(userProfile.getProfileImageUri());
                        if (imageUri.getScheme().equals("file")) {
                            File imageFile = new File(imageUri.getPath());
                            if (!imageFile.exists()) {
                                Toast.makeText(this, "Profile image file not found", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        // Use Glide to load the image
                        Glide.with(this)
                            .load(imageUri)
                            .placeholder(R.drawable.userlogo)
                            .error(R.drawable.userlogo)
                            .into(profileImage);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error loading profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void saveUserProfileData() {
        String username = usernameField.getText().toString();
        String email = emailField.getText().toString();
        String phone = phoneField.getText().toString();

        int userId = databaseHelper.getUserId(currentUsername);
        if (userId != -1) {
            // Get current profile data to preserve the profile image
            UserProfile currentProfile = databaseHelper.getUserProfile(userId);
            String profileImageUri = currentProfile != null ? currentProfile.getProfileImageUri() : null;

            // Update the profile with all data including the preserved profile image
            boolean success = databaseHelper.updateUserProfile(userId, username, email, phone, profileImageUri);
            
            if (success) {
                // Update current username if it was changed
                if (!username.equals(currentUsername)) {
                    currentUsername = username;
                    // Update SharedPreferences with new username
                    SharedPreferences sharedPreferences = getSharedPreferences("TaskyPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", username);
                    editor.apply();
                }
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfileData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveUserProfileData();
    }
}