package com.example.tennis_padel;

import com.google.android.material.textview.MaterialTextView;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.Button;
import android.util.Log;

public class HomeFragment extends Fragment {

    private RecyclerView courtRecyclerView;
    private RecyclerView rankTableRecyclerView;
    private CourtAdapter courtAdapter;
    private RankTableAdapter rankTableAdapter;
    private List<Court> courtList = new ArrayList<>();
    private List<User> rankList = new ArrayList<>();
    private FirebaseFirestore db;
    private MaterialTextView selectedDateTextView;
    private Calendar currentDate;
    private Button datePickerButton;
    private Button timePickerButton;

    // Selected date and time
    private Calendar selectedDateTime = Calendar.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        selectedDateTextView = view.findViewById(R.id.selected_date_textview);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerViews
        courtRecyclerView = view.findViewById(R.id.courtView);
        rankTableRecyclerView = view.findViewById(R.id.rankTableView);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        courtRecyclerView.setLayoutManager(gridLayoutManager);
        rankTableRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize buttons
        datePickerButton = view.findViewById(R.id.datePickerButton);
        timePickerButton = view.findViewById(R.id.timePickerButton);

        // Set up adapters
        courtAdapter = new CourtAdapter(courtList, getParentFragmentManager(), selectedDateTime);
        rankTableAdapter = new RankTableAdapter(rankList);

        courtRecyclerView.setAdapter(courtAdapter);
        rankTableRecyclerView.setAdapter(rankTableAdapter);

        // Date picker listener
        datePickerButton.setOnClickListener(v -> showDatePicker());

        // Time picker listener
        timePickerButton.setOnClickListener(v -> showTimePicker());

        // Load initial data
        loadCourtData();
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

                    loadCourtData();  // Reload courts based on selected date
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
                    selectedDateTextView.setText("Selected Date: " + currentDate.get(Calendar.DAY_OF_MONTH) + "/" + currentDate.get(Calendar.MONTH) + "/" + currentDate.get(Calendar.YEAR) + " at " + hourOfDay + ":00");

                    loadCourtData();  // Reload courts based on selected time
                },
                currentTime.get(Calendar.HOUR_OF_DAY),
                0,  // Start at 0 minutes
                true
        );
        timePickerDialog.show();
    }

    private void loadCourtData() {
        db.collection("courts")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        courtList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            String name = document.getString("name");
                            String typeStr = document.getString("type");
                            String statStr = document.getString("status");
                            CourtStatus status = getStatusFromString(statStr);
                            CourtType type;
                            switch (typeStr){
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

                            Court court = new Court(id, name, type, status);
                            court.setReservations(loadReservations(document)); // Method to load reservations

                            if (isCourtAvailable(court)) { // Check if court is available in the selected time range
                                courtList.add(court);
                            }
                        }
                        courtAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("HomeFragment", "Error getting courts", task.getException());
                    }
                });
    }

    private ArrayList<Reservation> loadReservations(QueryDocumentSnapshot courtDocument) {
        ArrayList<Reservation> reservations = new ArrayList<>();
        List<Map<String, Object>> reservationsData = (List<Map<String, Object>>) courtDocument.get("reservations");

        if (reservationsData != null) {
            for (Map<String, Object> reservationData : reservationsData) {
                Date dateTime = (Date) reservationData.get("dateTime");
                boolean isLesson = (Boolean) reservationData.get("isLesson");
                String reservationId = (String) reservationData.get("id");

                // Assuming `Court` is already initialized with name, type, etc.
                Court court = new Court(courtDocument.getId(),courtDocument.getString("name"), CourtType.valueOf(courtDocument.getString("type")));

                Reservation reservation = new Reservation(
                        reservationId,
                        court,
                        dateTime,
                        isLesson
                );

                reservations.add(reservation);
            }
        }

        return reservations;
    }

    private boolean isCourtAvailable(Court court) {
        // Check if the court has any reservations that overlap with the selected date and time
        for (Reservation reservation : court.getReservations()) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(reservation.getDateTime());

            if (cal.get(Calendar.YEAR) == selectedDateTime.get(Calendar.YEAR) &&
                    cal.get(Calendar.MONTH) == selectedDateTime.get(Calendar.MONTH) &&
                    cal.get(Calendar.DAY_OF_MONTH) == selectedDateTime.get(Calendar.DAY_OF_MONTH) &&
                    cal.get(Calendar.HOUR_OF_DAY) == selectedDateTime.get(Calendar.HOUR_OF_DAY) &&
                    cal.get(Calendar.MINUTE) == selectedDateTime.get(Calendar.MINUTE)) {
                return false; // Court is not available at the selected date and time
            }
        }
        return true; // Court is available at the selected date and time
    }

    private CourtStatus getStatusFromString(String statusStr) {
        switch (statusStr) {
            case "AVAILABLE":
                return CourtStatus.AVAILABLE;
            case "RESERVED":
                return CourtStatus.RESERVED;
            case "SEMI_RESERVED":
                return CourtStatus.SEMI_RESERVED;
            default:
                return CourtStatus.AVAILABLE;
        }
    }

    private void loadRankData() {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        rankList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String profilePictureUrl = document.getString("profilePicture");
                            String lastName = document.getString("lastName");
                            int wins = ((Number) document.get("wins")).intValue();
                            int losses = ((Number) document.get("losses")).intValue();

                            User user = new User();
                            user.setName(name);
                            user.setLastName(lastName);
                            user.setProfilePicture(profilePictureUrl);
                            user.setWins(wins);
                            user.setLosses(losses);
                            rankList.add(user);
                        }

                        // Now sort the list by rank in descending order
                        Collections.sort(rankList, (u1, u2) -> Float.compare(u2.getRatingRank(), u1.getRatingRank())); // Sorting by rank, highest first

                        rankTableAdapter.notifyDataSetChanged();
                    }
                });
    }
}
