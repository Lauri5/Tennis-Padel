package com.example.tennis_padel;

public class Reservation {
    String teacher;
    Court court;

    public Reservation(String teacher, Court court) {
        this.teacher = teacher;
        this.court = court;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public Court getCourt() {
        return court;
    }

    public void setCourt(Court court) {
        this.court = court;
    }
}
