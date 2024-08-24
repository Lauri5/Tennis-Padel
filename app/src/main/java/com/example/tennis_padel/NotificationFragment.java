package com.example.tennis_padel;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private FirebaseFirestore db;
    private NotificationAdapter notificationAdapter;
    private List<Invitation> invitationsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.notification_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        invitationsList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(invitationsList, this::onAcceptClicked, this::onDeclineClicked);
        recyclerView.setAdapter(notificationAdapter);

        db = FirebaseFirestore.getInstance();
        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("invitations")
                .whereEqualTo("inviteeId", currentUserId)
                .whereEqualTo("status", "notified")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        invitationsList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Invitation invitation = document.toObject(Invitation.class);

                            // Set the Firestore document ID
                            invitation.setId(document.getId());

                            // Debugging log to check courtId
                            Log.d("NotificationFragment", "Court ID: " + invitation.getCourtId());

                            invitationsList.add(invitation);
                        }
                        notificationAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("NotificationFragment", "Error getting invitations: ", task.getException());
                    }
                });
    }

    private void onAcceptClicked(Invitation invitation) {
        // Ensure the invitation ID is not null
        if (invitation.getId() == null) {
            Log.e("NotificationFragment", "Invitation ID is null");
            Toast.makeText(getContext(), "Error: Invitation ID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        // Remove the invitation from Firestore
        db.collection("invitations").document(invitation.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Add the user to the court's reservation
                    addReservation(invitation);
                    Toast.makeText(getContext(), "Invitation accepted", Toast.LENGTH_SHORT).show();
                    invitationsList.remove(invitation);
                    notificationAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("NotificationFragment", "Error deleting invitation: ", e);
                    Toast.makeText(getContext(), "Failed to accept invitation", Toast.LENGTH_SHORT).show();
                });
    }

    private void onDeclineClicked(Invitation invitation) {
        // Ensure the invitation ID is not null
        if (invitation.getId() == null) {
            Log.e("NotificationFragment", "Invitation ID is null");
            Toast.makeText(getContext(), "Error: Invitation ID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        // Remove the invitation from Firestore
        db.collection("invitations").document(invitation.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Invitation declined", Toast.LENGTH_SHORT).show();
                    invitationsList.remove(invitation);
                    notificationAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("NotificationFragment", "Error deleting invitation: ", e);
                    Toast.makeText(getContext(), "Failed to decline invitation", Toast.LENGTH_SHORT).show();
                });
    }

    private void addReservation(Invitation invitation) {
        // Check if the courtId is null
        if (invitation.getCourtId() == null) {
            Log.e("NotificationFragment", "Error: Court ID is null");
            Toast.makeText(getContext(), "Error: Court ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        String formattedDateTime = invitation.getTime();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String reservationId = userId + "-" + formattedDateTime;

        Reservation newReservation = new Reservation(
                reservationId,
                invitation.getCourtId(),
                formattedDateTime,
                false, // Assuming this means "is not a lesson"
                userId
        );

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Start a Firestore batch operation
        WriteBatch batch = db.batch();

        // Reference to the court document
        DocumentReference courtRef = db.collection("courts").document(invitation.getCourtId());
        courtRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Court court = documentSnapshot.toObject(Court.class);
                if (court != null) {
                    // Initialize the reservations list if it's null
                    if (court.getReservations() == null) {
                        court.setReservations(new ArrayList<>());
                    }
                    // Add the new reservation to the court's reservation list
                    court.getReservations().add(newReservation);
                    batch.set(courtRef, court);
                }
            }

            // Reference to the user document
            DocumentReference userRef = db.collection("users").document(userId);
            userRef.get().addOnSuccessListener(userSnapshot -> {
                if (userSnapshot.exists()) {
                    User user = userSnapshot.toObject(User.class);
                    if (user != null) {
                        // Initialize the reservations list if it's null
                        if (user.getReservations() == null) {
                            user.setReservations(new ArrayList<>());
                        }
                        // Add the new reservation to the user's reservation list
                        user.getReservations().add(newReservation);
                        batch.set(userRef, user);
                    }
                }

                // Commit the batch operation
                batch.commit().addOnSuccessListener(aVoid -> {
                    Log.d("NotificationFragment", "Reservation added successfully to both court and user.");
                    Toast.makeText(getContext(), "Reservation added successfully!", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Log.e("NotificationFragment", "Error adding reservation: ", e);
                    Toast.makeText(getContext(), "Failed to add reservation", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Log.e("NotificationFragment", "Error fetching user: ", e);
                Toast.makeText(getContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Log.e("NotificationFragment", "Error fetching court: ", e);
            Toast.makeText(getContext(), "Failed to fetch court data", Toast.LENGTH_SHORT).show();
        });
    }
}
