package usc.edu.ph.taskybear;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CreateAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        TextView alreadyhave;

        setContentView(R.layout.activity_create_account);
        alreadyhave = findViewById(R.id.alreadyhaveaccount);
        alreadyhave.setOnClickListener(v -> {
            Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
            startActivity(intent);
        });

    }
}