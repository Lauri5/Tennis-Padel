package com.example.tennis_padel;

import java.util.ArrayList;
import java.util.Date;

public class Reservation {
    private String id;
    private Court court;
    private Date dateTime;
    private ArrayList<User> players;
    private boolean isLesson;
    private User teacher;

    public Reservation(String id, Court court, Date dateTime, boolean isLesson) {
        this.id = id;
        this.court = court;
        this.dateTime = dateTime;
        this.players = new ArrayList<>();
        this.isLesson = isLesson;
    }

    public boolean addPlayer(User user) {
        int maxPlayers = isLesson ? 2 : 4;  // 2 for lessons, 4 for matches
        if (players.size() < maxPlayers) {
            players.add(user);
            updateCourtStatus();
            return true;
        }
        return false;
    }

    public void updateCourtStatus() {
        if (players.size() == 4 || (isLesson && players.size() == 2)) {
            court.setStatus(CourtStatus.RESERVED);
        } else if (players.size() > 0) {
            court.setStatus(CourtStatus.SEMI_RESERVED);
        } else {
            court.setStatus(CourtStatus.AVAILABLE);
        }
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

    public ArrayList<User> getPlayers() {
        return players;
    }

    public boolean isLesson() {
        return isLesson;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public User getTeacher() {
        return teacher;
    }
}
