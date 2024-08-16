package com.example.tennis_padel;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


public class ProfileFragment extends Fragment {

    private TextInputEditText name;
    private TextInputEditText lastName;
    private TextInputEditText bio;
    private User user;
    private FirebaseFirestore db;
    private boolean isEditMode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        user = UserDataRepository.getInstance().getUser();
        loadUserProfile();
        MaterialButton editButton = view.findViewById(R.id.edit);
        MaterialButton logoutButton = view.findViewById(R.id.logout);

        isEditMode = false;
        updateEditMode();

        // Edit button
        editButton.setOnClickListener(view1 -> {
            isEditMode = !isEditMode;
            editButton.setText(isEditMode ? "Save" : "Edit");
            updateEditMode();

            if (!isEditMode){
                user.setName(name.getText().toString().trim());
                user.setLastName(lastName.getText().toString().trim());
                user.setBio(bio.getText().toString().trim());

                updateUserInDB();
            }
        });

        // Logout button
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(requireContext(), Login.class));
            requireActivity().finish();
        });
    }

    private void updateEditMode() {
        name.setFocusable(isEditMode);
        name.setFocusableInTouchMode(isEditMode);
        lastName.setFocusable(isEditMode);
        lastName.setFocusableInTouchMode(isEditMode);
        bio.setFocusable(isEditMode);
        bio.setFocusableInTouchMode(isEditMode);
    }

    private void updateUserInDB() {
        if (user.getId() != null) {
            db.collection("users").document(user.getId())
                    .update(
                            "name", user.getName(),
                            "lastName", user.getLastName(),
                            "bio", user.getBio()
                    );
        }
    }

    private void loadUserProfile() {
        if (user != null) {
            // Update UI elements with user data
            name = requireView().findViewById(R.id.nameInProfile);
            lastName = requireView().findViewById(R.id.lastNameInProfile);
            bio = requireView().findViewById(R.id.bioInProfile);
            MaterialTextView wins = requireView().findViewById(R.id.winsNumber);
            MaterialTextView losses = requireView().findViewById(R.id.lossesNumber);
            MaterialTextView rank = requireView().findViewById(R.id.rankNumber);
            RatingBar ratingBar = requireView().findViewById(R.id.ratingBar);

            name.setText(user.getName());
            lastName.setText(user.getLastName());
            bio.setText(user.getBio());
            wins.setText(String.valueOf(user.getWins()));
            losses.setText(String.valueOf(user.getLosses()));
            rank.setText(String.valueOf(user.getRatingRank()));
            ratingBar.setRating(user.getRatingRep());


            ImageView profileImage = requireView().findViewById(R.id.profileImage);
            if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                Glide.with(requireContext())
                        .load(user.getProfilePicture())
                        .apply(RequestOptions.circleCropTransform())
                        .into(profileImage);
            }
        }
    }

}