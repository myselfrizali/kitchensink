package org.quickstarts.kitchensink.util;

public class ApplicationConstants {
    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*(),.])[A-Za-z\\d!@#$%^&*(),.]{8,}$";
    public static final String EMAIL_REGEX = "^([a-zA-Z0-9._%-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$";
}
