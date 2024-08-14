package com.example.tennis_padel;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;

public class LoginViewModel extends ViewModel {
    private FirebaseAuth mAuth;
    public MutableLiveData<Boolean> userAuthenticated = new MutableLiveData<>();
    public MutableLiveData<Boolean> authenticationFailed = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LoginViewModel() {
        mAuth = FirebaseAuth.getInstance();
    }

    public void loginUser(String email, String password) {
        isLoading.setValue(true);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            isLoading.setValue(false);
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
