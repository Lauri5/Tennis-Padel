package com.example.tennis_padel;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.List;

public class TeacherAdminFragment extends Fragment {

    private RecyclerView teacherRecyclerView;
    private TeacherAdapter teacherAdapter;
    private List<User> teacherList = new ArrayList<>();
    private FirebaseFirestore db;
    private MaterialButton datePickerButton, halfDayButton, fullDayButton, removeButton;
    private Calendar startDate, endDate;
    private User selectedTeacher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_admin, container, false);

        db = FirebaseFirestore.getInstance();
        teacherRecyclerView = view.findViewById(R.id.teacher_recycler_view);
        teacherRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        datePickerButton = view.findViewById(R.id.datePickerButton);
        halfDayButton = view.findViewById(R.id.halfDayButton);
        fullDayButton = view.findViewById(R.id.fullDayButton);
        removeButton = view.findViewById(R.id.removeButton);

        loadTeachers();

        datePickerButton.setOnClickListener(v -> showDateRangePicker());

        halfDayButton.setOnClickListener(v -> addAvailability("09", "13"));
        fullDayButton.setOnClickListener(v -> addAvailability("09", "17"));
        removeButton.setOnClickListener(v -> removeAvailability());

        return view;
    }

    private void loadTeachers() {
        db.collection("users")
                .whereEqualTo("role", Role.TEACHER.toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        teacherList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User teacher = document.toObject(User.class);
                            teacherList.add(teacher);
                        }
                        teacherAdapter = new TeacherAdapter(teacherList, teacher -> {
                            selectedTeacher = teacher;
                            datePickerButton.setEnabled(true);
                            Toast.makeText(getContext(), "Teacher " + teacher.getName() + " selected.", Toast.LENGTH_SHORT).show();
                        }, requireContext());
                        teacherRecyclerView.setAdapter(teacherAdapter);
                    } else {
                        Toast.makeText(getContext(), "Failed to load teachers", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDateRangePicker() {
        // Create the date range picker
        MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Select Date Range");

        final MaterialDatePicker<androidx.core.util.Pair<Long, Long>> datePicker = builder.build();
        datePicker.show(getChildFragmentManager(), datePicker.toString());

        datePicker.addOnPositiveButtonClickListener(selection -> {
            androidx.core.util.Pair<Long, Long> dateRange = selection;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

            // Get the current date
            Calendar currentDate = Calendar.getInstance();

            // Convert start and end timestamps to Calendar instances
            startDate = Calendar.getInstance();
            startDate.setTimeInMillis(dateRange.first);

            endDate = Calendar.getInstance();
            endDate.setTimeInMillis(dateRange.second);

            // Check if the start date is before the current date
            if (startDate.before(currentDate)) {
                // Show a toast and disable buttons
                Toast.makeText(getContext(), "Start date cannot be before today.", Toast.LENGTH_SHORT).show();
                halfDayButton.setEnabled(false);
                fullDayButton.setEnabled(false);
                removeButton.setEnabled(false);
            } else {
                // Enable the buttons if the selected dates are valid
                halfDayButton.setEnabled(true);
                fullDayButton.setEnabled(true);
                removeButton.setEnabled(true);

                Toast.makeText(getContext(), "Date range selected: " + dateFormat.format(startDate.getTime()) + " - " + dateFormat.format(endDate.getTime()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addAvailability(String startHour, String endHour) {
        if (selectedTeacher == null || startDate == null || endDate == null) {
            Toast.makeText(getContext(), "Please select a teacher and date range.", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar current = (Calendar) startDate.clone();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

        while (!current.after(endDate)) {
            String dateKey = dateFormat.format(current.getTime());

            int startHourInt = Integer.parseInt(startHour);
            int endHourInt = Integer.parseInt(endHour);

            for (int hour = startHourInt; hour <= endHourInt; hour++) {
                String hourString = String.format(Locale.getDefault(), "%02d", hour);
                selectedTeacher.addAvailability(dateKey, hourString);
            }

            current.add(Calendar.DAY_OF_MONTH, 1);
        }

        db.collection("users").document(selectedTeacher.getId())
                .update("availability", selectedTeacher.getAvailability())
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Availability updated.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update availability: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void removeAvailability() {
        if (selectedTeacher == null || startDate == null || endDate == null) {
            Toast.makeText(getContext(), "Please select a teacher and date range.", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar current = (Calendar) startDate.clone();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

        while (!current.after(endDate)) {
            String dateKey = dateFormat.format(current.getTime());

            selectedTeacher.getAvailability().remove(dateKey);

            current.add(Calendar.DAY_OF_MONTH, 1);
        }

        db.collection("users").document(selectedTeacher.getId())
                .update("availability", selectedTeacher.getAvailability())
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Availability removed.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to remove availability: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
