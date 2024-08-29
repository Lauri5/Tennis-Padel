package com.example.tennis_padel;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private final List<Map.Entry<String, Report>> reportList;
    private final OnReportActionListener actionListener;
    private final Map<Report, String> reportDisplayMap;
    private final Context context;
    private final FirebaseFirestore firestore;

    public interface OnReportActionListener {
        void onEditReport(String reportKey, Report report);

        void onDeleteReport(String reportKey);
    }

    public ReportAdapter(List<Map.Entry<String, Report>> reportList, OnReportActionListener actionListener, Map<Report, String> reportDisplayMap, Context context) {
        this.reportList = reportList;
        this.actionListener = actionListener;
        this.reportDisplayMap = reportDisplayMap;
        this.context = context;
        this.firestore = FirebaseFirestore.getInstance(); // Initialize Firestore instance
    }

    public void updateReports(List<Map.Entry<String, Report>> newReports) {
        reportList.clear();
        reportList.addAll(newReports);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Map.Entry<String, Report> reportEntry = reportList.get(position);
        holder.bind(reportEntry);
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    class ReportViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView reportTextView;
        private final MaterialButton editButton;
        private final MaterialButton deleteButton;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            reportTextView = itemView.findViewById(R.id.reportTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(Map.Entry<String, Report> reportEntry) {
            // Display the report text using reportDisplayMap
            String reportText = reportDisplayMap.get(reportEntry.getValue());
            reportTextView.setText(reportText);

            // Handle click on the report item (show user name and date in a Toast)
            itemView.setOnClickListener(v -> {
                String reportId = reportEntry.getKey();
                String[] parts = reportId.split("_");
                String userId = parts[0];
                String date = parts[1];

                // Fetch the user's name from Firestore using userId
                firestore.collection("users").document(userId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String name = document.getString("name");
                            String lastName = document.getString("lastName");

                            if (!name.isEmpty() && !lastName.isEmpty()) {
                                lastName = " " + lastName;
                            }

                            if ((name == null || name.isEmpty()) && (lastName == null || lastName.isEmpty())) {
                                String email = document.getString("email");
                                if (email != null) {
                                    // Split the email at "@" and use the first part as the display name
                                    String[] part = email.split("@");
                                    email = part[0];
                                }
                                Toast.makeText(context, "Report by: " + email + " on " + date, Toast.LENGTH_LONG).show();
                            } else
                                Toast.makeText(context, "Report by: " + name + lastName + " on " + date, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Failed to load user data", Toast.LENGTH_SHORT).show();
                    }
                });
            });

            // Handle click on the edit button (show dialog to select a new report)
            editButton.setOnClickListener(v -> {
                showEditReportDialog(reportEntry.getKey(), reportEntry.getValue());
            });

            // Handle click on the delete button
            deleteButton.setOnClickListener(v -> {
                actionListener.onDeleteReport(reportEntry.getKey());
            });
        }

        private void showEditReportDialog(String reportKey, Report currentReport) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Select a New Report Type");

            // Convert reportDisplayMap to a list of display names and associated reports
            List<String> reportDisplayNames = new ArrayList<>(reportDisplayMap.values());
            List<Report> reportTypes = new ArrayList<>(reportDisplayMap.keySet());

            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, reportDisplayNames);
            builder.setAdapter(adapter, (dialog, which) -> {
                Report newReport = reportTypes.get(which);
                actionListener.onEditReport(reportKey, newReport);
                dialog.dismiss();
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            builder.show();
        }
    }
}