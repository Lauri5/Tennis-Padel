package com.example.tennis_padel;

import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Date;

public class Reservation {
    private String id;
    private Court court;
    private Date dateTime;
    private ArrayList<String> players;
    private boolean isLesson;
    private User teacher;

    public Reservation(String id, Court court, Date dateTime, boolean isLesson) {
        this.id = id;
        this.court = court;
        this.dateTime = dateTime;
        this.players = new ArrayList<>();
        this.isLesson = isLesson;
    }

    public void addPlayer(String user) {
        if (!players.contains(user) && players.size() < 4) {
            players.add(user);
        }
    }

    public void updateCourtStatus() {
        if (isFull()) {
            court.setStatus(CourtStatus.RESERVED);
        } else if (getPlayerCount() > 0) {
            court.setStatus(CourtStatus.SEMI_RESERVED);
        } else {
            court.setStatus(CourtStatus.AVAILABLE);
        }
    }

    public int getPlayerCount() {
        return players.size();
    }

    public boolean isFull() {
        return players.size() == 4;
    }

    public String getId() {
        return id;
    }

    public Court getCourt() {
        return court;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public ArrayList<String> getPlayers() {
        return players;
    }

    public boolean isLesson() {
        return isLesson;
    }

    public User getTeacher() {
        return teacher;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }
}