package com.example.tennis_padel;

import android.net.Uri;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileViewModel extends ViewModel {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private User user = UserDataRepository.getInstance().getUser();

    public User getUser() {
        return user;
    }

    public void updateUser(String name, String lastName, String bio, String profilePicture) {
        user.setName(name);
        user.setLastName(lastName);
        user.setBio(bio);
        user.setProfilePicture(profilePicture);
        if (user.getId() != null) {
            db.collection("users").document(user.getId())
                    .update("name", name, "lastName", lastName, "bio", bio, "profilePicture", profilePicture);
        }
    }

    public void uploadImageToFirebase(Uri imageUri, OnImageUploadListener listener) {
        if (imageUri != null && user != null) {
            StorageReference storageReference = storage.getReference("images/" + user.getId());
            storageReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        listener.onImageUploaded(downloadUrl);
                    })
            );
        }
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
    }

    public interface OnImageUploadListener {
        void onImageUploaded(String imageUrl);
    }
}