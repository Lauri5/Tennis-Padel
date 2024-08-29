package com.example.tennis_padel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainViewModel extends ViewModel {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<List<User>> allUsersLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isUserBannedLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LiveData<Boolean> getIsUserBannedLiveData() {
        return isUserBannedLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void checkUserBanStatus() {
        FirebaseUser firebaseUser = getCurrentUser();
        if (firebaseUser != null) {
            db.collection("users").document(firebaseUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            if (user.isBanned() || (user.isSuspended() && checkIfStillSuspended(user.getSuspensionEndDate()))) {
                                isUserBannedLiveData.setValue(true);
                            } else {
                                isUserBannedLiveData.setValue(false);
                            }
                        } else {
                            isUserBannedLiveData.setValue(false);
                        }
                    })
                    .addOnFailureListener(e -> isUserBannedLiveData.setValue(false));
        }
    }

    private boolean checkIfStillSuspended(String suspensionEndDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date endDate = sdf.parse(suspensionEndDate);
            return endDate != null && endDate.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // LiveData to observe user changes
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void loadUserData() {
        FirebaseUser firebaseUser = getCurrentUser();
        if (firebaseUser != null) {
            isLoading.setValue(true);  // Signal that loading has started
            db.collection("users").document(firebaseUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Handle user data received
                            UserDataRepository.getInstance().setUser(task.getResult().toObject(User.class));
                        }
                        isLoading.setValue(false);  // Signal that loading has finished
                    });
        }
    }

    public boolean userLoggedIn() {
        return getCurrentUser() != null;
    }

    // Method to fetch all users from Firestore
    public void loadAllUsers() {
        // Define an array of roles to filter by
        List<String> allowedRoles = Arrays.asList("STUDENT", "TEACHER");

        // Use the 'whereIn' clause to filter documents by roles
        db.collection("users")
                .whereIn("role", allowedRoles)
                .whereEqualTo("banned", false)
                .whereEqualTo("suspended", false)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        if (result != null) {
                            List<User> users = result.toObjects(User.class);
                            allUsersLiveData.setValue(users);
                        }
                    }
                });
    }

    // LiveData to observe the list of all users
    public LiveData<List<User>> getAllUsersLiveData() {
        return allUsersLiveData;
    }

    public void removePastReservations() {
        // Get the current date with time set to the start of the day (00:00)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        String currentDateStr = sdf.format(new Date());

        // Remove old reservations from users collection
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            if (user != null && user.getReservations() != null) {
                                ArrayList<Reservation> reservationsToRemove = new ArrayList<>();
                                for (Reservation reservation : user.getReservations()) {
                                    String reservationDateStr = reservation.getDateTime().split("-")[0]; // Extract the date part
                                    try {
                                        Date reservationDate = sdf.parse(reservationDateStr);
                                        Date currentDate = sdf.parse(currentDateStr);
                                        if (reservationDate != null && reservationDate.before(currentDate)) {
                                            reservationsToRemove.add(reservation); // Mark reservation for deletion
                                        }
                                    } catch (Exception e) {
                                        Log.e("MainViewModel", "Error parsing reservation date", e);
                                    }
                                }
                                if (!reservationsToRemove.isEmpty()) {
                                    user.getReservations().removeAll(reservationsToRemove);
                                    db.collection("users").document(user.getId())
                                            .update("reservations", user.getReservations())
                                            .addOnSuccessListener(aVoid -> Log.d("MainViewModel", "Old reservations removed from user " + user.getId()))
                                            .addOnFailureListener(e -> Log.e("MainViewModel", "Error removing reservations for user " + user.getId(), e));
                                }
                            }
                        }
                    } else {
                        Log.e("MainViewModel", "Error getting users documents: ", task.getException());
                    }
                });

        // Remove old reservations from courts collection
        db.collection("courts")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            Court court = document.toObject(Court.class);
                            if (court != null && court.getReservations() != null) {
                                ArrayList<Reservation> reservationsToRemove = new ArrayList<>();
                                for (Reservation reservation : court.getReservations()) {
                                    String reservationDateStr = reservation.getDateTime().split("-")[0]; // Extract the date part
                                    try {
                                        Date reservationDate = sdf.parse(reservationDateStr);
                                        Date currentDate = sdf.parse(currentDateStr);
                                        if (reservationDate != null && reservationDate.before(currentDate)) {
                                            reservationsToRemove.add(reservation); // Mark reservation for deletion
                                        }
                                    } catch (Exception e) {
                                        Log.e("MainViewModel", "Error parsing reservation date", e);
                                    }
                                }
                                if (!reservationsToRemove.isEmpty()) {
                                    court.getReservations().removeAll(reservationsToRemove);
                                    db.collection("courts").document(court.getId())
                                            .update("reservations", court.getReservations())
                                            .addOnSuccessListener(aVoid -> Log.d("MainViewModel", "Old reservations removed from court " + court.getId()))
                                            .addOnFailureListener(e -> Log.e("MainViewModel", "Error removing reservations for court " + court.getId(), e));
                                }
                            }
                        }
                    } else {
                        Log.e("MainViewModel", "Error getting courts documents: ", task.getException());
                    }
                });
    }
}