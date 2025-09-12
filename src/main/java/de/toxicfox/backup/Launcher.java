package de.toxicfox.backup;

import de.toxicfox.backup.cli.CliUserInterface;
import de.toxicfox.backup.core.Backup;
import de.toxicfox.backup.core.IUserInterface;
import de.toxicfox.backup.core.SessionResult;
import de.toxicfox.backup.core.util.FileModificationCompression;
import de.toxicfox.backup.core.util.FileModificationEncryption;
import de.toxicfox.backup.core.util.FileUtil;
import de.toxicfox.backup.tui.TuiUserInterface;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Launcher {
    private static final Pattern scheduledOptions = Pattern.compile("(\\d{2}):(\\d{2})/(\\d+)");

    public static void main(String[] args) throws Exception {
        ArgumentParser parser = new ArgumentParser(args, new String[]{"--interface", "--scheduled", "--restore", "--compression", "--encryption", "--notify"});

        String interfaceType = parser.consumeOption("--interface", "tui");
        IUserInterface user = switch (interfaceType) {
            case "tui" -> new TuiUserInterface();
            case "cli" -> new CliUserInterface();
            default -> throw new IllegalArgumentException("Invalid interface type");
        };

        FileUtil fileUtil = new FileUtil();

        if (parser.isOption("--compression")) {
            fileUtil.addModification(new FileModificationCompression());
        }

        if (parser.isOption("--encryption")) {
            String keyFile = parser.consumeOption("--encryption", null);
            fileUtil.addModification(new FileModificationEncryption(new File(keyFile), user));
        }


        // Needs to be final because of a lambda
        final SMTP[] smtp = new SMTP[] { null };
        if (parser.isOption("--notify")) {
            smtp[0] = new SMTP(parser.consumeOption("--notify", null));
            smtp[0].load();
        }
        

        if (parser.isOption("--restore")) {
            Backup.restore(user, fileUtil);
        } else {
            if (parser.isOption("--scheduled")) {
                String options = parser.consumeOption("--scheduled", "06:30/24");
                Matcher matcher = scheduledOptions.matcher(options);

                if (!matcher.matches()) {
                    throw new IllegalArgumentException("Invalid syntax for --scheduled");
                }
                
                int hours = Integer.parseInt(matcher.group(1));
                int minutes = Integer.parseInt(matcher.group(2));
                int interval = Integer.parseInt(matcher.group(3));

                user.log(String.format("Scheduled backup started (Starting at %d:%d running every %d hours)", hours, minutes, interval));
                Scheduler.every(hours, minutes, interval, () -> {
                    startBackup(smtp[0], fileUtil, user);
                });
            } else {
                startBackup(smtp[0], fileUtil, user);
            }
        }
    }

    private static void startBackup(SMTP smtp, FileUtil fileUtil, IUserInterface user) {
        try {
            SessionResult result = Backup.backup(user, fileUtil);
            if (smtp != null) {
                smtp.notifySuccess(result);;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (smtp != null) {
                smtp.notifyError(e);
            }
        }
    }

}
