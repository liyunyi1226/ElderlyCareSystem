package com.elderlycare.common;

import jakarta.servlet.http.HttpSession;
import model.User;

public final class WebUtils {

    public static final String CURRENT_USER = "currentUser";

    private WebUtils() {}

    public static User currentUser(HttpSession session) {
        return (User) session.getAttribute(CURRENT_USER);
    }
}
