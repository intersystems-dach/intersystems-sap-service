package ASPB.utils.interfaces;

import java.lang.reflect.Field;

import com.intersystems.jdbc.IRIS;

/**
 * A service interface to access the database and the settings from the active
 * service.
 * 
 * @author Philipp Bonin
 * @version 1.0
 * 
 */
public interface MyService {

    /**
     * Get the connection to the database
     * 
     * @return the connection
     */
    IRIS getConnection();

    /**
     * Get the setting with the given name
     * 
     * @param name the name of the setting
     * @return the setting
     */
    Field getSetting(String name);

    /**
     * Get all settings
     * 
     * @return all settings
     */
    Field[] getAllSettings();

    /**
     * Log an info message to the production
     * 
     * @param message the message to log
     */
    void logInfo(String message);

    /**
     * Log an error message to the production
     * 
     * @param message the message to log
     */
    void logError(String message);

    /**
     * Get the server instance
     * 
     * @return the server instance
     */
    MyServer getServer();
}
