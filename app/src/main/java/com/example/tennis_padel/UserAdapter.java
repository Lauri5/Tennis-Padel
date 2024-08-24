package com.example.tennis_padel;

import android.content.Context;
import android.view.LayoutInflater;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    private List<User> userList, filteredUserList;
    private Context context;
    private static boolean isRank;
    private OnItemClickListener listener;

    public UserAdapter(Context context, List<User> userList, OnItemClickListener listener, boolean isRank) {
        this.context = context;
        this.userList = userList != null ? userList : new ArrayList<>(); // Ensure userList is not null
        this.filteredUserList = new ArrayList<>(this.userList);
        this.listener = listener;
        this.isRank = isRank;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (!isRank)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        else
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rank, parent, false);

        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = filteredUserList.get(position);

        if (!isRank) {
            // if no name is provided, remove white space from last name
            if (user.getName().isEmpty() && !user.getLastName().isEmpty()){
                holder.nameTextView.setText(user.getLastName().trim());
            }else{
                holder.nameTextView.setText(user.getName());
                holder.lastNameTextView.setText(user.getLastName());
            }
        } else {
            // if no name is provided, remove white space from last name
            if (user.getName().isEmpty() && !user.getLastName().isEmpty())
                holder.nameTextView.setText(user.getLastName().trim());
            else
                holder.nameTextView.setText(user.getName() + " " + user.getLastName());
            holder.lastNameTextView.setText(String.valueOf(user.getRatingRank()));
        }
        Glide.with(context)
                .load(user.getProfilePicture())
                .apply(RequestOptions.circleCropTransform())
                .into(holder.imageView);

        // Set click listener on the itemView
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(user);
            }
        });

        // If no name or last name is provided, set the email as the name
        if (user.getName().isEmpty() && user.getLastName().isEmpty()){
            String[] email = user.getEmail().split("@");
            holder.nameTextView.setText(email[0]);
        }
    }

    @Override
    public int getItemCount() {
        if (filteredUserList == null) {
            return 0;
        }
        return filteredUserList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView nameTextView, lastNameTextView;
        ImageView imageView;


        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            if (!isRank){
                nameTextView = itemView.findViewById(R.id.nameSearch);
                lastNameTextView = itemView.findViewById(R.id.lastNameSearch);
                imageView = itemView.findViewById(R.id.imageSearch);
            }else{

                nameTextView = itemView.findViewById(R.id.user_name);
                imageView = itemView.findViewById(R.id.imageRank);
                lastNameTextView = itemView.findViewById(R.id.user_rank);
            }
        }
    }

    // Update the list of users
    public void setUserList(List<User> userList) {
        this.userList = userList != null ? userList : new ArrayList<>();
        this.filteredUserList = new ArrayList<>(this.userList);
        notifyDataSetChanged();
    }

    public void sortUserList(List<User> userList) {
        Collections.sort(userList, (u1, u2) -> Float.compare(u2.getRatingRank(), u1.getRatingRank()));
        this.userList = userList;
        this.filteredUserList = new ArrayList<>(this.userList);
        notifyDataSetChanged();
    }

    // Filter the list based on the query
    public void filter(String query) {
        filteredUserList.clear();
        if (query.isEmpty()) {
            filteredUserList.addAll(userList);
        } else {
            query = query.toLowerCase();
            for (User user : userList) {
                if (user.getName().toLowerCase().contains(query) ||
                        user.getLastName().toLowerCase().contains(query)) {
                    filteredUserList.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }
}
