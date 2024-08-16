package com.example.tennis_padel;

import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainViewModel extends ViewModel {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

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
}