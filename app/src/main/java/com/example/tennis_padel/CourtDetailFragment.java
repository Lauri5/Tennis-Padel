package com.example.tennis_padel;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CourtDetailFragment extends Fragment {
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
            court = (Court) getArguments().getSerializable("court");
            selectedDateTime = (Date) getArguments().getSerializable("dateTime");
        }
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_court_detail, container, false);
        initializeUI(view);

        androidx.appcompat.widget.SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false);
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
        loadAllUsers();
        loadCourtDetails();
        joinButton.setOnClickListener(v -> joinCourt());
        return view;
    }

    private void initializeUI(View view) {
        courtName = view.findViewById(R.id.court_name);
        joinButton = view.findViewById(R.id.join_button);
        courtImage = view.findViewById(R.id.court_image);
        courtStatus = view.findViewById(R.id.court_status);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter = new UserAdapter(getContext(), new ArrayList<>(), user -> inviteUser(user, court), true);
        recyclerView.setAdapter(userAdapter);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        if (court != null) {
            courtName.setText(court.getName());
            loadImage();
        }
    }

    private void loadImage() {
        String imageName = getImageName(court.getType().toString());
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imageName);
        storageRef.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(requireContext()).load(uri).into(courtImage));
    }

    private void loadAllUsers() {
        viewModel.loadAllUsers();
        viewModel.getAllUsersLiveData().observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                userAdapter.sortUserList(users);
            }
        });
    }

    private void loadCourtDetails() {
        String formattedDateTime = formatDateTime(selectedDateTime);

        db.collection("courts").document(court.getId()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                Court court = task.getResult().toObject(Court.class);
                if (court != null) {
                    int playerCount = 0;

                    for (Reservation reservation : court.getReservations()) {
                        if (reservation.getDateTime().equals(formattedDateTime)) {
                            playerCount++;
                        }
                    }

                    courtStatus.setText("There are currently " + playerCount + "/4 players in this court.");
                }
            } else {
                Toast.makeText(getContext(), "Failed to load court details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void joinCourt() {
        FirebaseUser firebaseUser = viewModel.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(getContext(), "You need to be logged in to join a court.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = firebaseUser.getUid();
        String reservationId = generateReservationId(userId, selectedDateTime);

        db.collection("reservations").whereEqualTo("id", reservationId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().isEmpty()) {
                createReservation(reservationId);
            } else {
                Toast.makeText(getContext(), "You already have a reservation at this time.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createReservation(String reservationId) {
        String formattedDateTime = formatDateTime(selectedDateTime);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.runTransaction(transaction -> {
            DocumentReference courtRef = db.collection("courts").document(court.getId());
            Court court = transaction.get(courtRef).toObject(Court.class);

            DocumentReference userRef = db.collection("users").document(userId);
            User user = transaction.get(userRef).toObject(User.class);

            if (court != null && user != null) {
                List<Reservation> reservations = court.getReservations();
                if (reservations == null) {
                    reservations = new ArrayList<>();
                }

                List<Reservation> userReservations = user.getReservations();
                if (userReservations == null) {
                    userReservations = new ArrayList<>();
                }

                // Check if the user already has a reservation at the selected time
                for (Reservation reservation : userReservations) {
                    if (reservation.getDateTime().equals(formattedDateTime) && reservation.getCourtId().equals(court.getId())) {
                        throw new FirebaseFirestoreException("User already has a reservation at this time.", FirebaseFirestoreException.Code.ABORTED);
                    }
                }

                Reservation newReservation = new Reservation(reservationId, court.getId(), formattedDateTime, false, userId);

                reservations.add(newReservation);
                userReservations.add(newReservation);

                transaction.update(courtRef, "reservations", reservations);
                transaction.update(userRef, "reservations", userReservations);

                // If the court is full, send notifications to all players
                if (reservations.size() == 4) {
                    for (Reservation reservation : reservations) {
                        Invitation fullReservationNotification = new Invitation();
                        fullReservationNotification.setCourtId(court.getId());
                        fullReservationNotification.setCourtName(court.getName());
                        fullReservationNotification.setTime(formattedDateTime);
                        fullReservationNotification.setCourtType(court.getType().toString());
                        fullReservationNotification.setInviterId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        fullReservationNotification.setInviteeId(reservation.getPlayer());
                        fullReservationNotification.setStatus("full");

                        transaction.set(db.collection("invitations").document(), fullReservationNotification);
                    }
                }

            } else {
                throw new FirebaseFirestoreException("Court or User not found.", FirebaseFirestoreException.Code.ABORTED);
            }

            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("CourtDetailFragment", "Reservation added to user and court successfully.");
            Toast.makeText(getContext(), "Reservation successful!", Toast.LENGTH_SHORT).show();
            loadCourtDetails();
        }).addOnFailureListener(e -> {
            Log.e("CourtDetailFragment", "Failed to add reservation: " + e.getMessage());
            Toast.makeText(getContext(), "Failed to make reservation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private String generateReservationId(String userId, Date dateTime) {
        return userId + "-" + formatDateTime(dateTime);
    }

    private String formatDateTime(Date dateTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd-HH", Locale.getDefault());
        return dateFormat.format(dateTime);
    }

    private String getImageName(String typeStr) {
        switch (typeStr) {
            case "TENNIS_INDOOR":
                return "indoor.jpg";
            case "PADEL_OUTDOOR":
                return "padel_outdoor.jpg";
            case "PADEL_INDOOR":
                return "padel_indoor.jpg";
            default:
                return "outdoor.jpg";
        }
    }

    private void inviteUser(User user, Court court) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String formattedDateTime = formatDateTime(selectedDateTime);

        if (user.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            Toast.makeText(getContext(), "You cannot invite yourself.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(user.getId()).get().addOnSuccessListener(documentSnapshot -> {
            User existingUser = documentSnapshot.toObject(User.class);
            if (existingUser != null) {
                boolean hasReservation = false;
                if (existingUser.getReservations() != null) {
                    for (Reservation reservation : existingUser.getReservations()) {
                        if (reservation.getDateTime().equals(formattedDateTime)) {
                            hasReservation = true;
                            break;
                        }
                    }
                }

                if (hasReservation) {
                    Toast.makeText(getContext(), "User already has a reservation at this time.", Toast.LENGTH_SHORT).show();
                    return;
                }

                db.collection("invitations").whereEqualTo("inviteeId", user.getId()).whereEqualTo("time", formattedDateTime).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().isEmpty()) {
                        sendInvitation(user, court, formattedDateTime);
                    } else {
                        Toast.makeText(getContext(), "User already has an invitation for this time.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "User data not found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error checking reservations.", Toast.LENGTH_SHORT).show();
            Log.e("CourtDetailFragment", "Error fetching user data", e);
        });
    }

    private void sendInvitation(User user, Court court, String formattedDateTime) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Invitation invitation = new Invitation();
        invitation.setCourtId(court.getId());
        invitation.setCourtName(court.getName());
        invitation.setTime(formattedDateTime);
        invitation.setCourtType(court.getType().toString());
        invitation.setInviterId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        invitation.setInviteeId(user.getId());
        invitation.setStatus("pending");

        db.collection("invitations").add(invitation).addOnSuccessListener(documentReference -> {
            Toast.makeText(getContext(), "Invitation sent to " + user.getName(), Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to send invitation", Toast.LENGTH_SHORT).show();
            Log.e("CourtDetailFragment", "Error sending invitation", e);
        });
    }

    public static CourtDetailFragment newInstance(Court court, Date dateTime) {
        CourtDetailFragment fragment = new CourtDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("court", court);
        args.putSerializable("dateTime", dateTime);
        fragment.setArguments(args);
        return fragment;
    }
}