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

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isAvailable(Date dateTime) {
        String formattedDate = formatDateTime(dateTime);
        for (Reservation reservation : reservations) {
            if (reservation.getDateTime().equals(formattedDate)) {
                return false;
            }
        }
        return true;
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
        for (Reservation reservation : reservations) {
            if (reservation.getDateTime().equals(formattedDate)) {
                playerCount++;
            }
        }

        if (playerCount == 0) {
            return CourtStatus.AVAILABLE;
        } else if (playerCount < 4) {
            return CourtStatus.SEMI_RESERVED;
        } else {
            return CourtStatus.RESERVED;
        }
    }

    public int getReservationsNumber(Date dateTime) {
        String formattedDate = formatDateTime(dateTime);
        int playerCount = 0;
        for (Reservation reservation : reservations) {
            if (reservation.getDateTime().equals(formattedDate)) {
                playerCount++;
            }
        }
        return playerCount;
    }

    private String formatDateTime(Date dateTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd-HH", Locale.getDefault());
        return dateFormat.format(dateTime);
    }
}