package com.example.tennis_padel;

import java.util.ArrayList;
import java.util.Date;

enum CourtStatus {
    AVAILABLE,
    RESERVED,
    SEMI_RESERVED
}

enum CourtType {
    INDOOR,
    OUTDOOR
}


public class Court {
    private String name;
    private CourtStatus status;
    private CourtType type;
    private ArrayList<Reservation> reservations;

    public Court(String name, CourtType type) {
        this.name = name;
        this.status = CourtStatus.AVAILABLE;
        this.type = type;
        this.reservations = new ArrayList<>();
    }

    public Court(String name, CourtType type, CourtStatus status) {
        this.name = name;
        this.status = status;
        this.type = type;
        this.reservations = new ArrayList<>();
    }

    public boolean isAvailable(Date dateTime) {
        for (Reservation reservation : reservations) {
            if (reservation.getDateTime().equals(dateTime)) {
                return false;
            }
        }
        return true;
    }

    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
        reservation.updateCourtStatus();
    }

    public CourtStatus getStatus() {
        return status;
    }

    public void setStatus(CourtStatus status) {
        this.status = status;
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
}

