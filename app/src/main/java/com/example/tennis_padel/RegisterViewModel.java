package com.example.tennis_padel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterViewModel extends ViewModel {
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    public MutableLiveData<Boolean> userRegistered = new MutableLiveData<>();
    public MutableLiveData<Boolean> registrationFailed = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public RegisterViewModel() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    // Register process
    public void registerUser(String email, String password) {
        isLoading.setValue(true);
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            isLoading.setValue(false);
            if (task.isSuccessful()) {
                userRegistered.setValue(true);
                String id = mAuth.getCurrentUser().getUid();
                User user = new User(mAuth.getCurrentUser().getUid(), email);
                db.collection("users") // Replace "users" with your collection name
                        .document(id).set(user).addOnSuccessListener(aVoid -> userRegistered.setValue(true)).addOnFailureListener(e -> registrationFailed.setValue(true));
            } else {
                registrationFailed.setValue(true);
            }
        });
    }
}
