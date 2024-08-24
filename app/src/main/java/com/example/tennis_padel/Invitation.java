package com.example.tennis_padel;

public class Invitation {

    private String id, courtId, courtName, time, inviterId, status,  inviteeId, courtType;

    // Constructor, getters, and setters

    public Invitation() {
    }

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

    public String getCourtName() {
        return courtName;
    }

    public void setCourtName(String courtName) {
        this.courtName = courtName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getInviterId() {
        return inviterId;
    }

    public void setInviterId(String inviterId) {
        this.inviterId = inviterId;
    }

    public String getInviteeId() {
        return inviteeId;
    }

    public void setInviteeId(String inviteeId) { this.inviteeId = inviteeId; }

    public String getCourtType() { return courtType; }

    public void setCourtType(String courtType) { this.courtType = courtType; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
}