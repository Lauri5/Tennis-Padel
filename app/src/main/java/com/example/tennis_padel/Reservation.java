package com.example.tennis_padel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Reservation implements Serializable {
    private String id;
    private String courtId;  // Store the court's ID
    private String dateTime;
    private String player;  // Store player IDs
    private boolean isLesson;
    private String teacherId;  // Store the teacher's ID

    // No-argument constructor required for Firestore serialization
    public Reservation() {
    }

    public Reservation(String id, String courtId, String dateTime, boolean isLesson, String player) {
        this.id = id;
        this.courtId = courtId;
        this.dateTime = dateTime;
        this.isLesson = isLesson;
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

    public boolean isLesson() {
        return isLesson;
    }

    public void setLesson(boolean lesson) {
        isLesson = lesson;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }
}