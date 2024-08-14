package com.example.tennis_padel;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterViewModel extends ViewModel {
    private FirebaseAuth mAuth;
    public MutableLiveData<Boolean> userRegistered = new MutableLiveData<>();
    public MutableLiveData<Boolean> registrationFailed = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public RegisterViewModel() {
        mAuth = FirebaseAuth.getInstance();
    }

    public void registerUser(String email, String password) {
        isLoading.setValue(true);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    isLoading.setValue(false);
                    if (task.isSuccessful()) {
                        userRegistered.setValue(true);
                    } else {
                        registrationFailed.setValue(true);
                    }
                });
    }
}
