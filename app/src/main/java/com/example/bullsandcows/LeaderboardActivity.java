package com.example.bullsandcows;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private List<ScoreItem> scoreItems;
    private ListView leaderboardListView;
    private ScoreCardAdapter adapter;
    private FirebaseFirestore db;
    private TextView bestScoreText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        leaderboardListView = findViewById(R.id.leaderboard_list_view);
        bestScoreText = findViewById(R.id.best_score_text);
        scoreItems = new ArrayList<>();
        adapter = new ScoreCardAdapter(this, scoreItems);
        leaderboardListView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadLeaderboard();
    }

    private void loadLeaderboard() {
        db.collection("scores")
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        scoreItems.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            String user = doc.getString("username");
                            Long score = doc.getLong("score");
                            Long time = doc.getLong("timestamp");

                            if (user != null && score != null && time != null) {
                                scoreItems.add(new ScoreItem(user, score.intValue(), time));
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

}