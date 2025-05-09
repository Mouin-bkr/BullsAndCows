package com.example.bullsandcows;

import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        Button playButton = findViewById(R.id.playButton);
        Button settingsButton = findViewById(R.id.settingsButton);
        Button viewScoresButton = findViewById(R.id.viewScoresButton);
        Button logoutButton = findViewById(R.id.logoutButton);
        Button leaderboardButton = findViewById(R.id.leaderboardButton);
        Button howToPlayButton = findViewById(R.id.howToPlayButton);


        TextView welcome = findViewById(R.id.playerWelcome);
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        String username = prefs.getString("username", "Player");
        welcome.setText("Hey, " + username + " 👋");



        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PlayGameActivity.class));
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

        viewScoresButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RecentScoresActivity.class));
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();

                //  Clear SharedPreferences on logout
                SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
                prefs.edit().clear().apply();

                Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }

        });
        leaderboardButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LeaderboardActivity.class));
        });

        howToPlayButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, HowToPlayActivity.class));
        });
    }
}
