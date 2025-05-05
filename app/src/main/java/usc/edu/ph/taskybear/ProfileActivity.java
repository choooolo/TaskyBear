package usc.edu.ph.taskybear;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

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

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private ImageView todobtn,shelfbtn,homebtn,schedbtn;
    CircleImageView profileImage;
    Button changePicBtn, logoutBtn, changePasswordBtn;
    EditText usernameField, emailField, phoneField;
    ImageView editUsernameIcon, editEmailIcon, editPhoneIcon;
    //asddsdasd
    ActivityResultLauncher<String> imagePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        todobtn = findViewById(R.id.todobtn);
        homebtn = findViewById(R.id.homebtn);
        schedbtn = findViewById(R.id.schedbtn);

        shelfbtn = findViewById(R.id.shelfbtn);
        changePasswordBtn = findViewById(R.id.changePasswordBtn);

        profileImage = findViewById(R.id.profileImage);
        changePicBtn = findViewById(R.id.changePicBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        changePasswordBtn = findViewById(R.id.changePasswordBtn);

        usernameField = findViewById(R.id.usernameField);
        emailField = findViewById(R.id.emailField);
        phoneField = findViewById(R.id.phoneField);

        editUsernameIcon = findViewById(R.id.icon_edit_username);
        editEmailIcon = findViewById(R.id.icon_edit_email);
        editPhoneIcon = findViewById(R.id.icon_edit_phone);
        homebtn.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
        });

        shelfbtn.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, ShelfActivity.class));
        });
        todobtn.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, ToDoActivity.class));
        });
        schedbtn.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, ScheduleActivity.class));
        });
        changePasswordBtn.setOnClickListener(view -> {
            LayoutInflater inflater = LayoutInflater.from(this);
            View dialogView = inflater.inflate(R.layout.dialog_change_password, null);

            EditText currentPassword = dialogView.findViewById(R.id.currentPassword);
            EditText newPassword = dialogView.findViewById(R.id.newPassword);
            EditText retypePassword = dialogView.findViewById(R.id.retypePassword);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Change Password")
                    .setView(dialogView)
                    .setPositiveButton("Change", (dialog, which) -> {
                        String current = currentPassword.getText().toString();
                        String newPass = newPassword.getText().toString();
                        String retype = retypePassword.getText().toString();

                        if (newPass.equals(retype)) {
                            Toast.makeText(this, "Password changed!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "New passwords do not match!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
        });
        imagePicker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                startCrop(uri);
            }
        });

        changePicBtn.setOnClickListener(v -> imagePicker.launch("image/*"));

        logoutBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, LandingPageActivity.class);
            startActivity(intent);
            finish();
        });

        Button changePasswordBtn = findViewById(R.id.changePasswordBtn);

        changePasswordBtn.setOnClickListener(view -> {
            LayoutInflater inflater = LayoutInflater.from(this);
            View dialogView = inflater.inflate(R.layout.dialog_change_password, null);

            EditText currentPassword = dialogView.findViewById(R.id.currentPassword);
            EditText newPassword = dialogView.findViewById(R.id.newPassword);
            EditText retypePassword = dialogView.findViewById(R.id.retypePassword);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Change Password")
                    .setView(dialogView)
                    .setPositiveButton("Change", (dialog, which) -> {
                        String current = currentPassword.getText().toString();
                        String newPass = newPassword.getText().toString();
                        String retype = retypePassword.getText().toString();

                        if (newPass.equals(retype)) {
                            // Proceed with password change logic here
                            Toast.makeText(this, "Password changed!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "New passwords do not match!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
        });


        editUsernameIcon.setOnClickListener(v -> toggleEditSave(usernameField, editUsernameIcon));

        editEmailIcon.setOnClickListener(v -> toggleEditSave(emailField, editEmailIcon));

        editPhoneIcon.setOnClickListener(v -> toggleEditSave(phoneField, editPhoneIcon));
    }

    private void startCrop(Uri sourceUri) {
        String destName = UUID.randomUUID().toString() + ".jpg";
        Uri destUri = Uri.fromFile(new File(getCacheDir(), destName));
        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(true);
        options.setShowCropGrid(false);
        UCrop.of(sourceUri, destUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(300, 300)
                .withOptions(options)
                .start(ProfileActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Uri resultUri = UCrop.getOutput(data);
            profileImage.setImageURI(resultUri);
        }
    }

    private void toggleEditSave(EditText field, ImageView icon) {
        if (field.isEnabled()) {
            icon.setImageResource(R.drawable.check_icon);
            field.setEnabled(false);
        } else {
            icon.setImageResource(R.drawable.edit_icon);
            field.setEnabled(true);
        }
    }
}
