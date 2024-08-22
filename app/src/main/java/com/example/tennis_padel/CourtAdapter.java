package com.example.tennis_padel;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.List;

public class CourtAdapter extends RecyclerView.Adapter<CourtAdapter.CourtViewHolder> {

    private List<Court> courtList;
    private FragmentManager fragmentManager;
    private Calendar selectedDateTime;

    public CourtAdapter(List<Court> courtList, FragmentManager fragmentManager, Calendar selectedDateTime) {
        this.courtList = courtList;
        this.fragmentManager = fragmentManager;
        this.selectedDateTime = selectedDateTime;
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
        holder.bind(court, holder.itemView.getContext(), fragmentManager, selectedDateTime);
    }

    @Override
    public int getItemCount() {
        return courtList.size();
    }

    public static class CourtViewHolder extends RecyclerView.ViewHolder {
        ImageView courtImage;
        TextView courtName;
        CardView cardView;

        public CourtViewHolder(View itemView) {
            super(itemView);
            courtImage = itemView.findViewById(R.id.court_image);
            courtName = itemView.findViewById(R.id.court_name);
            cardView = itemView.findViewById(R.id.cardView);
        }

        public void bind(Court court, Context context, FragmentManager fragmentManager, Calendar selectedDateTime) {
            courtName.setText(court.getName());
            CourtStatus status = court.getStatus();

            // Set background color based on court status
            switch (status) {
                case AVAILABLE:
                    cardView.setCardBackgroundColor(Color.parseColor("#B000962A"));  // Example color for AVAILABLE
                    break;
                case RESERVED:
                    cardView.setCardBackgroundColor(Color.parseColor("#B0FFE604"));  // Example color for RESERVED
                    break;
                case SEMI_RESERVED:
                    cardView.setCardBackgroundColor(Color.parseColor("#B0FF2626"));  // Example color for SEMI_RESERVED
                    break;
            }

            // Set click listener to open CourtDetailFragment
            cardView.setOnClickListener(v -> {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_container, CourtDetailFragment.newInstance(court, selectedDateTime.getTime()));
                transaction.addToBackStack(null);
                transaction.commit();
            });

            // Load the image based on CourtType
            String imageName;
            switch (court.getType()) {
                case TENNIS_INDOOR:
                    imageName = "indoor.jpg";
                    break;
                case TENNIS_OUTDOOR:
                    imageName = "outdoor.jpg";
                    break;
                case PADEL_INDOOR:
                    imageName = "padel_indoor.jpg";
                    break;
                case PADEL_OUTDOOR:
                    imageName = "padel_outdoor.jpg";
                    break;
                default:
                    imageName = "indoor.jpg";
                    break;
            }
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imageName);

            // Use Glide to load the image from Firebase Storage
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(context).load(uri).into(courtImage);
            });
        }
    }
}
