package com.example.tennis_padel;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Hide the ActionBar if it's present
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        requestNotificationPermission();

        Club club = new Club();
        UserDataRepository.getInstance().setClub(club);
        manageActionBar();

        // Initialize ViewModel
        MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.removePastReservations();

        // Check if user is logged in
        if (!viewModel.userLoggedIn()) {
            startActivity(new Intent(this, Login.class));
            finish();
        } else {
            viewModel.loadUserData();
        }

        // Observe the ban status LiveData
        viewModel.getIsUserBannedLiveData().observe(this, banned -> {
            if (banned) {
                // Log out the user and redirect to login activity
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, Login.class));
                finish();
            }
        });

        // It's important to call this after setting up the observer
        if (viewModel.userLoggedIn()) {
            viewModel.checkUserBanStatus();  // Check if the user is banned
        }

        if (UserDataRepository.getInstance().getIsFromLogin()) {
            // Observe the loading status to decide when to set up navigation
            viewModel.getIsLoading().observe(this, isLoading -> {
                if (isLoading != null && !isLoading) {
                    setupNavigation(); // Call setupNavigation when loading is complete
                }
            });
            UserDataRepository.getInstance().setIsFromLogin(false);
        } else if (UserDataRepository.getInstance().getUser() == null)
            // Observe the loading status to decide when to set up navigation
            viewModel.getIsLoading().observe(this, isLoading -> {
                if (isLoading != null && !isLoading) {
                    setupNavigation(); // Call setupNavigation when loading is complete
                }
            });
        else setupNavigation();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if the intent contains the fragment to open
        if (getIntent().hasExtra("openFragment")) {
            String fragmentName = getIntent().getStringExtra("openFragment");
            if ("NotificationFragment".equals(fragmentName)) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new NotificationFragment()).commit();
            }
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Listen for invitations where the current user is the invitee
        db.collection("invitations").whereEqualTo("inviteeId", currentUserId).whereIn("status", Arrays.asList("pending", "full")).addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w("MainActivity", "Listen failed.", e);
                return;
            }

            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                if (dc.getType() == DocumentChange.Type.ADDED) {
                    String court = dc.getDocument().getString("courtName");
                    String time = dc.getDocument().getString("time");
                    String status = dc.getDocument().getString("status");

                    // Show a notification to the user
                    showNotification(court, time, status);

                    // Update the status to "notified" to prevent duplicate notifications
                    assert status != null;
                    if (!status.equals("full"))
                        dc.getDocument().getReference().update("status", "notified");
                }
            }
        });

        checkAndUpdateUserSuspensions();
    }

    private void checkAndUpdateUserSuspensions() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult()) {
                    User user = document.toObject(User.class);
                    if (user != null && user.isSuspended()) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            Date suspensionEndDate = sdf.parse(user.getSuspensionEndDate());
                            Date currentDate = new Date();
                            if (suspensionEndDate != null && suspensionEndDate.before(currentDate)) {
                                // Update the user's suspended status in Firestore
                                updateSuspensionStatus(document.getId(), false);
                            }
                        } catch (Exception e) {
                            Log.e("MainActivity", "Error parsing date for user suspension", e);
                        }
                    }
                }
            } else {
                Log.e("MainActivity", "Error getting documents: ", task.getException());
            }
        });
    }

    private void updateSuspensionStatus(String userId, boolean isSuspended) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).update("suspended", isSuspended).addOnSuccessListener(aVoid -> Log.d("MainActivity", "User suspension status updated successfully")).addOnFailureListener(e -> Log.e("MainActivity", "Error updating user suspension status", e));
    }

    private void showNotification(String court, String time, String status) {
        // Step 1: Create a Notification Channel (for Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "InviteNotificationChannel";
            String description = "Channel for Court Invitation Notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("InviteNotificationChannel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("openFragment", "NotificationFragment");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Here we add the FLAG_IMMUTABLE flag
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        String message;
        if (status.equals("full")) message = "The game is on!";
        else message = "You've been invited to play!";

        // Step 2: Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "InviteNotificationChannel").setSmallIcon(R.drawable.ic_notification)  // Replace with your notification icon
                .setContentTitle(message).setContentText("Court: " + court + " at " + time).setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true).setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            return;
        }
        // Step 3: Show the notification
        notificationManager.notify(2, builder.build());
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }

    private void setupNavigation() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.navigation_search) {
                selectedFragment = new SearchFragment();
            } else if (itemId == R.id.navigation_profile) {
                selectedFragment = new ProfileFragment();
            } else if (itemId == R.id.navigation_book_lesson) {
                if (UserDataRepository.getInstance().getUser().getRole() == Role.ADMIN)
                    selectedFragment = new TeacherAdminFragment();
                else selectedFragment = new BookLessonFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }
            return true;
        });

        // To display the home fragment initially when the app starts
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    v.clearFocus();
                    hideKeyboard(v);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void manageActionBar() {
        ImageView clubLogo = findViewById(R.id.action_bar_logo);
        MaterialTextView clubName = findViewById(R.id.action_bar_title);
        Club club = UserDataRepository.getInstance().getClub();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("admin.jpg");

        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            // This is called once the download URL is available
            String imageUrl = uri.toString();
            club.setClubLogo(imageUrl);

            // Use Glide to load the image
            Glide.with(MainActivity.this).load(club.getClubLogo()).circleCrop().into(clubLogo);
        });

        StorageReference fileRef = storage.getReference().child("title.txt");

        fileRef.getBytes(100).addOnSuccessListener(bytes -> {
            // Convert bytes data back to string
            String downloadedText = new String(bytes, StandardCharsets.UTF_8);
            club.setClubName(downloadedText);
            clubName.setText(downloadedText);
        });
    }
}