package com.example.tennis_padel;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class CourtAdapter extends RecyclerView.Adapter<CourtAdapter.CourtViewHolder> {

    private List<Court> courtList;

    public CourtAdapter(List<Court> courtList) {
        this.courtList = courtList;
    }

    @Override
    public CourtViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_court, parent, false);
        return new CourtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CourtViewHolder holder, int position) {
        Court court = courtList.get(position);
        holder.bind(court, holder.itemView.getContext());
    }

    @Override
    public int getItemCount() {
        return courtList.size();
    }

    public static class CourtViewHolder extends RecyclerView.ViewHolder {
        ImageView courtImage;
        MaterialTextView courtName;
        CardView cardView;


        public CourtViewHolder(View itemView) {
            super(itemView);
            courtImage = itemView.findViewById(R.id.court_image);
            courtName = itemView.findViewById(R.id.court_name);
            cardView = itemView.findViewById(R.id.cardView);
        }

        public void bind(Court court, Context context) {
            courtName.setText(court.getName());
            CourtStatus status = court.getStatus();
            switch (status) {
                case AVAILABLE:
                    cardView.setCardBackgroundColor(Color.parseColor("#B000962A"));
                    break;
                case RESERVED:
                    cardView.setCardBackgroundColor(Color.parseColor("#B0FFE604"));
                    break;
                case SEMI_RESERVED:
                    cardView.setCardBackgroundColor(Color.parseColor("#B0FF2626"));
                    break;
            }

            // Load the image based on CourtType
            String imageName = court.getType() == CourtType.OUTDOOR ? "outdoor.jpg" : "indoor.jpg";
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imageName);

            // Use Glide to load the image from Firebase Storage
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(context).load(uri).into(courtImage);
            });
        }

    }
}
