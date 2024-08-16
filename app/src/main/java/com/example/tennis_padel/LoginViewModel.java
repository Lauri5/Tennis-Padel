package com.example.tennis_padel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;

public class LoginViewModel extends ViewModel {
    private final FirebaseAuth mAuth;
    public MutableLiveData<Boolean> userAuthenticated = new MutableLiveData<>();
    public MutableLiveData<Boolean> authenticationFailed = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LoginViewModel() {
        mAuth = FirebaseAuth.getInstance();
    }

    // Login process
    public void loginUser(String email, String password) {
        isLoading.setValue(true); // Makes the progressbar appear
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            isLoading.setValue(false); // Makes the progressbar disappear
            if (task.isSuccessful()) {
                userAuthenticated.setValue(true);
            } else {
                authenticationFailed.setValue(true);
            }
        });
    }

    public boolean isUserLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }
}
