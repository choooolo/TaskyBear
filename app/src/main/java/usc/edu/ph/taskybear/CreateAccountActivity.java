package usc.edu.ph.taskybear;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button createAccountButton;
    private TextView alreadyHaveAccount;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account);

        usernameEditText = findViewById(R.id.usernameEditText); // make sure your XML has this ID
        passwordEditText = findViewById(R.id.passwordEditText); // a
        createAccountButton = findViewById(R.id.createAccountButton);
        alreadyHaveAccount = findViewById(R.id.alreadyhaveaccount);

        dbHelper = new DatabaseHelper(this);

        createAccountButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(CreateAccountActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                boolean inserted = dbHelper.insertUser(username, password);
                if (inserted) {
                    Toast.makeText(CreateAccountActivity.this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CreateAccountActivity.this, "Account Creation Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        alreadyHaveAccount.setOnClickListener(v -> {
            Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
