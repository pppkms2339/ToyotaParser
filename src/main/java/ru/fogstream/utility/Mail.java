package ru.fogstream.utility;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Mail {

    public void send(String subject, String text) {
        try {
            Properties prop = new Properties();
            prop.put("mail.smtp.host", "smtp.yandex.ru");
            prop.put("mail.smtp.port", "465");
            prop.put("mail.smtp.auth", "true");
            prop.put("mail.smtp.socketFactory.port", "465");
            prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            Session session = Session.getInstance(prop, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("fogstream-debug", "123456Qq");
                }
            });
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("fogstream-debug@yandex.ru"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("pppkms2339@gmail.com"));
            message.setSubject(subject);
            message.setText(text);
            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
