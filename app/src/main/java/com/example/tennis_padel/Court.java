package com.example.tennis_padel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

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
    private String id;  // Unique identifier for the court
    private String name;
    private CourtStatus status;
    private CourtType type;
    private ArrayList<Reservation> reservations;

    public Court(String id, String name, CourtType type) {
        this.id = id;
        this.name = name;
        this.status = CourtStatus.AVAILABLE;
        this.type = type;
        this.reservations = new ArrayList<>();
    }

    public Court(String id, String name, CourtType type, CourtStatus status) {
        this.id = id;
        this.name = name;
        this.status = status;
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

    public void setReservations(ArrayList<Reservation> reservations) {
        this.reservations = reservations;
    }

    public ArrayList<Reservation> getReservations() {
        return reservations;
    }
}

