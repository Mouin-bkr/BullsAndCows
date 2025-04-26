package com.example.bullsandcows;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
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

    private TextView timerText, scoreText, attemptText, resultText;
    private EditText inputCode;
    private Button submitButton, replayButton;
    private ImageView moodIcon;
    private ProgressBar timerProgress;
    private RecyclerView previousAttemptsRecycler;
    private AttemptAdapter attemptAdapter;
    private ArrayList<String> attemptsHistory = new ArrayList<>();

    private String username = null;

    private int score = 0;
    private int attemptsRemaining = 3;
    private int seconds = 0;
    private int maxGameSeconds = 60;
    private String secretCode = "1234";
    private Handler handler = new Handler();

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private MediaPlayer tickSound, winSound, loseSound;

    private Runnable timerRunnable = new Runnable() {
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
                return;
            }

            playSound(tickSound);
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_game);

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

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();


        tickSound = MediaPlayer.create(this, R.raw.tick);
        winSound = MediaPlayer.create(this, R.raw.sucess);
        loseSound = MediaPlayer.create(this, R.raw.error);


        GameDatabaseHelper dbHelper = new GameDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT username FROM users ORDER BY id DESC LIMIT 1", null);
        username = null;
        if (cursor.moveToFirst()) {
            username = cursor.getString(0);
        }if (username != null) {
                            TextView welcome = findViewById(R.id.player_name);
                            welcome.setText("Hey, " + username + " 👋");
                        }

        cursor.close();
        db.close();

        //  Get username from SharedPreferences
        SharedPreferences userPrefs = getSharedPreferences("user_data", MODE_PRIVATE);
         username = userPrefs.getString("username", "Player");

        //  Get game settings from PreferenceManager
        SharedPreferences settingsprefs = PreferenceManager.getDefaultSharedPreferences(this);
        int wordLength = Integer.parseInt(settingsprefs.getString("word_length", "4"));
        maxGameSeconds = Integer.parseInt(settingsprefs.getString("game_duration", "60"));
        attemptsRemaining = Integer.parseInt(settingsprefs.getString("attempts_limit", "3"));

        secretCode = generateSecretCode(wordLength);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        previousAttemptsRecycler.setLayoutManager(layoutManager);


        attemptAdapter = new AttemptAdapter(attemptsHistory);
        previousAttemptsRecycler.setAdapter(attemptAdapter);

        timerProgress.setMax(maxGameSeconds);

        updateUI();
        handler.post(timerRunnable);

        submitButton.setOnClickListener(v -> validateCode(inputCode.getText().toString()));

        replayButton.setOnClickListener(v -> resetGame(wordLength));
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
            moodIcon.animate().rotationYBy(360).setDuration(500).start(); //Animation
        } else {
            attemptsRemaining--;
            moodIcon.setImageResource(R.drawable.ic_mode_sad);
            moodIcon.animate().rotationYBy(360).setDuration(500).start(); //  Animation
        }


        if (attemptsRemaining <= 0) {
            Toast.makeText(this, "Game Over! Code was: " + secretCode, Toast.LENGTH_LONG).show();
            playSound(loseSound);
            tickSound.release();
            endGame();
        }

        updateUI();
        inputCode.setText("");
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

    private void endGame() {
        handler.removeCallbacks(timerRunnable);
        submitButton.setEnabled(false);
        inputCode.setEnabled(false);
        replayButton.setVisibility(View.VISIBLE);
        saveScoreToFirestore();
        saveScoreLocal( username, score);
    }

    public void saveScoreLocal(String username, int score) {
        GameDatabaseHelper dbHelper = new GameDatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("score", score);
        values.put("timestamp", System.currentTimeMillis());

        db.insert("scores", null, values);
        db.close();
    }


    private void resetGame(int wordLength) {
        score = 0;
        seconds = 0;
        secretCode = generateSecretCode(wordLength);
        attemptsRemaining = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("attempts_limit", "3"));
        timerProgress.setProgress(0);
        attemptsHistory.clear();
        attemptAdapter.notifyDataSetChanged();

        resultText.setText("");
        inputCode.setEnabled(true);
        submitButton.setEnabled(true);
        replayButton.setVisibility(View.GONE);
        tickSound = MediaPlayer.create(this, R.raw.tick);
        handler.post(timerRunnable);
        updateUI();
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
        if (secret.equals(guess)) {
            return "You guessed it!";
        }

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

    private void saveScoreToFirestore() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        String username = document.getString("username");
                        if (username == null) username = user.getEmail();

                        Map<String, Object> scoreEntry = new HashMap<>();
                        scoreEntry.put("username", username);
                        scoreEntry.put("score", score);

                        db.collection("scores")
                                .add(scoreEntry)
                                .addOnSuccessListener(documentReference ->
                                        Toast.makeText(this, "Score saved!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Failed to save score.", Toast.LENGTH_SHORT).show());
                    });
        }
    }

    private void playSound(MediaPlayer sound) {
        if (sound != null) sound.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(timerRunnable);
        if (tickSound != null) tickSound.release();
        if (winSound != null) winSound.release();
        if (loseSound != null) loseSound.release();
    }
}
