package com.example.tennis_padel;

enum CourtStatus {
    AVAILABLE,
    RESERVED,
    SEMI_RESERVED
}

public class Court {
    private String name;
    private CourtStatus status;

    public Court(String name) {
        this.name = name;
        this.status = CourtStatus.AVAILABLE;
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
}
