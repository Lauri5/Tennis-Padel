package com.example.tennis_padel;

import android.net.Uri;

import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.nio.charset.StandardCharsets;

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
            db.collection("users").document(user.getId()).update("name", name, "lastName", lastName, "bio", bio, "profilePicture", profilePicture);
        }
    }

    public void updateLabel(String label) {
        byte[] data = label.getBytes(StandardCharsets.UTF_8);

        StorageReference fileRef = storage.getReference().child("title.txt");

        UploadTask uploadTask = fileRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> UserDataRepository.getInstance().getClub().setClubName(label));
    }

    public void updateImage(String image) {
        storage.getReference("admin.jpg").child(image);
        UserDataRepository.getInstance().getClub().setClubLogo(image);
    }

    public void uploadImageToFirebase(Uri imageUri, OnImageUploadListener listener) {
        if (imageUri != null && user != null) {
            StorageReference storageReference = storage.getReference("images/" + user.getId());
            storageReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                listener.onImageUploaded(downloadUrl);
            }));
        }
    }

    public void uploadImageLabelToFirebase(Uri imageUri, OnImageUploadListener listener) {
        if (imageUri != null && user != null) {
            StorageReference storageReference = storage.getReference("admin.jpg");
            storageReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                listener.onImageUploaded(downloadUrl);
            }));
        }
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
        user = null;
    }

    public interface OnImageUploadListener {
        void onImageUploaded(String imageUrl);
    }
}