package com.example.bullsandcows;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PlayGameActivity extends AppCompatActivity {

    // UI Elements
    private TextView timerText, scoreText, attemptText, resultText;
    private EditText inputCode;
    private Button submitButton, replayButton;
    private ImageView moodIcon;
    private ProgressBar timerProgress;
    private RecyclerView previousAttemptsRecycler;

    // Game Data
    private AttemptAdapter attemptAdapter;
    private ArrayList<String> attemptsHistory = new ArrayList<>();
    private String username;
    private String secretCode;
    private int score = 0;
    private int attemptsRemaining;
    private int seconds = 0;
    private int maxGameSeconds = 60;

    // Firebase and SQLite
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Sounds
    private MediaPlayer tickSound, winSound, loseSound;

    // Timer
    private final Handler handler = new Handler();
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            seconds++;
            int mins = seconds / 60;
            int secs = seconds % 60;
            timerText.setText(String.format("%02d:%02d", mins, secs));
            timerProgress.setProgress(seconds);

            if (seconds >= maxGameSeconds) {
                Toast.makeText(PlayGameActivity.this, "Time's up!", Toast.LENGTH_SHORT).show();
                playSound(loseSound);
                endGame();
            } else {
                playSound(tickSound);
                handler.postDelayed(this, 1000);
            }
        }
    };

    // Lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_game);

        initViews();
        loadUserData();
        loadGameSettings();
        startNewGame();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(timerRunnable);
        releaseSounds();
    }

    // Initialization

    private void initViews() {
        timerText = findViewById(R.id.timer_text);
        scoreText = findViewById(R.id.score_text);
        attemptText = findViewById(R.id.attempts_text);
        resultText = findViewById(R.id.result_text);
        inputCode = findViewById(R.id.input_code);
        submitButton = findViewById(R.id.submit_button);
        replayButton = findViewById(R.id.replay_button);
        moodIcon = findViewById(R.id.mood_icon);
        timerProgress = findViewById(R.id.timer_progress);
        previousAttemptsRecycler = findViewById(R.id.previous_attempts_recycler);

        // Recycler setup
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        previousAttemptsRecycler.setLayoutManager(layoutManager);
        attemptAdapter = new AttemptAdapter(attemptsHistory);
        previousAttemptsRecycler.setAdapter(attemptAdapter);

        // Sound setup
        tickSound = MediaPlayer.create(this, R.raw.tick);
        winSound = MediaPlayer.create(this, R.raw.sucess);
        loseSound = MediaPlayer.create(this, R.raw.error);

        // Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Buttons
        submitButton.setOnClickListener(v -> validateCode(inputCode.getText().toString()));
        replayButton.setOnClickListener(v -> startNewGame());
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        username = prefs.getString("username", "Player");

        TextView welcome = findViewById(R.id.player_name);
        welcome.setText("Hey, " + username + " 👋");
    }

    private void loadGameSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int wordLength = Integer.parseInt(prefs.getString("word_length", "4"));
        maxGameSeconds = Integer.parseInt(prefs.getString("game_duration", "60"));
        attemptsRemaining = Integer.parseInt(prefs.getString("attempts_limit", "3"));
        timerProgress.setMax(maxGameSeconds);
        secretCode = generateSecretCode(wordLength);
    }

    // Game Logic

    private void startNewGame() {
        handler.removeCallbacks(timerRunnable);
        releaseSounds();
        tickSound = MediaPlayer.create(this, R.raw.tick);

        score = 0;
        seconds = 0;
        resultText.setText("");
        inputCode.setText("");
        inputCode.setEnabled(true);
        submitButton.setEnabled(true);
        replayButton.setVisibility(View.GONE);
        attemptsHistory.clear();
        attemptAdapter.notifyDataSetChanged();

        loadGameSettings();
        updateUI();
        handler.post(timerRunnable);
    }

    private void validateCode(String guess) {
        if (guess.length() != secretCode.length()) {
            Toast.makeText(this, "Enter a " + secretCode.length() + "-digit code", Toast.LENGTH_SHORT).show();
            return;
        }

        String feedback = getBullsAndCows(secretCode, guess);
        resultText.setText(feedback);
        animateResult();

        attemptsHistory.add(guess + " ➜ " + feedback);
        attemptAdapter.notifyItemInserted(attemptsHistory.size() - 1);
        previousAttemptsRecycler.scrollToPosition(attemptsHistory.size() - 1);

        if (feedback.startsWith("You guessed it!")) {
            score += 10;
            moodIcon.setImageResource(R.drawable.ic_mode_happy);
            playSound(winSound);
            endGame();
        } else {
            attemptsRemaining--;
            moodIcon.setImageResource(R.drawable.ic_mode_sad);
            moodIcon.animate().rotationBy(20f).setDuration(100)
                    .withEndAction(() -> moodIcon.animate().rotationBy(-40f).setDuration(100)
                            .withEndAction(() -> moodIcon.animate().rotationBy(20f).setDuration(100)))
                    .start();
        }

        if (attemptsRemaining <= 0) {
            Toast.makeText(this, "Game Over! Code was: " + secretCode, Toast.LENGTH_LONG).show();
            playSound(loseSound);
            endGame();
        }

        updateUI();
        inputCode.setText("");
    }

    private void endGame() {
        handler.removeCallbacks(timerRunnable);
        inputCode.setEnabled(false);
        submitButton.setEnabled(false);
        replayButton.setVisibility(View.VISIBLE);
        replayButton.setAlpha(0f);
        replayButton.animate().alpha(1f).setDuration(500).start();

        releaseSounds();

        saveScoreToFirestore();
        saveScoreLocal();
    }

    private void updateUI() {
        scoreText.setText("Score: " + score);
        attemptText.setText("Attempts Left: " + attemptsRemaining);
    }

    private void animateResult() {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(300);
        resultText.startAnimation(fadeIn);
    }

    // Utilities

    private void saveScoreToFirestore() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && username != null) {
            Map<String, Object> scoreEntry = new HashMap<>();
            scoreEntry.put("username", username);
            scoreEntry.put("score", score);
            scoreEntry.put("timestamp", System.currentTimeMillis());

            db.collection("scores").add(scoreEntry);
        }
    }

    private void saveScoreLocal() {
        GameDatabaseHelper dbHelper = new GameDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("score", score);
        values.put("timestamp", System.currentTimeMillis());

        db.insert("scores", null, values);
        db.close();
    }

    private void playSound(MediaPlayer sound) {
        if (sound != null) sound.start();
    }

    private void releaseSounds() {
        if (tickSound != null) {
            tickSound.stop();
            tickSound.release();
            tickSound = null;
        }
    }

    private String generateSecretCode(int length) {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    private String getBullsAndCows(String secret, String guess) {
        if (secret.equals(guess)) return "You guessed it!";

        int bulls = 0, cows = 0;
        boolean[] secretMatched = new boolean[secret.length()];
        boolean[] guessMatched = new boolean[guess.length()];

        for (int i = 0; i < secret.length(); i++) {
            if (secret.charAt(i) == guess.charAt(i)) {
                bulls++;
                secretMatched[i] = true;
                guessMatched[i] = true;
            }
        }

        for (int i = 0; i < guess.length(); i++) {
            if (guessMatched[i]) continue;
            for (int j = 0; j < secret.length(); j++) {
                if (!secretMatched[j] && guess.charAt(i) == secret.charAt(j)) {
                    cows++;
                    secretMatched[j] = true;
                    break;
                }
            }
        }

        return bulls + " 🐂 " + cows + " 🐄 ";
    }
}
