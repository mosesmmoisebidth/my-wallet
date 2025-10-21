package com.fullStack.expenseTracker.services.impls;

import com.fullStack.expenseTracker.models.User;
import com.fullStack.expenseTracker.services.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class EmailNotificationService implements NotificationService {

    private final WebClient webClient;

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    public EmailNotificationService(@Value("${resend.base.url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public void sendUserRegistrationVerificationEmail(User user) {
        String subject = "Welcome to MyWallet! Please verify your email";
        String htmlContent = buildVerificationEmailContent(user, false);
        sendEmail(user.getEmail(), subject, htmlContent);
    }

    @Override
    public void sendForgotPasswordVerificationEmail(User user) {
        String subject = "Reset your MyWallet password";
        String htmlContent = buildVerificationEmailContent(user, true);
        sendEmail(user.getEmail(), subject, htmlContent);
    }

    /**
     * Builds a friendly and human-like email content with a clear structure,
     * proper branding tone, and simple language that avoids spam triggers.
     */
    private String buildVerificationEmailContent(User user, boolean isForgotPassword) {
        String greeting = "Hello " + user.getUsername() + ",";
        String intro = isForgotPassword
                ? "We received a request to reset your MyWallet password."
                : "Welcome to MyWallet! We're excited to have you join our community.";
        String instruction = isForgotPassword
                ? "To continue, please use the verification code below to confirm your request:"
                : "To activate your account, please use the verification code below:";
        String closing = "If you didn’t request this, you can safely ignore this email.";

        return """
                <html>
                    <body style="font-family: Arial, sans-serif; color: #333;">
                        <div style="max-width: 600px; margin: auto; padding: 20px; border-radius: 10px; background-color: #fafafa; border: 1px solid #eee;">
                            <h2 style="color: #2b6cb0;">MyWallet</h2>
                            <p>%s</p>
                            <p>%s</p>
                            <p>%s</p>
                            <div style="text-align: center; margin: 20px 0;">
                                <p style="font-size: 20px; font-weight: bold; background-color: #2b6cb0; color: #fff; display: inline-block; padding: 10px 20px; border-radius: 6px;">
                                    %s
                                </p>
                            </div>
                            <p>This code will expire in 15 minutes.</p>
                            <p>%s</p>
                            <br>
                            <p>Warm regards,</p>
                            <p><strong>The MyWallet Team</strong><br>
                            <a href="https://mywallet.example.com" style="color:#2b6cb0; text-decoration:none;">mywallet.example.com</a></p>
                        </div>
                    </body>
                </html>
                """.formatted(greeting, intro, instruction, user.getVerificationCode(), closing);
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            webClient.post()
                    .uri("/emails")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + resendApiKey)
                    .body(Mono.just(new EmailRequest(fromEmail, to, subject, htmlContent)), EmailRequest.class)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // blocking for simplicity
        } catch (Exception e) {
            System.err.println("❌ Failed to send email via Resend: " + e.getMessage());
        }
    }

    private static class EmailRequest {
        private final String from;
        private final String to;
        private final String subject;
        private final String html;

        public EmailRequest(String from, String to, String subject, String html) {
            this.from = from;
            this.to = to;
            this.subject = subject;
            this.html = html;
        }

        public String getFrom() { return from; }
        public String getTo() { return to; }
        public String getSubject() { return subject; }
        public String getHtml() { return html; }
    }
}
