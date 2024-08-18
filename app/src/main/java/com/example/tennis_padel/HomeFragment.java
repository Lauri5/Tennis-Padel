package com.example.tennis_padel;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView courtRecyclerView;
    private RecyclerView rankTableRecyclerView;
    private CourtAdapter courtAdapter;
    private RankTableAdapter rankTableAdapter;
    private List<Court> courtList = new ArrayList<>();
    private List<User> rankList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firestore and Firebase Storage
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerViews
        courtRecyclerView = view.findViewById(R.id.courtView);
        rankTableRecyclerView = view.findViewById(R.id.rankTableView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);

        // Set up the layout managers
        courtRecyclerView.setLayoutManager(gridLayoutManager);
        rankTableRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up adapters
        courtAdapter = new CourtAdapter(courtList);
        rankTableAdapter = new RankTableAdapter(rankList);

        // Attach adapters to RecyclerViews
        courtRecyclerView.setAdapter(courtAdapter);
        rankTableRecyclerView.setAdapter(rankTableAdapter);

        // Load data from Firestore
        loadCourtData();
        loadRankData();

        return view;
    }

    private void loadCourtData() {
        db.collection("courts")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        courtList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String typeStr = document.getString("type");
                            String statStr = document.getString("status");
                            CourtStatus status = getStatusFromString(statStr);
                            CourtType type = typeStr.equals("OUTDOOR") ? CourtType.OUTDOOR : CourtType.INDOOR;

                            Court court = new Court(name, type, status);
                            courtList.add(court);
                        }
                        courtAdapter.notifyDataSetChanged();
                    }
                });
    }

    private CourtStatus getStatusFromString(String statusStr) {
        if (statusStr.equals("AVAILABLE")) {
            return CourtStatus.AVAILABLE;
        } else if (statusStr.equals("RESERVED")) {
            return CourtStatus.RESERVED;
        }else{
            return CourtStatus.SEMI_RESERVED;
        }
    }

    private void loadRankData() {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        rankList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String profilePictureUrl = document.getString("profilePicture");
                            String lastName = document.getString("lastName");
                            int wins = ((Number) document.get("wins")).intValue();
                            int losses = ((Number) document.get("losses")).intValue();

                            User user = new User();
                            user.setName(name);
                            user.setLastName(lastName);
                            user.setProfilePicture(profilePictureUrl);
                            user.setWins(wins);
                            user.setLosses(losses);
                            rankList.add(user);
                        }

                        // Now sort the list by rank in descending order
                        Collections.sort(rankList, (u1, u2) -> Float.compare(u2.getRatingRank(), u1.getRatingRank())); // Sorting by rank, highest first

                        rankTableAdapter.notifyDataSetChanged();
                    }
                });
    }
}
