package com.example.tennis_padel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<Invitation> invitations;
    private final OnAcceptClickListener acceptClickListener;
    private final OnDeclineClickListener declineClickListener;

    public NotificationAdapter(List<Invitation> invitations, OnAcceptClickListener acceptClickListener, OnDeclineClickListener declineClickListener) {
        this.invitations = invitations;
        this.acceptClickListener = acceptClickListener;
        this.declineClickListener = declineClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Invitation invitation = invitations.get(position);
        holder.bind(invitation, acceptClickListener, declineClickListener);
    }

    @Override
    public int getItemCount() {
        return invitations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView invitationDetails;
        private final MaterialButton acceptButton, declineButton;
        private final ImageView courtImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            invitationDetails = itemView.findViewById(R.id.invitation_details);
            acceptButton = itemView.findViewById(R.id.accept_button);
            declineButton = itemView.findViewById(R.id.decline_button);
            courtImage = itemView.findViewById(R.id.court_image);
        }

        public void bind(Invitation invitation, OnAcceptClickListener acceptClickListener, OnDeclineClickListener declineClickListener) {
            invitationDetails.setText("Court: " + invitation.getCourtName() + "\nTime: " + invitation.getTime());

            // This allows to chose the correct image based on the CourtType
            String imageName;
            switch (invitation.getCourtType()) {
                case "TENNIS_OUTDOOR":
                    imageName = "outdoor.jpg";
                    break;
                case "PADEL_INDOOR":
                    imageName = "padel_indoor.jpg";
                    break;
                case "PADEL_OUTDOOR":
                    imageName = "padel_outdoor.jpg";
                    break;
                default:
                    imageName = "indoor.jpg";
                    break;
            }

            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imageName);

            // Use Glide to load the image from Firebase Storage
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(itemView.getContext()).load(uri).circleCrop().into(courtImage);
            });

            if ("full".equals(invitation.getStatus())) {
                acceptButton.setVisibility(View.GONE);
                declineButton.setText("Got it!");
            } else {
                acceptButton.setVisibility(View.VISIBLE);
                declineButton.setText("Decline");
            }

            acceptButton.setOnClickListener(v -> acceptClickListener.onAcceptClick(invitation));
            declineButton.setOnClickListener(v -> declineClickListener.onDeclineClick(invitation));
        }
    }

    public interface OnAcceptClickListener {
        void onAcceptClick(Invitation invitation);
    }

    public interface OnDeclineClickListener {
        void onDeclineClick(Invitation invitation);
    }
}
