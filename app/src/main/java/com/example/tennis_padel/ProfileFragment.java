package com.example.tennis_padel;

import android.content.Intent;
import android.net.Uri;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileFragment extends Fragment {

    private TextInputEditText name, lastName, bio;
    private ImageView profileImage;
    private ProfileViewModel viewModel;
    private boolean isEditMode;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setupViews(view);
        setupImagePicker();
        loadUserProfile();
        setupEditButton(view.findViewById(R.id.edit));
        setupLogoutButton(view.findViewById(R.id.logout));
    }

    private void setupViews(View view) {
        name = view.findViewById(R.id.nameInProfile);
        lastName = view.findViewById(R.id.lastNameInProfile);
        bio = view.findViewById(R.id.bioInProfile);
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

    private void setupLogoutButton(MaterialButton logoutButton) {
        logoutButton.setOnClickListener(v -> {
            viewModel.logout();
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

    private void loadUserProfile() {
        User user = viewModel.getUser();
        if (user != null) {
            name.setText(user.getName());
            lastName.setText(user.getLastName());
            bio.setText(user.getBio());
            if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                updateProfileImage(user.getProfilePicture());
            }
        }
    }
}
