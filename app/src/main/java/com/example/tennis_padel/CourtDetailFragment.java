package com.example.tennis_padel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;

public class CourtDetailFragment extends Fragment {

    private static final String ARG_COURT = "court";
    private static final String ARG_DATE_TIME = "dateTime";

    private Court court;
    private Date selectedDateTime;
    private FirebaseFirestore db;
    private MainViewModel viewModel;

    private TextView courtName, courtStatus;
    private MaterialButton joinButton;
    private ImageView courtImage;

    private UserAdapter userAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            court = (Court) getArguments().getSerializable(ARG_COURT);
            selectedDateTime = (Date) getArguments().getSerializable(ARG_DATE_TIME);
        }
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_court_detail, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        androidx.appcompat.widget.SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false); // Make sure it's fully expanded

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userAdapter = new UserAdapter(getContext(), null, this::inviteUser); // Initialize with an empty list
        recyclerView.setAdapter(userAdapter);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // Observe all users data
        viewModel.getAllUsersLiveData().observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                userAdapter.setUserList(users);
            }
        });

        // Filter the list as the user types in the search bar
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                userAdapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                userAdapter.filter(newText);
                return false;
            }
        });

        // Load all users data
        viewModel.loadAllUsers();

        courtName = view.findViewById(R.id.court_name);
        joinButton = view.findViewById(R.id.join_button);
        courtImage = view.findViewById(R.id.court_image);
        courtStatus = view.findViewById(R.id.court_status);

        courtStatus.setText("There are currently "+court.getReservations().size()+"/4 players in this court.");
        courtName.setText(court.getName());
        joinButton.setOnClickListener(v -> joinCourt());

        String typeStr = court.getType().toString();
        String imageName;
        switch (typeStr){
            case "TENNIS_INDOOR":
                imageName = "indoor.jpg";
                break;
            case "PADEL_OUTDOOR":
                imageName = "padel_outdoor.jpg";
                break;
            case "PADEL_INDOOR":
                imageName = "padel_indoor.jpg";
                break;
            default:
                imageName = "outdoor.jpg";
                break;
        }
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imageName);

        // Use Glide to load the image from Firebase Storage
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(requireContext()).load(uri).into(courtImage);
        });

        return view;
    }

    private void inviteUser(User user) {
        // Create a new instance of OtherProfileFragment
        Toast.makeText(requireContext(), "User " + user.getName() + " " + user.getLastName() + " invited", Toast.LENGTH_SHORT).show();
    }

    public static CourtDetailFragment newInstance(Court court, Date dateTime) {
        CourtDetailFragment fragment = new CourtDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_COURT, court);
        args.putSerializable(ARG_DATE_TIME, dateTime);
        fragment.setArguments(args);
        return fragment;
    }

    private void joinCourt() {
        // Assume current user is fetched or passed somehow
        FirebaseUser currentUser = viewModel.getCurrentUser(); // Implement this method to get the current user

        Reservation reservation = findOrCreateReservation();
        if (reservation != null) {
            reservation.addPlayer(currentUser.getUid());

            // Update court status based on number of players
            if (reservation.isFull()) {
                court.setStatus(CourtStatus.RESERVED);
            } else {
                court.setStatus(CourtStatus.SEMI_RESERVED);
            }

            // Update Firestore with the new status and reservation data
            db.collection("courts").document(court.getId())
                    .update("reservations", court.getReservations(), "status", court.getStatus().toString())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Joined the court!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to join court: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private Reservation findOrCreateReservation() {
        for (Reservation reservation : court.getReservations()) {
            if (reservation.getDateTime().equals(selectedDateTime)) {
                return reservation;
            }
        }

        // Create a new reservation if one doesn't exist for the selected date/time
        Reservation newReservation = new Reservation(
                db.collection("reservations").document().getId(),  // Use Firestore to generate an ID
                court,
                selectedDateTime,
                false  // Assuming it's not a lesson
        );

        court.addReservation(newReservation);
        return newReservation;
    }
}
