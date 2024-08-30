package com.example.tennis_padel;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        db.collection("invitations").whereEqualTo("inviteeId", currentUserId).whereIn("status", Arrays.asList("notified", "full")).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                invitationsList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Invitation invitation = document.toObject(Invitation.class);

                    // Set the Firestore document ID
                    invitation.setId(document.getId());

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

        // Add the user to the court's reservation
        addReservation(invitation);

        // Remove the invitation from Firestore
        db.collection("invitations").document(invitation.getId()).delete().addOnSuccessListener(aVoid -> {
            if (!invitation.getStatus().equals("full"))
                Toast.makeText(getContext(), "Invitation declined", Toast.LENGTH_SHORT).show();

            invitationsList.remove(invitation);
            notificationAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Log.e("NotificationFragment", "Error deleting invitation: ", e);
            Toast.makeText(getContext(), "Failed to decline invitation", Toast.LENGTH_SHORT).show();
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
        db.collection("invitations").document(invitation.getId()).delete().addOnSuccessListener(aVoid -> {
            if (!invitation.getStatus().equals("full"))
                Toast.makeText(getContext(), "Invitation declined", Toast.LENGTH_SHORT).show();

            invitationsList.remove(invitation);
            notificationAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Log.e("NotificationFragment", "Error deleting invitation: ", e);
            Toast.makeText(getContext(), "Failed to decline invitation", Toast.LENGTH_SHORT).show();
        });
    }

    private void addReservation(Invitation invitation) {
        String formattedDateTime = invitation.getTime();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String reservationId = generateReservationId(userId, formattedDateTime);

        db.runTransaction(transaction -> {
            DocumentReference courtRef = db.collection("courts").document(invitation.getCourtId());
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
            Log.d("NotificationFragment", "Reservation added to user and court successfully.");
            Toast.makeText(getContext(), "Reservation successful!", Toast.LENGTH_SHORT).show();
            loadNotifications(); // Refresh notifications
        }).addOnFailureListener(e -> {
            Log.e("NotificationFragment", "Failed to add reservation: " + e.getMessage());
            Toast.makeText(getContext(), "Failed to make reservation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private String generateReservationId(String userId, String dateTime) {
        return userId + "-" + dateTime;
    }
}