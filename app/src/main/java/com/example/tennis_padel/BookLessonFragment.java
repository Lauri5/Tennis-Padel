package com.example.tennis_padel;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookLessonFragment extends Fragment {

    private RecyclerView teacherRecyclerView, courtRecyclerView;
    private TeacherAdapter teacherAdapter;
    private CourtAdapter courtAdapter;
    private List<User> teacherList = new ArrayList<>();
    private List<Court> courtList = new ArrayList<>();
    private MainViewModel viewModel;
    private FirebaseFirestore db;
    private Calendar selectedDateTime = Calendar.getInstance();
    private boolean isDateSelected = false;
    private boolean isTimeSelected = false;
    private boolean isTeacherSelected = false;
    private boolean isCourtSelected = false;
    private MaterialButton datePickerButton, timePickerButton, bookButton;
    private MaterialTextView selectedDateTextView;
    private Court selectedCourt;
    private User selectedTeacher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_lesson, container, false);

        db = FirebaseFirestore.getInstance();
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        teacherRecyclerView = view.findViewById(R.id.teacher_recycler_view);
        courtRecyclerView = view.findViewById(R.id.court_recycler_view);
        selectedDateTextView = view.findViewById(R.id.selected_date_textview);
        teacherRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        courtRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Initialize buttons
        datePickerButton = view.findViewById(R.id.datePickerButton);
        timePickerButton = view.findViewById(R.id.timePickerButton);
        bookButton = view.findViewById(R.id.bookButton);

        timePickerButton.setEnabled(false); // Disable time picker initially
        bookButton.setEnabled(false); // Disable book button initially

        // Date picker listener
        datePickerButton.setOnClickListener(v -> showDatePicker());

        // Time picker listener
        timePickerButton.setOnClickListener(v -> showTimePicker());

        // Book button listener
        bookButton.setOnClickListener(v -> {
            if (selectedTeacher != null && selectedCourt != null) {
                bookLessonWithTeacher(selectedTeacher);
            } else {
                Toast.makeText(getContext(), "Please select both a teacher and a court.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void showDatePicker() {
        Calendar currentDate = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    isDateSelected = true; // Mark date as selected
                    timePickerButton.setEnabled(true); // Enable time picker now that date is selected

                    // Display the selected date
                    selectedDateTextView.setText("Selected Date: " + dayOfMonth + "/" + (month + 1) + "/" + year);

                    if (isTimeSelected) {
                        loadAvailableTeachersAndCourts();
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

                    isTimeSelected = true; // Mark time as selected

                    // Append the selected hour to the displayed date
                    selectedDateTextView.setText("Selected Date: " + selectedDateTime.get(Calendar.DAY_OF_MONTH) + "/" + (selectedDateTime.get(Calendar.MONTH) + 1) + "/" + selectedDateTime.get(Calendar.YEAR) + " at " + hourOfDay + ":00");

                    if (isDateSelected) {
                        loadAvailableTeachersAndCourts();
                    }
                },
                currentTime.get(Calendar.HOUR_OF_DAY),
                0,  // Start at 0 minutes
                true
        );
        timePickerDialog.show();
    }

    private void loadAvailableTeachersAndCourts() {
        loadAvailableTeachers();
        loadAvailableCourts();
    }

    private void loadAvailableTeachers() {
        // Similar implementation to your existing loadAvailableTeachers() method
        // Update the adapter and set the OnTeacherClickListener
        db.collection("users")
                .whereEqualTo("role", Role.TEACHER.toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        teacherList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User teacher = document.toObject(User.class);
                            if (isTeacherAvailable(teacher, selectedDateTime.getTime())) {
                                teacherList.add(teacher);
                            }
                        }
                        teacherAdapter = new TeacherAdapter(teacherList, teacher -> {
                            selectedTeacher = teacher;
                            isTeacherSelected = true;
                            checkIfReadyToBook();
                            // Provide feedback for selection
                            Toast.makeText(getContext(), "Teacher " + teacher.getName() + " selected.", Toast.LENGTH_SHORT).show();
                        }, requireContext());
                        teacherRecyclerView.setAdapter(teacherAdapter);
                    } else {
                        Toast.makeText(getContext(), "Failed to load teachers", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadAvailableCourts() {
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
                                .anyMatch(reservation -> reservation.getDateTime().equals(formatDateTime(selectedDateTime.getTime())));

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

                                            CourtStatus status = court.getStatus(selectedDateTime.getTime());

                                            if (status == CourtStatus.AVAILABLE) {
                                                courtList.add(court);
                                            }
                                        }

                                        courtAdapter = new CourtAdapter(courtList, selectedDateTime, selectedCourt -> {
                                            this.selectedCourt = selectedCourt;
                                            isCourtSelected = true;
                                            checkIfReadyToBook();
                                            // Provide feedback for selection
                                            Toast.makeText(getContext(), "Court " + selectedCourt.getName() + " selected.", Toast.LENGTH_SHORT).show();
                                        });
                                        courtRecyclerView.setAdapter(courtAdapter);
                                    } else {
                                        Toast.makeText(getContext(), "Failed to load courts", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            });
        }
    }

    private boolean isTeacherAvailable(User teacher, Date selectedDateTime) {
        String formattedDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(selectedDateTime);
        return teacher.getAvailability() != null &&
                teacher.getAvailability().containsKey(formattedDate) &&
                teacher.getAvailability().get(formattedDate).contains(new SimpleDateFormat("HH", Locale.getDefault()).format(selectedDateTime));
    }

    private void checkIfReadyToBook() {
        if (isTeacherSelected && isCourtSelected) {
            bookButton.setEnabled(true); // Enable the "Book" button
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

    private void bookLessonWithTeacher(User teacher) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String reservationId = userId + "-" + formatDateTime(selectedDateTime.getTime());

        db.runTransaction(transaction -> {
            // Get references to the court, user, and teacher documents
            DocumentReference courtRef = db.collection("courts").document(selectedCourt.getId());
            Court court = transaction.get(courtRef).toObject(Court.class);

            DocumentReference userRef = db.collection("users").document(userId);
            User user = transaction.get(userRef).toObject(User.class);

            DocumentReference teacherRef = db.collection("users").document(teacher.getId());
            User updatedTeacher = transaction.get(teacherRef).toObject(User.class);

            if (court != null && user != null && updatedTeacher != null) {
                // Initialize reservations lists if they are null
                if (court.getReservations() == null) {
                    court.setReservations(new ArrayList<>());
                }
                if (user.getReservations() == null) {
                    user.setReservations(new ArrayList<>());
                }
                if (updatedTeacher.getReservations() == null) {
                    updatedTeacher.setReservations(new ArrayList<>());
                }

                // Create a new reservation
                Reservation newReservation = new Reservation(
                        reservationId,
                        court.getId(),
                        formatDateTime(selectedDateTime.getTime()),
                        true, // isLesson set to true
                        userId
                );
                newReservation.setTeacherId(teacher.getId());

                // Add the reservation to court, user, and teacher
                court.getReservations().add(newReservation);
                user.getReservations().add(newReservation);
                updatedTeacher.getReservations().add(newReservation);

                // Update the court, user, and teacher documents
                transaction.update(courtRef, "reservations", court.getReservations());
                transaction.update(userRef, "reservations", user.getReservations());
                transaction.update(teacherRef, "reservations", updatedTeacher.getReservations());

                // Update teacher's availability
                String formattedDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(selectedDateTime.getTime());
                String hour = new SimpleDateFormat("HH", Locale.getDefault()).format(selectedDateTime.getTime());
                updatedTeacher.removeAvailability(formattedDate, hour);
                transaction.update(teacherRef, "availability", updatedTeacher.getAvailability());

            } else {
                throw new FirebaseFirestoreException("Court, User, or Teacher not found.", FirebaseFirestoreException.Code.ABORTED);
            }

            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Lesson booked successfully!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to book lesson: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private String formatDateTime(Date dateTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd-HH", Locale.getDefault());
        return dateFormat.format(dateTime);
    }
}
