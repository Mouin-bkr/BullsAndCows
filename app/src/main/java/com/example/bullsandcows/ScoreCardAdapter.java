package com.example.bullsandcows;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScoreCardAdapter extends ArrayAdapter<ScoreItem> {

    private final Context context;
    private final List<ScoreItem> scores;

    public ScoreCardAdapter(Context context, List<ScoreItem> scores) {
        super(context, 0, scores);
        this.context = context;
        this.scores = scores;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ScoreItem item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_score_card, parent, false);
        }

        TextView usernameText = convertView.findViewById(R.id.score_username);
        TextView scoreValueText = convertView.findViewById(R.id.score_value);
        TextView icon = convertView.findViewById(R.id.score_icon);

        usernameText.setText(item.getUsername());

        // Format timestamp into a readable date
        String date = formatTimestamp(item.getTimestamp());
        scoreValueText.setText("Score: " + item.getScore() + " (" + date + ")");

        // Emoji based on leaderboard position
        String emoji = position == 0 ? "👑" : position == 1 ? "🥈" : position == 2 ? "🥉" : "🏅";
        icon.setText(emoji);

        return convertView;
    }

    private String formatTimestamp(long millis) {
        Date date = new Date(millis);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
        return sdf.format(date);
    }
}
