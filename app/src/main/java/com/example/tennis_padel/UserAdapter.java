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
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList, filteredUserList;
    private Context context;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList != null ? userList : new ArrayList<>(); // Ensure userList is not null
        this.filteredUserList = new ArrayList<>(this.userList);
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = filteredUserList.get(position);

        // Clear or set placeholder for the imageView to avoid showing incorrect images
        holder.imageView.setImageDrawable(null);  // Optional: Use a placeholder if needed

        holder.nameTextView.setText(user.getName());
        holder.lastNameTextView.setText(user.getLastName());

        // Correctly set the image for this user
        Glide.with(context)
                .load(user.getProfilePicture())
                .apply(RequestOptions.circleCropTransform())
                .into(holder.imageView);
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
            nameTextView = itemView.findViewById(R.id.nameSearch);
            lastNameTextView = itemView.findViewById(R.id.lastNameSearch);
            imageView = itemView.findViewById(R.id.imageSearch);
        }
    }

    // Update the list of users
    public void setUserList(List<User> userList) {
        this.userList = userList != null ? userList : new ArrayList<>();
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
                    Log.d("Filter", "Added user: " + user.getName() + " " + user.getLastName());
                }
            }
        }
        Log.d("Filter", "Total users found: " + filteredUserList.size());
        notifyDataSetChanged();
    }
}
