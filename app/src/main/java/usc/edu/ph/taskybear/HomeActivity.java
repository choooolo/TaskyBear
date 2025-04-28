package usc.edu.ph.taskybear;

import static android.os.Build.*;
import static android.os.Build.VERSION.*;


import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

public class HomeActivity extends AppCompatActivity {

    CalendarView calendarView;
    TextView addTaskText, userNameTextView; // Add userNameTextView
    LinearLayout taskList;
    private ImageView todobtn, shelfbtn, profilebtn, schedbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize the views
        todobtn = findViewById(R.id.todobtn);
        shelfbtn = findViewById(R.id.shelfbtn);
        schedbtn = findViewById(R.id.schedbtn);
        profilebtn = findViewById(R.id.profilebtn);
        calendarView = findViewById(R.id.calendarView);
        addTaskText = findViewById(R.id.addtask);
        taskList = findViewById(R.id.taskList);
        userNameTextView = findViewById(R.id.userNameTextView); // Initialize userNameTextView

        // Retrieve the username from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserDetails", MODE_PRIVATE);
        String userName = sharedPreferences.getString("username", "Jane Doe"); // Default to "Jane Doe" if not found

        // Set the username in the TextView
        userNameTextView.setText(userName);

        // Set up button click listeners (existing code)
        todobtn.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ToDoActivity.class)));
        schedbtn.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ScheduleActivity.class)));
        shelfbtn.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ShelfActivity.class)));
        profilebtn.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));

        // Calendar view listener (existing code)
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> showCalendarTaskDialog(year, month, dayOfMonth));

        // Add task click listener (existing code)
        addTaskText.setOnClickListener(view -> showPopupTaskDialog());
    }

    private void showCalendarTaskDialog(int year, int month, int dayOfMonth) {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("Edit Tasks for " + (month + 1) + "/" + dayOfMonth + "/" + year);

        final EditText input = new EditText(HomeActivity.this);
        input.setHint("Enter your task here...");
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, id) -> {
            String task = input.getText().toString();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showPopupTaskDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.task_input_dialog, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        EditText editText = dialogView.findViewById(R.id.editTextTask);
        Button addButton = dialogView.findViewById(R.id.addButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        addButton.setOnClickListener(v -> {
            String taskText = editText.getText().toString().trim();
            if (!taskText.isEmpty()) {
                LinearLayout contentLayout = new LinearLayout(this);
                contentLayout.setOrientation(LinearLayout.HORIZONTAL);
                contentLayout.setPadding(20, 20, 20, 20);
                contentLayout.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.START);

                TextView taskContent = new TextView(this);
                taskContent.setText(taskText);
                taskContent.setTextSize(16f);
                taskContent.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

                TextView deleteBtn = new TextView(this);
                deleteBtn.setText("✖");
                deleteBtn.setTextSize(18f);
                deleteBtn.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                deleteBtn.setPadding(16, 0, 0, 0);

                CardView taskCard = new CardView(this);
                taskCard.setRadius(16f);
                taskCard.setCardElevation(6f);
                taskCard.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white));

                contentLayout.addView(taskContent);
                contentLayout.addView(deleteBtn);
                taskCard.addView(contentLayout);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.topMargin = 16;

                taskList.addView(taskCard, params);

                deleteBtn.setOnClickListener(del -> taskList.removeView(taskCard));

                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}

