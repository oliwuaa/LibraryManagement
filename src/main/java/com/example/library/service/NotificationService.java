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
            message.setSubject("Powiadomienie o opóźnieniu zwrotu książki");
            message.setText("Drogi użytkowniku,\n\n" +
                    "Przypominamy, że termin zwrotu książki '" + loan.getCopy().getBook().getTitle() + "' już minął.\n" +
                    "Prosimy o jak najszybszy zwrot książki.\n\n" +
                    "Pozdrawiamy,\n" + loan.getCopy().getLibrary().getName());
            mailSender.send(message);
        } catch (MailException e) {
            e.printStackTrace();
        }
    }

    public void sendLoanSuccess(String toEmail, Loan loan) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Powiadomienie o wypożyczeniu książki");
            message.setText("Drogi użytkowniku,\n\n" +
                    "Wypożyczyłeś '" + loan.getCopy().getBook().getTitle() + "'.\n" +
                    "Data zwrotu powyższej pozycji mija : " + loan.getFormattedEndDate()  + ".\n\n" +
                    "Pozdrawiamy,\n" + loan.getCopy().getLibrary().getName());
            mailSender.send(message);
        } catch (MailException e) {
            e.printStackTrace();
        }
    }

    public void sendOneDayLeftNotification(String toEmail, Reservation reservation) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Przypomnienie o odebraniu zarezerwowanej pozycji");
            message.setText("Drogi użytkowniku,\n\n" +
                    "Przypominamy, że pozostał 1 dzień do odebrania zarezerwowanej ksiązki '" + reservation.getCopy().getBook().getTitle() + "'.\n" +
                    "Pozdrawiamy,\n" + reservation.getCopy().getLibrary().getName());
            mailSender.send(message);
        } catch (MailException e) {
            e.printStackTrace();
        }
    }

    public void sendCancelReservationNotification(String toEmail, Reservation reservation) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Powiadomienie o anulowaniu rezerwacji");
            message.setText("Drogi użytkowniku,\n\n" +
                    "Anulowaliśmy twoją rezerwację książki '" + reservation.getCopy().getBook().getTitle() + "'.\n" +
                    "Pozdrawiamy,\n" + reservation.getCopy().getLibrary().getName());
            mailSender.send(message);
        } catch (MailException e) {
            e.printStackTrace();
        }
    }

    public void sendAcceptedReservationNotification(String toEmail, Reservation reservation) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Powiadomienie o akceptacji rezerwacji");
            message.setText("Drogi użytkowniku,\n\n" +
                    "Pomyślnie zarezerwowałeś książke '" + reservation.getCopy().getBook().getTitle() + "'.\n" +
                    "Pamiętaj, że masz 2 dni na jej odebranie.\n" +
                    "Możesz odebrać tą książke do : " + reservation.getFormattedExpirationDate()  + ".\n\n" +
                    "Pozdrawiamy,\n" + reservation.getCopy().getLibrary().getName());
            mailSender.send(message);
        } catch (MailException e) {
            e.printStackTrace();
        }
    }
}
