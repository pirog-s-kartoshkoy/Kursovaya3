package com.example.kursovaya3;

public class UserSession {
    private static int userId;
    private static String login;

    public static void startSession(int id, String userLogin) {
        userId = id;
        login = userLogin;
    }

    public static int getUserId() {
        return userId;
    }

    public static String getLogin() {
        return login;
    }

    public static void clearSession() {
        userId = 0;
        login = null;
    }
}