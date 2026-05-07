package com.tobi.qbank.test;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailTest {

    private static final String HOST = "smtp.gmail.com";
    private static final String PORT = "465";
    private static final String USER = "rook6859@gmail.com";
    private static final String PASS = "pbfntnrzmxdsldnq";

    public static void main(String[] args) {
        try {
            System.out.println("Testing Gmail SMTP over SSL (port 465)...");

            String otp = String.format("%06d", (int) (Math.random() * 1000000));
            sendEmail("rook6859@gmail.com", otp);

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void sendEmail(String to, String otp) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.auth", "true");

        // Implicit SSL on port 465 — do NOT enable STARTTLS here
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.trust", HOST);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");

        // Helpful timeouts so it fails fast instead of hanging
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        // Uncomment this line if you want to see the full SMTP conversation
        // props.put("mail.debug", "true");

        Session session = Session.getInstance(props,
            new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(USER, PASS);
                }
            });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USER, "QBank"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Your OTP Code");
            message.setText("Your One-Time Password is:\n\n" + otp + "\n\nThis code expires in 5 minutes.");

            Transport.send(message);
            System.out.println("OTP '" + otp + "' sent successfully to " + to);

        } catch (MessagingException e) {
            System.err.println("Send failed: " + e.getMessage());
            throw e;
        } catch (java.io.UnsupportedEncodingException e) {
            throw new MessagingException("Bad sender name encoding", e);
        }
    }
}