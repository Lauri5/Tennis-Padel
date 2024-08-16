package com.example.tennis_padel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainViewModel extends ViewModel {

    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();

    public void loadUserData(FirebaseUser firebaseUser) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(firebaseUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            User user = new User();
                            user.setId(document.getId());
                            user.setName(document.getString("name"));
                            user.setLastName(document.getString("lastName"));
                            user.setBio(document.getString("bio"));
                            user.setWins(document.getLong("wins").intValue());
                            user.setLosses(document.getLong("losses").intValue());
                            user.setRatingRank(document.getLong("ratingRank").intValue());
                            user.setRatingRep(document.getLong("ratingRep").floatValue());
                            user.setProfilePicture(document.getString("profilePicture"));
                            userLiveData.setValue(user);
                        }
                    }
                });
    }
}
