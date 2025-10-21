package com.fullStack.expenseTracker.services;

import com.fullStack.expenseTracker.models.User;
import org.springframework.stereotype.Service;

@Service
public interface NotificationService {

    /**
     * Sends a verification email to the user after registration.
     *
     * @param user The user to send the verification email to.
     */
    void sendUserRegistrationVerificationEmail(User user);

    /**
     * Sends a verification email to the user for password reset.
     *
     * @param user The user to send the password reset verification email to.
     */
    void sendForgotPasswordVerificationEmail(User user);
}
