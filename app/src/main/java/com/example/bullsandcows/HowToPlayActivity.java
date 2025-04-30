package com.example.bullsandcows;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HowToPlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_play);

        // Initialize interactive elements
        Button showFeedbackBtn = findViewById(R.id.btn_show_feedback);
        TextView feedbackText = findViewById(R.id.txt_feedback);

        // Set up the interactive example
        showFeedbackBtn.setOnClickListener(v -> {
            feedbackText.setText("1 🐂 (digit 2)\n1 🐄 (digit 4)");
            feedbackText.setVisibility(View.VISIBLE);
            showFeedbackBtn.setVisibility(View.GONE);
        });
    }
}