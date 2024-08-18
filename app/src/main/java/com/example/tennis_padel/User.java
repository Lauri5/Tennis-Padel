package com.example.tennis_padel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

enum Role {
    ADMIN,
    TEACHER,
    STUDENT
}

enum Report{
    UNSPORTSMANLIKE_CONDUCT,
    CHEATING,
    PHYSICAL_AGGRESSION,
    FALSE_VICTORY,
    RULE_VIOLATION,
    SAFETY_CONCERN
}

public class User implements Serializable {
    private String id, email, name, lastName, bio, profilePicture;
    private int wins, losses, ratingRank;
    private HashMap<String, Report> reports;
    private ArrayList<Reservation> reservations;
    private HashMap<String, Float> voters;
    private Role role;

    public User(String id, String email) {
        this.id = id;
        this.email = email;
        this.role = Role.STUDENT;
        this.profilePicture = "https://firebasestorage.googleapis.com/v0/b/tennis-padel-85718.appspot.com/o/ProfilePlaceHolder.png?alt=media&token=0fdd99d6-c24a-47bf-9a1c-336883cf5fc9";
    }

    public User() {
    }

    public boolean makeReservation(Court court, Date dateTime, boolean isLesson) {
        if (court.isAvailable(dateTime)) {
            Reservation reservation = new Reservation(generateReservationId(), court, dateTime, isLesson);
            reservation.addPlayer(this);
            court.addReservation(reservation);
            reservations.add(reservation);
            return true;
        }
        return false;
    }

    private String generateReservationId() {
        return "RES-" + System.currentTimeMillis();  // just a simple example
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public HashMap<String, Float> getVoters() {
        return voters;
    }

    public void setVoters(HashMap<String, Float> voters) {
        this.voters = voters;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public HashMap<String, Report> getReports() {
        return reports;
    }

    public void setReports(HashMap<String, Report> reports) {
        this.reports = reports;
    }

    public float getRatingRep() {
        if (voters == null || voters.isEmpty()) {
            return 0;
        }

        float sum = 0;
        for (Float value : voters.values()) {
            sum += value;
        }
        return sum / (float) voters.size();
    }

    public int getRatingRank() {
        if (wins + losses != 0) {
            ratingRank = (int)(((float) wins / (wins + losses)) * 100);
        } else {
            ratingRank = 0; // Handle cases where no games have been played yet
        }
        return ratingRank;
    }

    public void setRatingRank(int ratingRank) {
        this.ratingRank = ratingRank;
    }

    public ArrayList<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(ArrayList<Reservation> reservations) {
        this.reservations = reservations;
    }
}
