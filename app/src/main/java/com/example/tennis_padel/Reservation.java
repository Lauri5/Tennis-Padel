package com.example.tennis_padel;

import java.io.Serializable;

public class Reservation implements Serializable {
    private String id;
    private String courtId;  // Store the court's ID
    private String dateTime;
    private String player;  // Store player IDs
    private boolean lesson;
    private String teacherId;  // Store the teacher's ID

    // No-argument constructor required for Firestore serialization
    public Reservation() {
    }

    public Reservation(String id, String courtId, String dateTime, boolean lesson, String player) {
        this.id = id;
        this.courtId = courtId;
        this.dateTime = dateTime;
        this.lesson = lesson;
        this.player = player;
    }

    // Getter and setter methods
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourtId() {
        return courtId;
    }

    public void setCourtId(String courtId) {
        this.courtId = courtId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayers(String players) {
        this.player = player;
    }

    public boolean getLesson() {
        return lesson;
    }

    public void setLesson(boolean lesson) {
        this.lesson = lesson;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }
}