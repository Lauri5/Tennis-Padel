package com.example.tennis_padel;

public class UserDataRepository {
    private static UserDataRepository instance;
    private User user;

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
}

