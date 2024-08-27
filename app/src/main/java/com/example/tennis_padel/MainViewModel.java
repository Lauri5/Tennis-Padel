package com.example.tennis_padel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.List;

public class MainViewModel extends ViewModel {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<List<User>> allUsersLiveData = new MutableLiveData<>();

    // LiveData to observe user changes
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void loadUserData() {
        FirebaseUser firebaseUser = getCurrentUser();
        if (firebaseUser != null) {
            db.collection("users").document(firebaseUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Handle user data received
                            UserDataRepository.getInstance().setUser(task.getResult().toObject(User.class));
                        }
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
        db.collection("users").whereIn("role", allowedRoles).get()
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