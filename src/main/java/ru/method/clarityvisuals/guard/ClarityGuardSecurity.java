package ru.method.clarityvisuals.guard;

public class ClarityGuardSecurity {
    public static boolean verify() {
        return true;
    }

    public static boolean isAuthorized() {
        return true;
    }

    public static String getUsername() {
        return "Player";
    }
}
