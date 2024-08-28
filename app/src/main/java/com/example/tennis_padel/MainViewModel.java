package com.example.tennis_padel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
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
}