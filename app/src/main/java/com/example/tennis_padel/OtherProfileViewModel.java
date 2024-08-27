package com.example.tennis_padel;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class OtherProfileViewModel extends AndroidViewModel {

    private final FirebaseFirestore db;
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<Float> lastRating = new MutableLiveData<>(0f);
    private final MutableLiveData<String> reportStatus = new MutableLiveData<>();
    private final MutableLiveData<String> ratingStatus = new MutableLiveData<>();
    private final MutableLiveData<String> banStatus = new MutableLiveData<>();

    public OtherProfileViewModel(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public void setUser(User user) {
        userLiveData.setValue(user);
    }

    public LiveData<Float> getLastRating() {
        return lastRating;
    }

    public void setLastRating(float rating) {
        lastRating.setValue(rating);
    }

    public LiveData<String> getReportStatus() {
        return reportStatus;
    }

    public LiveData<String> getRatingStatus() {
        return ratingStatus;
    }

    public LiveData<String> getBanStatus() {
        return banStatus;
    }

    public void submitRating(String userId, float rating) {
        User user = userLiveData.getValue();
        if (user == null) return;

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
            ratingStatus.postValue("Rating submitted successfully");
        }).addOnFailureListener(e -> {
            ratingStatus.postValue("Error submitting rating");
        });
    }

    public void submitReport(String userId, Report selectedReport) {
        User user = userLiveData.getValue();
        if (user == null || selectedReport == null) return;

        String reportKey = userId + "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
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
            reportStatus.postValue("Report added successfully");
        }).addOnFailureListener(e -> {
            reportStatus.postValue("Error submitting report");
        });
    }

    public void deleteReport(User user, String reportKey) {
        if (user != null && user.getReports() != null) {
            user.getReports().remove(reportKey);
            userLiveData.setValue(user); // Update the LiveData so the UI reflects the changes

            // Update Firestore
            DocumentReference userDocRef = db.collection("users").document(user.getId());
            db.runTransaction((Transaction.Function<Void>) transaction -> {
                DocumentSnapshot snapshot = transaction.get(userDocRef);
                Map<String, Object> existingReports = (Map<String, Object>) snapshot.get("reports");
                if (existingReports != null) {
                    existingReports.remove(reportKey);
                    transaction.update(userDocRef, "reports", existingReports);
                }
                return null;
            }).addOnSuccessListener(aVoid -> {
                reportStatus.postValue("Report deleted successfully");
            }).addOnFailureListener(e -> {
                reportStatus.postValue("Error deleting report");
            });
        }
    }

    public void updateReport(User user, String reportKey, Report newReport) {
        if (user != null && user.getReports() != null) {
            user.getReports().put(reportKey, newReport);
            userLiveData.setValue(user); // Update the LiveData so the UI reflects the changes

            // Update Firestore
            DocumentReference userDocRef = db.collection("users").document(user.getId());
            db.runTransaction((Transaction.Function<Void>) transaction -> {
                DocumentSnapshot snapshot = transaction.get(userDocRef);
                Map<String, Object> existingReports = (Map<String, Object>) snapshot.get("reports");
                if (existingReports == null) {
                    existingReports = new HashMap<>();
                }
                existingReports.put(reportKey, newReport.name());
                transaction.update(userDocRef, "reports", existingReports);
                return null;
            }).addOnSuccessListener(aVoid -> {
                reportStatus.postValue("Report updated successfully");
            }).addOnFailureListener(e -> {
                reportStatus.postValue("Error updating report");
            });
        }
    }

    public void incrementWins() {
        User user = userLiveData.getValue();
        if (user == null) return;

        user.addWins();

        DocumentReference userDocRef = db.collection("users").document(user.getId());
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            transaction.update(userDocRef, "wins", user.getWins());
            return null;
        });
    }

    public void incrementLosses() {
        User user = userLiveData.getValue();
        if (user == null) return;

        user.addLosses();

        DocumentReference userDocRef = db.collection("users").document(user.getId());
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            transaction.update(userDocRef, "losses", user.getLosses());
            return null;
        });
    }

    public void decrementWins() {
        User user = userLiveData.getValue();
        if (user == null) return;

        user.subWins();

        DocumentReference userDocRef = db.collection("users").document(user.getId());
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            transaction.update(userDocRef, "wins", user.getWins());
            return null;
        });
    }

    public void decrementLosses() {
        User user = userLiveData.getValue();
        if (user == null) return;

        user.subLosses();

        DocumentReference userDocRef = db.collection("users").document(user.getId());
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            transaction.update(userDocRef, "losses", user.getLosses());
            return null;
        });
    }

    public void banUser() {
        User user = userLiveData.getValue();
        if (user == null) return;

        DocumentReference userDocRef = db.collection("users").document(user.getId());
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            transaction.update(userDocRef, "isBanned", true);
            return null;
        }).addOnSuccessListener(aVoid -> {
            banStatus.postValue("Ban updated successfully");
        });
    }

    public void suspendUser() {
        User user = userLiveData.getValue();
        if (user == null) return;

        user.setSuspended(true);
    }
}
