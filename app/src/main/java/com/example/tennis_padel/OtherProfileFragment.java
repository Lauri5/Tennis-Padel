package com.example.tennis_padel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OtherProfileFragment extends Fragment {

    private static final String ARG_USER = "user";
    private User user;
    private OtherProfileViewModel viewModel;
    private Report selectedReport;
    private ReportAdapter reportAdapter;

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
        viewModel = new ViewModelProvider(this).get(OtherProfileViewModel.class);
        viewModel.setUser(user);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_other_profile, container, false);

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
        MaterialTextView repTextView = view.findViewById(R.id.repProfile);
        RecyclerView reportsRecyclerView = view.findViewById(R.id.reportsRecyclerView);
        MaterialButton winsPlus = view.findViewById(R.id.winsPlus);
        MaterialButton lossesPlus = view.findViewById(R.id.lossesPlus);
        MaterialButton winsMinus = view.findViewById(R.id.winsMinus);
        MaterialButton lossesMinus = view.findViewById(R.id.lossesMinus);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        User currentUser = UserDataRepository.getInstance().getUser();

        // Initialize the RecyclerView and the Adapter here to avoid NullPointerException
        setupReportsRecyclerView(reportsRecyclerView);

        viewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
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

                    Glide.with(requireContext())
                            .load(user.getProfilePicture())
                            .circleCrop()
                            .into(profileImageView);

                    if (currentUser.getRole() == Role.ADMIN) {
                        // Hide unnecessary UI elements for admin
                        reportButton.setVisibility(View.GONE);
                        starButton.setVisibility(View.GONE);
                        spinnerReport.setVisibility(View.GONE);

                        // Show the admin-specific RecyclerView and buttons
                        reportsRecyclerView.setVisibility(View.VISIBLE);
                        winsPlus.setVisibility(View.VISIBLE);
                        lossesPlus.setVisibility(View.VISIBLE);
                        winsMinus.setVisibility(View.VISIBLE);
                        lossesMinus.setVisibility(View.VISIBLE);

                        ratingBar.setIsIndicator(true);

                        // Populate the reports list for the admin
                        if (user.getReports() != null && !user.getReports().isEmpty()) {
                            List<Map.Entry<String, Report>> reportEntries = new ArrayList<>(user.getReports().entrySet());
                            reportAdapter.updateReports(reportEntries);
                        } else {
                            reportAdapter.updateReports(new ArrayList<>());
                        }
                    }
                }
            }
        });

        // Regular user functionalities
        if (currentUser.getRole() != Role.ADMIN) {
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
                    selectedReport = stringToReportMap.get(selectedItem);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            ratingBar.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> {
                if (fromUser) {
                    viewModel.setLastRating(rating);
                }
            });

            starButton.setOnClickListener(v -> {
                if (user != null) {
                    viewModel.submitRating(currentUserId, viewModel.getLastRating().getValue());
                }
            });

            reportButton.setOnClickListener(v -> {
                if (selectedReport != null) {
                    viewModel.submitReport(currentUserId, selectedReport);
                }
            });
        }

        // Admin-specific functionality for adjusting wins and losses
        winsPlus.setOnClickListener(v -> {
            viewModel.incrementWins();
            refreshRank(winsTextView, lossesTextView, rankTextView);
        });

        winsMinus.setOnClickListener(v -> {
            viewModel.decrementWins();
            refreshRank(winsTextView, lossesTextView, rankTextView);
        });

        lossesPlus.setOnClickListener(v -> {
            viewModel.incrementLosses();
            refreshRank(winsTextView, lossesTextView, rankTextView);
        });

        lossesMinus.setOnClickListener(v -> {
            viewModel.decrementLosses();
            refreshRank(winsTextView, lossesTextView, rankTextView);
        });

        viewModel.getRatingStatus().observe(getViewLifecycleOwner(), status -> {
            Toast.makeText(getContext(), status, Toast.LENGTH_SHORT).show();
        });

        viewModel.getReportStatus().observe(getViewLifecycleOwner(), status -> {
            Toast.makeText(getContext(), status, Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void setupReportsRecyclerView(RecyclerView reportsRecyclerView) {
        reportsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        reportAdapter = new ReportAdapter(new ArrayList<>(), new ReportAdapter.OnReportActionListener() {
            @Override
            public void onEditReport(String reportKey, Report report) {
                // Implement logic for editing the report here, e.g., update the report in the database
                viewModel.updateReport(user, reportKey, report);
                Toast.makeText(getContext(), "Report updated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteReport(String reportKey) {
                // Implement logic for deleting the report here
                viewModel.deleteReport(user, reportKey);
                Toast.makeText(getContext(), "Report deleted", Toast.LENGTH_SHORT).show();
            }
        }, reportDisplayMap, getContext()); // Pass the map and context here
        reportsRecyclerView.setAdapter(reportAdapter);
    }

    private void refreshRank(MaterialTextView winsTextView, MaterialTextView lossesTextView, MaterialTextView rankTextView) {
        winsTextView.setText(String.valueOf(viewModel.getUserLiveData().getValue().getWins()));
        lossesTextView.setText(String.valueOf(viewModel.getUserLiveData().getValue().getLosses()));
        rankTextView.setText(String.valueOf(viewModel.getUserLiveData().getValue().getRatingRank()));
    }
}
