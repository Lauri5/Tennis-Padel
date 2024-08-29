package com.example.tennis_padel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoginViewModel extends ViewModel {
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    public MutableLiveData<Boolean> userAuthenticated = new MutableLiveData<>();
    public MutableLiveData<Boolean> authenticationFailed = new MutableLiveData<>();
    public MutableLiveData<Boolean> isUserBanned = new MutableLiveData<>();
    public MutableLiveData<Boolean> isUserSuspended = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LoginViewModel() {
        mAuth = FirebaseAuth.getInstance();
    }

    // Login process
    public void loginUser(String email, String password) {
        isLoading.setValue(true); // Shows the progress bar
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                checkBanStatus();
            } else {
                isLoading.setValue(false); // Hides the progress bar
                authenticationFailed.setValue(true);
            }
        });
    }

    public void checkBanStatus() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(currentUserId).get().addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            if (user != null) {
                if (user.isBanned()) {
                    isUserBanned.postValue(true);
                    mAuth.signOut();
                } else if (user.isSuspended()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        Date suspensionEndDate = sdf.parse(user.getSuspensionEndDate());
                        Date currentDate = new Date();
                        if (suspensionEndDate != null && suspensionEndDate.after(currentDate)) {
                            isUserSuspended.postValue(true);
                            user.setSuspended(false);
                            mAuth.signOut();
                        } else {
                            userAuthenticated.postValue(true);
                        }
                    } catch (Exception e) {
                        Log.e("Date Parsing", "Error parsing suspension end date", e);
                        userAuthenticated.postValue(true); // Proceed if date parsing fails
                    }
                } else {
                    userAuthenticated.postValue(true);
                }
            } else {
                authenticationFailed.postValue(true);
            }
            isLoading.postValue(false);
        }).addOnFailureListener(e -> {
            authenticationFailed.postValue(true);
            isLoading.postValue(false);
            Log.e("LoginViewModel", "Failed to fetch user data: " + e.getMessage());
        });
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }
}