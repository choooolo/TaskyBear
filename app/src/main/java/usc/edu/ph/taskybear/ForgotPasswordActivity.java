package usc.edu.ph.taskybear;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private Button resetButton, backButton;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        usernameEditText = findViewById(R.id.usernameEditText); // Make sure your forgot password XML has an EditText for username
        resetButton = findViewById(R.id.resetButton);
        backButton = findViewById(R.id.backButton);

        dbHelper = new DatabaseHelper(this);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();

                if (username.isEmpty()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter your username", Toast.LENGTH_SHORT).show();
                } else {
                    boolean userExists = dbHelper.checkUsername(username);
                    if (userExists) {
                        // Navigate to ChangePasswordActivity to set a new password
                        Intent intent = new Intent(ForgotPasswordActivity.this, ChangePasswordActivity.class);
                        intent.putExtra("username", username);  // Pass username to ChangePasswordActivity
                        startActivity(intent);
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Username not found.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
