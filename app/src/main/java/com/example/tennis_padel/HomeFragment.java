package com.example.tennis_padel;

import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.Button;
import android.widget.Toast;

public class HomeFragment extends Fragment {

    private RecyclerView courtRecyclerView;
    private CourtAdapter courtAdapter;
    private List<Court> courtList = new ArrayList<>();
    private MainViewModel viewModel;
    private FirebaseFirestore db;
    private MaterialTextView selectedDateTextView;
    private Calendar currentDate;
    private UserAdapter userAdapter;
    private Button datePickerButton;
    private Button timePickerButton;
    private FirebaseUser user;

    // Selected date and time
    private Calendar selectedDateTime = Calendar.getInstance();
    private boolean isDateSelected = false;
    private boolean isTimeSelected = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        selectedDateTextView = view.findViewById(R.id.selected_date_textview);
        user = FirebaseAuth.getInstance().getCurrentUser();
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerViews
        courtRecyclerView = view.findViewById(R.id.courtView);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        courtRecyclerView.setLayoutManager(gridLayoutManager);

        // Initialize buttons
        datePickerButton = view.findViewById(R.id.datePickerButton);
        timePickerButton = view.findViewById(R.id.timePickerButton);
        timePickerButton.setEnabled(false); // Disable time picker initially

        // Set up adapters
        courtAdapter = new CourtAdapter(courtList, getParentFragmentManager(), selectedDateTime);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        userAdapter = new UserAdapter(getContext(), new ArrayList<>(), null, true); // Start with an empty list
        recyclerView.setAdapter(userAdapter);

        courtRecyclerView.setAdapter(courtAdapter);

        // Date picker listener
        datePickerButton.setOnClickListener(v -> showDatePicker());

        // Time picker listener
        timePickerButton.setOnClickListener(v -> showTimePicker());

        loadRankData();

        return view;
    }

    private void showDatePicker() {
        currentDate = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Display the selected date
                    selectedDateTextView.setText("Selected Date: " + dayOfMonth + "/" + (month + 1) + "/" + year);

                    isDateSelected = true; // Mark date as selected
                    timePickerButton.setEnabled(true); // Enable time picker now that date is selected

                    // Load courts only if time is also selected
                    if (isTimeSelected) {
                        loadCourtData();
                    }
                },
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar currentTime = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, 0); // Ignore minutes, set them to 0

                    // Append the selected hour to the displayed date
                    selectedDateTextView.setText("Selected Date: " + selectedDateTime.get(Calendar.DAY_OF_MONTH) + "/" + (selectedDateTime.get(Calendar.MONTH) + 1) + "/" + selectedDateTime.get(Calendar.YEAR) + " at " + hourOfDay + ":00");

                    isTimeSelected = true; // Mark time as selected

                    // Load courts only if date is also selected
                    if (isDateSelected) {
                        loadCourtData();
                    }
                },
                currentTime.get(Calendar.HOUR_OF_DAY),
                0,  // Start at 0 minutes
                true
        );
        timePickerDialog.show();
    }

    private void loadCourtData() {
        // Check if the selected date is in the future
        Date currentDate = new Date();
        if (!selectedDateTime.getTime().after(currentDate)) {
            Toast.makeText(getContext(), "Please select a future date.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the selected time is between 9 AM and 9 PM
        int selectedHour = selectedDateTime.get(Calendar.HOUR_OF_DAY);
        if (selectedHour < 9 || selectedHour > 21) {
            Toast.makeText(getContext(), "Please select a time between 9 AM and 9 PM.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current user and load courts if no conflict
        FirebaseUser firebaseUser = viewModel.getCurrentUser();
        if (firebaseUser != null) {
            db.collection("users").document(firebaseUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User currentUser = documentSnapshot.toObject(User.class);
                    if (currentUser != null) {
                        // Ensure the reservations list is initialized
                        if (currentUser.getReservations() == null) {
                            currentUser.setReservations(new ArrayList<>()); // Initialize if null
                        }

                        boolean hasReservation = currentUser.getReservations().stream()
                                .anyMatch(reservation -> reservation.getDateTime().equals(formatDateTime(selectedDateTime)));

                        if (hasReservation) {
                            Toast.makeText(getContext(), "You already have a reservation at this time.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        db.collection("courts")
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        courtList.clear();
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            String id = document.getId();
                                            String name = document.getString("name");
                                            String typeStr = document.getString("type");

                                            CourtType type;
                                            switch (typeStr) {
                                                case "TENNIS_INDOOR":
                                                    type = CourtType.TENNIS_INDOOR;
                                                    break;
                                                case "PADEL_OUTDOOR":
                                                    type = CourtType.PADEL_OUTDOOR;
                                                    break;
                                                case "PADEL_INDOOR":
                                                    type = CourtType.PADEL_INDOOR;
                                                    break;
                                                default:
                                                    type = CourtType.TENNIS_OUTDOOR;
                                                    break;
                                            }

                                            Court court = new Court(id, name, type);
                                            court.setReservations(loadReservations(document));

                                            // Dynamically calculate the status based on reservations
                                            CourtStatus status = court.getStatus(selectedDateTime.getTime());

                                            // Only add available or semi-reserved courts to the list
                                            if (status == CourtStatus.AVAILABLE || status == CourtStatus.SEMI_RESERVED) {
                                                courtList.add(court);
                                            }
                                        }
                                        courtAdapter.notifyDataSetChanged();
                                    }
                                });
                    }
                }
            });
        }
    }

    private ArrayList<Reservation> loadReservations(QueryDocumentSnapshot courtDocument) {
        ArrayList<Reservation> reservations = new ArrayList<>();
        List<Map<String, Object>> reservationsData = (List<Map<String, Object>>) courtDocument.get("reservations");

        if (reservationsData != null) {
            for (Map<String, Object> reservationData : reservationsData) {
                // Since dateTime is stored as a String, retrieve it directly
                String dateTimeStr = (String) reservationData.get("dateTime");

                // Safely retrieve the boolean value for lesson
                Boolean lessonObj = (Boolean) reservationData.get("lesson");
                boolean lesson = lessonObj != null ? lessonObj : false;

                String reservationId = (String) reservationData.get("id");
                String player = (String) reservationData.get("player");

                Reservation reservation = new Reservation(
                        reservationId,
                        courtDocument.getId(),
                        dateTimeStr, // Use the dateTime as a String
                        lesson,
                        player
                );

                reservations.add(reservation);
            }
        }

        return reservations;
    }

    private void loadRankData() {
        viewModel.loadAllUsers();

        viewModel.getAllUsersLiveData().observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                userAdapter.sortUserList(users);
            }
        });
    }

    private String formatDateTime(Calendar dateTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd-HH", Locale.getDefault());
        return dateFormat.format(dateTime.getTime());
    }
}