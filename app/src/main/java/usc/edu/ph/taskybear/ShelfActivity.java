package usc.edu.ph.taskybear;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.io.File;

public class ShelfActivity extends AppCompatActivity {
    private LinearLayout booksContainer;
    private static final int PDF_PICKER_REQUEST = 101;
    private static final int IMAGE_PICKER_REQUEST = 102;
    private static final int STORAGE_PERMISSION_CODE = 100;

    private Uri selectedPdfUri;
    private Uri selectedImageUri;
    private ImageView bookCoverPreview;
    private ImageView todobtn, homebtn, profilebtn, schedbtn, profilePic;
    private TextView welcomeText, usernameText;
    private DatabaseHelper dbHelper;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shelf);

        // Initialize views
        todobtn = findViewById(R.id.todobtn);
        homebtn = findViewById(R.id.homebtn);
        profilebtn = findViewById(R.id.profilebtn);
        schedbtn = findViewById(R.id.schedbtn);
        profilePic = findViewById(R.id.profilePic);
        welcomeText = findViewById(R.id.welcomeText);
        usernameText = findViewById(R.id.username);
        booksContainer = findViewById(R.id.booksContainer);
        dbHelper = new DatabaseHelper(this);

        // Get username from intent
        currentUsername = getIntent().getStringExtra("USERNAME");
        if (currentUsername != null) {
            loadUserProfileData();
        }

        // Set up navigation buttons
        setupNavigationButtons();

        // Set up upload functionality
        setupUploadFunctionality();
    }

    private void setupNavigationButtons() {
        todobtn.setOnClickListener(v -> {
            Intent intent = new Intent(ShelfActivity.this, ToDoActivity.class);
            intent.putExtra("USERNAME", currentUsername);
            startActivity(intent);
        });

        schedbtn.setOnClickListener(v -> {
            Intent intent = new Intent(ShelfActivity.this, ScheduleActivity.class);
            intent.putExtra("USERNAME", currentUsername);
            startActivity(intent);
        });

        homebtn.setOnClickListener(v -> {
            Intent intent = new Intent(ShelfActivity.this, HomeActivity.class);
            intent.putExtra("USERNAME", currentUsername);
            startActivity(intent);
        });

        profilebtn.setOnClickListener(v -> {
            Intent intent = new Intent(ShelfActivity.this, ProfileActivity.class);
            intent.putExtra("USERNAME", currentUsername);
            startActivity(intent);
        });
    }

    private void setupUploadFunctionality() {
        CardView uploadCard = (CardView) booksContainer.getChildAt(0);
        LinearLayout uploadFromDeviceLayout = uploadCard.findViewById(R.id.uploadFromDeviceLayout);

        uploadFromDeviceLayout.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                showUploadDialog();
            }
        });
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 and above, we don't need storage permissions for accessing media
            return true;
        } else {
            // For Android 9 and below, we need both READ and WRITE permissions
            boolean readPermission = ContextCompat.checkSelfPermission(this, 
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            boolean writePermission = ContextCompat.checkSelfPermission(this, 
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

            if (!readPermission || !writePermission) {
                ActivityCompat.requestPermissions(this,
                    new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    STORAGE_PERMISSION_CODE);
                return false;
            }
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                showUploadDialog();
            } else {
                Toast.makeText(this, "Storage permission is required to access files", Toast.LENGTH_SHORT).show();
                // Show settings dialog if permission is permanently denied
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, 
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showSettingsDialog();
                }
            }
        }
    }

    private void showSettingsDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Storage permission is required to access files. Please grant permission in Settings.")
            .setPositiveButton("Settings", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            })
            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
            .show();
    }

    private void showUploadDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_upload_book);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        bookCoverPreview = dialog.findViewById(R.id.bookCoverPreview);
        EditText bookTitleInput = dialog.findViewById(R.id.bookTitleInput);
        Button chooseFileBtn = dialog.findViewById(R.id.chooseFileBtn);
        Button doneBtn = dialog.findViewById(R.id.doneBtn);

        // Pick cover image
        bookCoverPreview.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_PICKER_REQUEST);
        });

        // Pick PDF file
        chooseFileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            startActivityForResult(intent, PDF_PICKER_REQUEST);
        });

        // Done button logic
        doneBtn.setOnClickListener(v -> {
            String title = bookTitleInput.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a book title", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedPdfUri == null) {
                Toast.makeText(this, "Please select a PDF file", Toast.LENGTH_SHORT).show();
                return;
            }

            addBookToShelf(title, selectedImageUri, selectedPdfUri);
            selectedImageUri = null;
            selectedPdfUri = null;
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            if (requestCode == PDF_PICKER_REQUEST) {
                selectedPdfUri = uri;
                Toast.makeText(this, "PDF selected", Toast.LENGTH_SHORT).show();
            } else if (requestCode == IMAGE_PICKER_REQUEST) {
                selectedImageUri = uri;
                if (bookCoverPreview != null) {
                    Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.upload_icon)
                        .error(R.drawable.upload_icon)
                        .into(bookCoverPreview);
                }
            }
        }
    }

    private void addBookToShelf(String title, Uri imageUri, Uri pdfUri) {
        View bookCardView = LayoutInflater.from(this).inflate(R.layout.item_book_card, booksContainer, false);

        EditText bookTitle = bookCardView.findViewById(R.id.bookTitle);
        ImageView bookCover = bookCardView.findViewById(R.id.bookCover);
        ImageButton editTitleBtn = bookCardView.findViewById(R.id.editTitleBtn);
        ImageButton deleteBookBtn = bookCardView.findViewById(R.id.deleteBookBtn);
        Button openBookBtn = bookCardView.findViewById(R.id.openBookBtn);

        bookTitle.setText(title);

        if (imageUri != null) {
            Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.upload_icon)
                .error(R.drawable.upload_icon)
                .into(bookCover);
        } else {
            bookCover.setImageResource(R.drawable.upload_icon);
        }

        editTitleBtn.setOnClickListener(v -> {
            if (bookTitle.isEnabled()) {
                bookTitle.setEnabled(false);
                editTitleBtn.setImageResource(R.drawable.edit_icon);
            } else {
                bookTitle.setEnabled(true);
                bookTitle.requestFocus();
                editTitleBtn.setImageResource(R.drawable.check_icon);
            }
        });

        deleteBookBtn.setOnClickListener(v -> booksContainer.removeView(bookCardView));

        openBookBtn.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(pdfUri, "application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "No PDF reader installed", Toast.LENGTH_SHORT).show();
            }
        });

        booksContainer.addView(bookCardView, booksContainer.getChildCount() - 1);
    }

    private void loadUserProfileData() {
        int userId = dbHelper.getUserId(currentUsername);
        if (userId != -1) {
            UserProfile userProfile = dbHelper.getUserProfile(userId);
            if (userProfile != null) {
                // Update welcome text and username
                welcomeText.setText("Welcome Back!");
                usernameText.setText(userProfile.getUsername());
                
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
        if (currentUsername != null) {
            loadUserProfileData();
        }
    }
}
