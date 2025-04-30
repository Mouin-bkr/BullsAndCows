package com.example.bullsandcows;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RecentScoresActivity extends AppCompatActivity {

    private ListView scoreListView;
    private ArrayList<String> scoreList;
    private ArrayAdapter<String> adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        scoreListView = findViewById(R.id.score_list_view);
        scoreList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, scoreList);
        scoreListView.setAdapter(adapter);


        db = FirebaseFirestore.getInstance();
        loadRecentScores();
    }
    private String formatTimestamp(Long millis) {
        Date date = new Date(millis);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(date);
    }


    private void loadRecentScores() {
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        String currentUsername = prefs.getString("username", null);

        if (currentUsername == null) {
            Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("scores")
                .whereEqualTo("username", currentUsername)
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        scoreList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Long score = document.getLong("score");
                            Long timestamp = document.getLong("timestamp");

                            if (score != null && timestamp != null) {
                                String formattedDate = formatTimestamp(timestamp);
                                scoreList.add("Score: " + score + " (" + formattedDate + ")");
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to load your scores", Toast.LENGTH_SHORT).show();
                    }
                });

    }

}
