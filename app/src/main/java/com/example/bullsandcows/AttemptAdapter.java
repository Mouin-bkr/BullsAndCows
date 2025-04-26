package com.example.bullsandcows;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AttemptAdapter extends RecyclerView.Adapter<AttemptAdapter.AttemptViewHolder> {
    private final List<String> attempts;

    public AttemptAdapter(List<String> attempts) {
        this.attempts = attempts;
    }

    @NonNull
    @Override
    public AttemptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attempt, parent, false);
        return new AttemptViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttemptViewHolder holder, int position) {
        holder.textView.setText(attempts.get(position));
    }

    @Override
    public int getItemCount() {
        return attempts.size();
    }

    static class AttemptViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        AttemptViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.attempt_text);
        }
    }
}

