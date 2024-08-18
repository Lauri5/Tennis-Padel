package com.example.tennis_padel;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OtherProfileFragment extends Fragment {

    private static final String ARG_USER = "user";
    private User user;
    private FirebaseFirestore db;
    private Report selectedReport;
    private float lastRating = 0;  // Variable to store the last rating

    // Map to store custom display strings for each enum value
    private final HashMap<Report, String> reportDisplayMap = new HashMap<Report, String>() {{
        put(Report.UNSPORTSMANLIKE_CONDUCT, "Unsportsmanlike Conduct");
        put(Report.CHEATING, "Cheating");
        put(Report.FALSE_VICTORY, "False Victory");
        put(Report.PHYSICAL_AGGRESSION, "Physical Aggression");
        put(Report.RULE_VIOLATION, "Rule Violation");
        put(Report.SAFETY_CONCERN, "Safety Concern");
    }};

    public static OtherProfileFragment newInstance(User user) {
        OtherProfileFragment fragment = new OtherProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(ARG_USER);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_other_profile, container, false);
        db = FirebaseFirestore.getInstance();

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        MaterialTextView nameTextView = view.findViewById(R.id.nameInProfileOther);
        MaterialTextView lastNameTextView = view.findViewById(R.id.lastnameInProfileOther);
        MaterialTextView bioTextView = view.findViewById(R.id.bioInProfileOther);
        MaterialTextView winsTextView = view.findViewById(R.id.winsNumberOther);
        MaterialTextView lossesTextView = view.findViewById(R.id.lossesNumberOther);
        MaterialTextView rankTextView = view.findViewById(R.id.rankNumberOther);
        RatingBar ratingBar = view.findViewById(R.id.ratingBarOther);
        ImageView profileImageView = view.findViewById(R.id.profileImageOther);
        MaterialButton reportButton = view.findViewById(R.id.report);
        MaterialButton starButton = view.findViewById(R.id.starButton);
        Spinner spinnerReport = view.findViewById(R.id.spinnerReport);

        if (currentUserId.equals(user.getId())) {
            Fragment ProfileFragment = new ProfileFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment)
                    .addToBackStack(null)
                    .commit();
            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.nav_view);
            bottomNavigationView.setSelectedItemId(R.id.navigation_profile);

        } else {
            if (user != null) {
                nameTextView.setText(user.getName());
                lastNameTextView.setText(user.getLastName());
                bioTextView.setText(user.getBio());
                winsTextView.setText(String.valueOf(user.getWins()));
                lossesTextView.setText(String.valueOf(user.getLosses()));
                rankTextView.setText(String.valueOf(user.getRatingRank()));
                ratingBar.setRating(user.getRatingRep());

                List<String> reportsList = new ArrayList<>();
                HashMap<String, Report> stringToReportMap = new HashMap<>();
                for (Report report : Report.values()) {
                    String displayString = reportDisplayMap.get(report);
                    reportsList.add(displayString);
                    stringToReportMap.put(displayString, report);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, reportsList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerReport.setAdapter(adapter);

                spinnerReport.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedItem = parent.getItemAtPosition(position).toString();
                        selectedReport = stringToReportMap.get(selectedItem); // Update the selected report
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

                Glide.with(requireContext())
                        .load(user.getProfilePicture())
                        .circleCrop()
                        .into(profileImageView);

                ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                        if (fromUser) {
                            lastRating = rating;  // Update the last rating whenever the user changes it
                        }
                    }
                });

                starButton.setOnClickListener(v -> {
                    if (user != null && lastRating > 0) {
                        sendRatingToDatabase(currentUserId, lastRating);
                    }
                });

                reportButton.setOnClickListener(view1 -> {
                    if (selectedReport != null) {
                        String reportKey = currentUserId + "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")); // Create a unique key using the user ID and date
                        Map<String, Object> reportMap = new HashMap<>();
                        reportMap.put(reportKey, selectedReport.name());

                        db.runTransaction((Transaction.Function<Void>) transaction -> {
                            DocumentReference userDocRef = db.collection("users").document(user.getId());
                            DocumentSnapshot snapshot = transaction.get(userDocRef);
                            Map<String, Object> existingReports = (Map<String, Object>) snapshot.get("reports");
                            if (existingReports == null) {
                                existingReports = new HashMap<>();
                            }
                            existingReports.putAll(reportMap);
                            transaction.update(userDocRef, "reports", existingReports);
                            return null;
                        }).addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Report added successfully", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error submitting report", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }
        }
        return view;
    }

    private void sendRatingToDatabase(String userId, float rating) {
        DocumentReference userDocRef = db.collection("users").document(user.getId());
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot snapshot = transaction.get(userDocRef);
            Map<String, Float> existingVoters = (Map<String, Float>) snapshot.get("voters");
            if (existingVoters == null) {
                existingVoters = new HashMap<>();
            }
            existingVoters.put(userId, rating);
            transaction.update(userDocRef, "voters", existingVoters);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Rating submitted successfully:", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error submitting rating", Toast.LENGTH_SHORT).show();
        });
    }
}