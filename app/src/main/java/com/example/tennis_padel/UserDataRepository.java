package com.example.tennis_padel;

// Singleton that allows always to get and set certain informations
public class UserDataRepository {
    private static UserDataRepository instance;
    private User user;
    private Club club;
    private boolean isFromLogin = false;

    private UserDataRepository() {}

    public static UserDataRepository getInstance() {
        if (instance == null) {
            instance = new UserDataRepository();
        }
        return instance;
    }
    public void setIsFromLogin(boolean isFromLogin){ this.isFromLogin = isFromLogin; }

    public boolean getIsFromLogin(){ return isFromLogin; }

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

