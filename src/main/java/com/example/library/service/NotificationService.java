package com.example.library.service;

import com.example.library.model.Loan;
import com.example.library.model.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final JavaMailSender mailSender;

    public void sendOverdueNotification(String toEmail, Loan loan) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Overdue Book Return Notification");
            message.setText("Dear User,\n\n" +
                    "This is a reminder that the return date for the book titled '" + loan.getCopy().getBook().getTitle() + "' has passed.\n" +
                    "Please return the book as soon as possible.\n\n" +
                    "Best regards,\n" + loan.getCopy().getLibrary().getName());
            mailSender.send(message);
        } catch (MailException e) {
            e.printStackTrace();
        }
    }

    public void sendLoanSuccess(String toEmail, Loan loan) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Book Loan Confirmation");
            message.setText("Dear User,\n\n" +
                    "You have successfully borrowed the book titled '" + loan.getCopy().getBook().getTitle() + "'.\n" +
                    "The return due date for this book is: " + loan.getFormattedEndDate()  + ".\n\n" +
                    "Best regards,\n" + loan.getCopy().getLibrary().getName());
            mailSender.send(message);
        } catch (MailException e) {
            e.printStackTrace();
        }
    }

    public void sendOneDayLeftNotification(String toEmail, Reservation reservation) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(" Reminder: 1 Day Left to Pick Up Your Reserved Book");
            message.setText("Dear User,\n\n" +
                    "This is a reminder that you have 1 day left to pick up the reserved book titled '" + reservation.getCopy().getBook().getTitle() + "'.\n" +
                    "Best regards,\n" + reservation.getCopy().getLibrary().getName());
            mailSender.send(message);
        } catch (MailException e) {
            e.printStackTrace();
        }
    }

    public void sendCancelReservationNotification(String toEmail, Reservation reservation) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Reservation Cancellation Notice");
            message.setText("Dear User,\n\n" +
                    "Your reservation for the book titled '" + reservation.getCopy().getBook().getTitle() + "' has been cancelled.\n" +
                    "Best regards,\n" + reservation.getCopy().getLibrary().getName());
            mailSender.send(message);
        } catch (MailException e) {
            e.printStackTrace();
        }
    }

    public void sendAcceptedReservationNotification(String toEmail, Reservation reservation) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Reservation Confirmed");
            message.setText("Dear User,\n\n" +
                    "Your reservation for the book titled '" + reservation.getCopy().getBook().getTitle() + "' has been successfully confirmed.\n" +
                    "Please note that you have 2 days to pick up the book.\n" +
                    "You can collect it until: " + reservation.getFormattedExpirationDate()  + ".\n\n" +
                    "Best regards,\n" + reservation.getCopy().getLibrary().getName());
            mailSender.send(message);
        } catch (MailException e) {
            e.printStackTrace();
        }
    }
}
