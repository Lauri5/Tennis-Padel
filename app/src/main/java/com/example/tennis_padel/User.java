package com.example.tennis_padel;

import java.util.ArrayList;

enum Role {
    ADMIN,
    TEACHER,
    STUDENT
}

public class User {
    private String id, email, name, lastName, bio, profilePicture;
    private int numberOfVotes, wins, losses, ratingRank;
    private ArrayList<String> reports;
    private float ratingRep;
    private ArrayList<Reservation> reservations;
    private Role role;

    public User(String id, String email) {
        this.id = id;
        this.email = email;
        this.role = Role.STUDENT;
        this.profilePicture = "https://firebasestorage.googleapis.com/v0/b/tennis-padel-85718.appspot.com/o/ProfilePlaceHolder.png?alt=media&token=0fdd99d6-c24a-47bf-9a1c-336883cf5fc9";
    }

    public User() {
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

    public int getNumberOfVotes() {
        return numberOfVotes;
    }

    public void setNumberOfVotes(int numberOfVotes) {
        this.numberOfVotes = numberOfVotes;
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

    public ArrayList<String> getReports() {
        return reports;
    }

    public void setReports(ArrayList<String> reports) {
        this.reports = reports;
    }

    public float getRatingRep() {
        return ratingRep;
    }

    public void setRatingRep(float ratingRep) {
        this.ratingRep = ratingRep;
    }

    public int getRatingRank() {
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
