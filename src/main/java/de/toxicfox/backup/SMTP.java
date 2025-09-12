package de.toxicfox.backup;

import de.toxicfox.backup.core.SessionResult;
import de.toxicfox.config.ConfigFile;
import de.toxicfox.config.auto.Saved;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.File;
import java.util.Properties;



public class SMTP extends ConfigFile {
    @Saved
    private String serverHostname = "mail.toxicfox.de";
    @Saved
    private int serverPort = 465;
    @Saved
    private String sourceMailAddress = "server@glowman554.de";
    @Saved
    private String password = "";

    private String recipient;

    public SMTP(String recipient) {
        super(new File("smtp.json"));
        setSaveAfterLoad(true);

        this.recipient = recipient;
    }

    @Override
    public void load() {
        super.load();
    }

    private Session start() {
        Properties props = new Properties();
        props.put("mail.smtp.host", serverHostname);
        props.put("mail.smtp.socketFactory.port", String.valueOf(serverPort));
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", String.valueOf(serverPort));

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sourceMailAddress, password);
            }
        });
        
        session.setDebug(true);

        return session;
    }

    public void notifySuccess(SessionResult result) {
        try {
            Session session = start();
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sourceMailAddress));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject("Backup job succeeded");

            String msg = "The backup job succeeded.\n";
            msg += String.format("Session ID: %d\n", result.getTimestamp());
            msg += String.format("Changed files: %d (%s)\n", result.getChangedFiles(), result.getChangedFilesSizeGB());
            msg += String.format("Deleted files: %d\n", result.getDeletions());
         
            if (result.getFailedFiles().size() > 0) {
                msg += "\nFailed files:\n";
                for (String file : result.getFailedFiles()) {
                    msg += file + "\n";
                }
            }

            message.setText(msg);
            
            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void notifyError(Exception result) {
        try {
            Session session = start();
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sourceMailAddress));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject("Backup job failed");

            String msg = "The backup job encountered an error.\n\n";
            msg += String.format("Exception: %s%n", result.getClass().getName());
            msg += String.format("Message: %s%n%n", result.getMessage());
            msg += "Stacktrace:\n";

            for (StackTraceElement element : result.getStackTrace()) {
                msg += String.format("  at %s%n", element.toString());
            }

            message.setText(msg);

            Transport.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
