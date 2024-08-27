package com.example.tennis_padel;

import android.content.Intent;
import android.net.Uri;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

public class ProfileFragment extends Fragment {

    private TextInputEditText name, lastName, bio, labelText;
    private MaterialTextView wins, losses, rank;
    private RatingBar  ratingBar;
    private ImageView profileImage, labelPicture;
    private ProfileViewModel viewModel;
    private boolean isEditMode, isAdmin;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;
    private User currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        currentUser = UserDataRepository.getInstance().getUser();
        isAdmin = currentUser.getRole() == Role.ADMIN;
        if(isAdmin)
            return inflater.inflate(R.layout.fragment_admin_profile, container, false);
        else
            return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        if (!isAdmin) {
            setupViews(view);
            setupImagePicker();
            loadUserProfile();
            setupEditButton(view.findViewById(R.id.edit));
            setupLogoutButton(view.findViewById(R.id.logout));
            notificationButton(view.findViewById(R.id.notifications));
        }else{
            setupAdminViews(view);
            setupAdminImagePicker();
            setupLogoutButton(view.findViewById(R.id.logout));
            setupEditLabelButton(view.findViewById(R.id.buttonEditLabel));
        }
    }

    private void setupAdminViews(View view) {
        labelText = view.findViewById(R.id.labelText);
        labelPicture = view.findViewById(R.id.labelPicture);
        labelPicture.setOnClickListener(v -> {
            if (isEditMode) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                imagePickerLauncher.launch(intent);
            }
        });
    }

    private void setupAdminImagePicker() {
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                selectedImageUri = result.getData().getData(); // Store the URI
                Glide.with(requireContext())
                        .load(selectedImageUri)
                        .apply(RequestOptions.circleCropTransform())
                        .into(labelPicture);
            }
        });
    }

    private void setupEditLabelButton(MaterialButton editButton) {
        isEditMode = false;
        updateEditMode();
        editButton.setOnClickListener(view -> {
            isEditMode = !isEditMode;
            editButton.setText(isEditMode ? "Save" : "Edit");
            updateEditMode();

            if (!isEditMode) {
                if (selectedImageUri != null) {
                    selectedImageUri = null; // Clear the URI after updating
                } else {
                    if (labelText.getText() != null) {
                    }
                }
            }
        });
    }

    private void setupViews(View view) {
        name = view.findViewById(R.id.nameInProfileOther);
        lastName = view.findViewById(R.id.lastnameInProfileOther);
        bio = view.findViewById(R.id.bioInProfileOther);
        wins = view.findViewById(R.id.winsNumberOther);
        losses = view.findViewById(R.id.lossesNumberOther);
        rank = view.findViewById(R.id.rankNumberOther);
        ratingBar = view.findViewById(R.id.ratingBar);
        profileImage = view.findViewById(R.id.profileImage);
        profileImage.setOnClickListener(v -> {
            if (isEditMode) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                imagePickerLauncher.launch(intent);
            }
        });
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                selectedImageUri = result.getData().getData(); // Store the URI
                Glide.with(requireContext())
                        .load(selectedImageUri)
                        .apply(RequestOptions.circleCropTransform())
                        .into(profileImage);
            }
        });
    }

    private void updateProfileImage(String imageUrl) {
        Glide.with(requireContext())
                .load(imageUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(profileImage);
        if (!isEditMode) {
            viewModel.updateUser(name.getText().toString().trim(), lastName.getText().toString().trim(),
                    bio.getText().toString().trim(), imageUrl);
        }
    }

    private void setupEditButton(MaterialButton editButton) {
        isEditMode = false;
        updateEditMode();
        editButton.setOnClickListener(view -> {
            isEditMode = !isEditMode;
            editButton.setText(isEditMode ? "Save" : "Edit");
            updateEditMode();

            if (!isEditMode) {
                User currentUser = viewModel.getUser(); // Retrieve the current user from the ViewModel
                if (selectedImageUri != null) {
                    // If a new image was selected, upload it and then update the user data
                    viewModel.uploadImageToFirebase(selectedImageUri, imageUrl -> {
                        viewModel.updateUser(name.getText().toString().trim(), lastName.getText().toString().trim(),
                                bio.getText().toString().trim(), imageUrl);
                        selectedImageUri = null; // Clear the URI after updating
                    });
                } else {
                    // Update the user without changing the image if no new image is selected
                    viewModel.updateUser(name.getText().toString().trim(), lastName.getText().toString().trim(),
                            bio.getText().toString().trim(), currentUser.getProfilePicture());
                }
            }
        });
    }

    private void notificationButton(MaterialButton notificationButton) {
        notificationButton.setOnClickListener(view -> {
            NotificationFragment notificationFragment = new NotificationFragment();
            FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container, notificationFragment)
                    .commit();
        });
    }

    private void setupLogoutButton(MaterialButton logoutButton) {
        logoutButton.setOnClickListener(v -> {
            viewModel.logout();
            startActivity(new Intent(requireContext(), Login.class));
            requireActivity().finish();
        });
    }

    private void updateEditMode() {
        if (!isAdmin){
            name.setFocusable(isEditMode);
            name.setFocusableInTouchMode(isEditMode);
            lastName.setFocusable(isEditMode);
            lastName.setFocusableInTouchMode(isEditMode);
            bio.setFocusable(isEditMode);
            bio.setFocusableInTouchMode(isEditMode);
        }else{
            labelText.setFocusable(isEditMode);
            labelText.setFocusableInTouchMode(isEditMode);
        }
    }

    private void loadUserProfile() {
        User user = viewModel.getUser();
        if (user != null) {
            name.setText(user.getName());
            lastName.setText(user.getLastName());
            bio.setText(user.getBio());
            wins.setText(String.valueOf(user.getWins()));
            losses.setText(String.valueOf(user.getLosses()));
            rank.setText(String.valueOf(user.getRatingRank()));
            ratingBar.setRating(user.getRatingRep());

            if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                updateProfileImage(user.getProfilePicture());
            }
        }
    }
}
