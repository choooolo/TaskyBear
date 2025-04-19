package usc.edu.ph.taskybear;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {

    TextView forgotPasswordText;
    TextView registerhere;
    TextView login;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        forgotPasswordText = findViewById(R.id.forgotpasssword);
        forgotPasswordText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        registerhere = findViewById(R.id.registerhere);
        registerhere.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });
        login = findViewById(R.id.loginBtn);
        login.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        });

    }
}
