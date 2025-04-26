package com.example.bullsandcows;

import static com.example.bullsandcows.R.*;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class LeaderboardActivity extends AppCompatActivity {
    private ListView leaderboardListView;
    private ArrayList<String> leaderboardList;
    private ArrayAdapter<String> adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        leaderboardListView = findViewById(R.id.leaderboard_list_view);
        leaderboardList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, leaderboardList);
        leaderboardListView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadLeaderboard();
    }

    private void loadLeaderboard() {
        db.collection("scores")
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        leaderboardList.clear();
                        int rank = 1;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String username = document.getString("username");
                            Long score = document.getLong("score");
                            leaderboardList.add(rank++ + ". " + username + " - " + score);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to load leaderboard", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}