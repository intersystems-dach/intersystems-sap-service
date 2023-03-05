package ASPB.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * A class for logging messages to the console and/or file
 * 
 * @author Philipp Bonin
 * @version 1.0
 *
 */
public abstract class Logger {

    // The log file
    private static File file = null;

    /**
     * Logs a message to a file
     * 
     * @param message the message to log
     */
    public static void log(String message) {
        if (file == null || message == null)
            return;

        message = message.replace("\n", " ");
        message = "[" + new java.util.Date() + "] " + message;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(message);
            bw.newLine();
            bw.close();
        } catch (Exception e) {
            file = null;
        }
    }

    /**
     * Logs an error message to a file
     * 
     * @param message the error message to log
     */
    public static void error(String message) {

        if (file == null || message == null)
            return;

        message = message.replace("\n", " ");
        message = "ERROR[" + new java.util.Date() + "] " + message;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(message);
            bw.newLine();
            bw.close();
        } catch (Exception e) {
            file = null;
        }

    }

    /**
     * Clears the log file
     */
    public static void clear() {
        if (file == null)
            return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            bw.write("");
            bw.close();
        } catch (Exception e) {
            file = null;
        }
    }

    /**
     * Sets the path of the log file
     * 
     * @param filePath the path of the log file
     */
    public static void setFilePath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            file = null;
            return;
        }

        File f = new File(filePath);
        if (f.exists() && !f.isDirectory() && f.canWrite()) {
            file = f;
            return;
        }
        file = null;
    }

}
