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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;


public class ProfileFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadUserProfile();
        MaterialButton button = view.findViewById(R.id.logout);

        // Logout button
        button.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(requireContext(), Login.class));
            requireActivity().finish();
        });
    }

    public void loadUserProfile() {
        User user = UserDataRepository.getInstance().getUser();
        if (user != null) {
            // Update UI elements with user data
            TextInputEditText name = requireView().findViewById(R.id.nameInProfile);
            TextInputEditText lastName = requireView().findViewById(R.id.lastNameInProfile);
            TextInputEditText bio = requireView().findViewById(R.id.bioInProfile);
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