package com.example.tennis_padel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.textview.MaterialTextView;

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
        holder.bind(user, holder.itemView.getContext());
    }

    @Override
    public int getItemCount() {
        return rankList.size();
    }

    public static class RankViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView userName, userRank;
        ImageView profilePicture;

        public RankViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userRank = itemView.findViewById(R.id.user_rank);
            profilePicture = itemView.findViewById(R.id.imageRank);
        }

        public void bind(User user, Context context) {
            userName.setText(user.getName() + " " + user.getLastName());
            userRank.setText(String.valueOf(user.getRatingRank()));

            Glide.with(context)
                    .load(user.getProfilePicture())
                    .apply(RequestOptions.circleCropTransform())
                    .into(profilePicture);
        }
    }
}
