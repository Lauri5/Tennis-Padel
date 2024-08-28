package com.example.tennis_padel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

enum CourtStatus {
    AVAILABLE,
    RESERVED,
    SEMI_RESERVED
}

enum CourtType {
    TENNIS_INDOOR,
    TENNIS_OUTDOOR,
    PADEL_INDOOR,
    PADEL_OUTDOOR
}

public class Court implements Serializable {
    private String id;
    private String name;
    private CourtType type;
    private ArrayList<Reservation> reservations;

    public Court(){
        this.reservations = new ArrayList<>();
    }

    public Court(String id, String name, CourtType type) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.reservations = new ArrayList<>();
    }

    public Court(String name, CourtType type) {
        this.name = name;
        this.type = type;
        this.reservations = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CourtType getType() {
        return type;
    }

    public void setType(CourtType type) {
        this.type = type;
    }

    public ArrayList<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(ArrayList<Reservation> reservations) {
        this.reservations = reservations;
    }

    public CourtStatus getStatus(Date dateTime) {
        String formattedDate = formatDateTime(dateTime);
        int playerCount = 0;
        boolean lesson = false;

        for (Reservation reservation : reservations) {
            if (reservation.getDateTime().equals(formattedDate)) {
                playerCount++;
                if (reservation.getLesson()) {
                    lesson = true;  // Identify that this is a lesson reservation
                }
            }
        }

        if (playerCount == 0) {
            return CourtStatus.AVAILABLE;
        } else if (lesson && playerCount >= 1) {
            // For lessons, the court is reserved if there are 2 or more players
            return CourtStatus.RESERVED;
        } else if (!lesson && playerCount < 4) {
            return CourtStatus.SEMI_RESERVED;
        } else if (!lesson && playerCount == 4) {
            return CourtStatus.RESERVED;
        } else {
            // Just in case, handle any edge cases
            return CourtStatus.AVAILABLE;
        }
    }

    private String formatDateTime(Date dateTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd-HH", Locale.getDefault());
        return dateFormat.format(dateTime);
    }
}