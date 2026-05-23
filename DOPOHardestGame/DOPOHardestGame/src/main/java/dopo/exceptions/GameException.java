package dopo.exceptions;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

/**
 * Custom exception for DOPO Hardest Game errors.
 * Also logs errors to a file for developers.
 */
public class GameException extends Exception {

    private static final String LOG_FILE = "game_errors.log";

    public GameException(String message) {
        super(message);
        log(message, null);
    }

    public GameException(String message, Throwable cause) {
        super(message, cause);
        log(message, cause);
    }

    private void log(String message, Throwable cause) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            pw.println("[" + LocalDateTime.now() + "] ERROR: " + message);
            if (cause != null) {
                pw.println("  Caused by: " + cause.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Could not write to log file: " + e.getMessage());
        }
    }
}
