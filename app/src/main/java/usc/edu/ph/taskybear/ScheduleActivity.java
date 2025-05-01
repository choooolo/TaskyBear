package usc.edu.ph.taskybear;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

public class ScheduleActivity extends AppCompatActivity {
    private static final int FILE_SELECT_CODE = 1;

    private ImageView todobtn, shelfbtn, profilebtn, homebtn, schedulebtn;
    private LinearLayout uploadCardContainer, scheduleDisplayContainer;
    private Button selectFromDeviceBtn, uploadBtn;
    private EditText linkInput;
    private Switch everydaySwitch, durationSwitch;
    private ImageView calendarBtn, fab, deleteIcon;
    private TextView selectedDateText;

    private String selectedDate = "";
    private boolean isUploadCardVisible = false;
    private String uploadedFileName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_schedule);

        // Navigation
        todobtn = findViewById(R.id.todobtn);
        shelfbtn = findViewById(R.id.shelfbtn);
        homebtn = findViewById(R.id.homebtn);
        profilebtn = findViewById(R.id.profilebtn);
        schedulebtn = findViewById(R.id.schedbtn);

        todobtn.setOnClickListener(v -> startActivity(new Intent(this, ToDoActivity.class)));
        shelfbtn.setOnClickListener(v -> startActivity(new Intent(this, ShelfActivity.class)));
        homebtn.setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        profilebtn.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        schedulebtn.setOnClickListener(v -> {}); // Already here

        // UI elements
        uploadCardContainer = findViewById(R.id.uploadCardContainer);
        uploadCardContainer.setVisibility(View.GONE);
        scheduleDisplayContainer = findViewById(R.id.scheduleDisplayContainer);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> toggleUploadCard());

        selectFromDeviceBtn = findViewById(R.id.selectFromDeviceBtn);
        linkInput = findViewById(R.id.linkInput);
        everydaySwitch = findViewById(R.id.everydaySwitch);
        durationSwitch = findViewById(R.id.durationSwitch);
        calendarBtn = findViewById(R.id.calendarBtn);
        selectedDateText = findViewById(R.id.selectedDateText);
        uploadBtn = findViewById(R.id.uploadBtn);
        deleteIcon = findViewById(R.id.deleteIcon);

        selectFromDeviceBtn.setOnClickListener(v -> openFilePicker());
        calendarBtn.setOnClickListener(v -> showDatePicker());
        deleteIcon.setOnClickListener(v -> hideUploadCard());

        uploadBtn.setOnClickListener(v -> {
            String link = linkInput.getText().toString().trim();
            boolean isEveryday = everydaySwitch.isChecked();

            if (link.isEmpty() && uploadedFileName.isEmpty()) {
                Toast.makeText(this, "Please provide a link or upload a file", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }

            String source = !link.isEmpty() ? link : uploadedFileName;
            addScheduleItem(source, selectedDate, isEveryday);

            // Reset form
            linkInput.setText("");
            everydaySwitch.setChecked(false);
            durationSwitch.setChecked(false);
            selectedDate = "";
            uploadedFileName = "";
            selectedDateText.setText("Selected Date:");

            Toast.makeText(this, "Schedule added", Toast.LENGTH_SHORT).show();
            hideUploadCard();
        });
    }

    private void toggleUploadCard() {
        if (isUploadCardVisible) hideUploadCard();
        else showUploadCard();
    }

    private void showUploadCard() {
        uploadCardContainer.setVisibility(View.VISIBLE);
        isUploadCardVisible = true;
    }

    private void hideUploadCard() {
        uploadCardContainer.setVisibility(View.GONE);
        isUploadCardVisible = false;
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimetypes = {"application/pdf", "text/csv", "text/calendar", "application/octet-stream"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        startActivityForResult(Intent.createChooser(intent, "Select Schedule File"), FILE_SELECT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            uploadedFileName = getFileNameFromUri(uri);
            parseFile(uri);
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = "";
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        if (result == null || result.isEmpty()) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) result = result.substring(cut + 1);
        }
        return result;
    }

    private void parseFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder fileContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append("\n");
            }
            reader.close();
            Toast.makeText(this, "File loaded: " + uploadedFileName, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to read file", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    selectedDate = (month1 + 1) + "/" + dayOfMonth + "/" + year1;
                    selectedDateText.setText("Selected Date: " + selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void addScheduleItem(String source, String date, boolean isEveryday) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.schedule_item_layout, scheduleDisplayContainer, false);

        TextView pdfName = itemView.findViewById(R.id.pdfName);
        TextView durationText = itemView.findViewById(R.id.scheduleDuration);
        TextView scheduleDate = itemView.findViewById(R.id.scheduleDate);
        TextView scheduleSource = itemView.findViewById(R.id.scheduleSource);

        // Show file name or link
        pdfName.setText("Uploaded: " + source);

        // Compute dynamic duration
        int durationDays = 0;
        if (!date.equals("Unknown") && !date.isEmpty()) {
            try {
                String[] parts = date.split("/");
                int month = Integer.parseInt(parts[0]) - 1;
                int day = Integer.parseInt(parts[1]);
                int year = Integer.parseInt(parts[2]);

                Calendar selectedCal = Calendar.getInstance();
                selectedCal.set(year, month, day, 0, 0, 0);
                selectedCal.set(Calendar.MILLISECOND, 0);

                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);

                long diffMillis = selectedCal.getTimeInMillis() - today.getTimeInMillis();
                durationDays = (int) Math.ceil(diffMillis / (1000.0 * 60 * 60 * 24));
                if (durationDays < 0) durationDays = 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        durationText.setText("Duration: " + durationDays + " days");
        scheduleDate.setText(date.isEmpty() ? "N/A" : date);
        scheduleSource.setText(linkInput.getText().toString().isEmpty() ? "From file" : "From link");

        scheduleDisplayContainer.addView(itemView);
    }
}
