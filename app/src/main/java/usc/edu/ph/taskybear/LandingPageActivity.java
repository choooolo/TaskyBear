package usc.edu.ph.taskybear;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LandingPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_landing_page);

        Button createAccountButton = findViewById(R.id.createAccountButton);
        Button loginButton = findViewById(R.id.loginBtn); // use loginButton id

        createAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(LandingPageActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(LandingPageActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
