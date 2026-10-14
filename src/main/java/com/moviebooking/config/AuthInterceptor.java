package com.moviebooking.config;

import com.moviebooking.controller.AuthController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        Object user = (session != null) ? session.getAttribute(AuthController.SESSION_USER_KEY) : null;

        if (user == null) {
            response.sendRedirect("/login.html");
            return false;
        }

        return true;
    }
}
