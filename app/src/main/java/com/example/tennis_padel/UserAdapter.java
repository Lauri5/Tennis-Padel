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
        // Retrieve the user at the given position from the filtered list
        User user = filteredUserList.get(position);

        // Extract the name and last name to handle possible null values safely
        String name = user.getName();
        String lastName = user.getLastName();

        // Condition to check and set the user's display name and last name based on the rank flag
        if (!isRank) {
            // Handle the case where the name might be empty or null
            if ((name == null || name.isEmpty()) && lastName != null){
                // Remove whitespace and set last name as the display name if no first name is provided
                holder.nameTextView.setText(lastName.trim());
            } else {
                // Set the name and last name in their respective TextViews, handling nulls
                holder.nameTextView.setText(name != null ? name : "");
                holder.lastNameTextView.setText(lastName != null ? lastName : "");
            }
        } else {
            // Handle the case for ranked users differently
            if (name == null || name.isEmpty()) {
                // Set trimmed last name if name is absent
                holder.nameTextView.setText(lastName != null ? lastName.trim() : "");
            } else {
                // Concatenate name and last name for ranked users, handling nulls
                holder.nameTextView.setText((name + " " + (lastName != null ? lastName : "")).trim());
                holder.lastNameTextView.setText(String.valueOf(user.getRatingRank()));
            }
        }

        // Load and set the user's profile picture using Glide with circle crop transformation
        Glide.with(context)
                .load(user.getProfilePicture())
                .apply(RequestOptions.circleCropTransform())
                .into(holder.imageView);

        // Set a click listener on the entire user item view
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(user);
            }
        });

        // Fallback to displaying the username extracted from the email if both name and last name are absent
        if ((name == null || name.isEmpty()) && (lastName == null || lastName.isEmpty())){
            String email = user.getEmail();
            if (email != null) {
                // Split the email at "@" and use the first part as the display name
                String[] parts = email.split("@");
                holder.nameTextView.setText(parts[0]);
            }
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
