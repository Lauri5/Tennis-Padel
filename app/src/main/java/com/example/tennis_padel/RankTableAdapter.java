package com.example.tennis_padel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RankTableAdapter extends RecyclerView.Adapter<RankTableAdapter.RankViewHolder> {

    private List<User> rankList;

    public RankTableAdapter(List<User> rankList) {
        this.rankList = rankList;
    }

    @Override
    public RankViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rank, parent, false);
        return new RankViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RankViewHolder holder, int position) {
        User user = rankList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return rankList.size();
    }

    public static class RankViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        TextView userRank;

        public RankViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userRank = itemView.findViewById(R.id.user_rank);
        }

        public void bind(User user) {
            userName.setText(user.getName());
            userRank.setText(String.valueOf(user.getRatingRank()));
        }
    }
}
