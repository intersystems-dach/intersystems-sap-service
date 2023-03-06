package ASPB.utils.interfaces;

/**
 * An Interface for a server
 * 
 * @author Philipp Bonin
 * @version 1.0
 * 
 */
public interface MyServer {

    /**
     * Register a callback to the starter service
     * 
     * @param callback the callback to register
     */
    void registerCallback(Callback<?> callback);

    /**
     * Start the server
     * 
     * @return true if the server was started successfully, false otherwise
     */
    boolean start();

    /**
     * Stop the server
     * 
     * @return true if the server was stopped successfully, false otherwise
     */
    boolean stop();

    /**
     * Check if the server is running
     * 
     * @return true if the server is running, false otherwise
     */
    boolean isRunning();

    /**
     * Set a property
     * 
     * @param key   the key of the property
     * @param value the value of the property
     */
    void setProperty(String key, String value);

    /**
     * Checks if all properties are set before starting the server
     * 
     * @return true if all properties are set, false otherwise
     */
    boolean checkIfAllPropertiesAreSet();

}
