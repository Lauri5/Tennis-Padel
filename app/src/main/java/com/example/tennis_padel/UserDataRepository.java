package com.example.tennis_padel;

public class UserDataRepository {
    private static UserDataRepository instance;
    private User user;
    private Club club;

    private UserDataRepository() {}

    public static UserDataRepository getInstance() {
        if (instance == null) {
            instance = new UserDataRepository();
        }
        return instance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }
}

