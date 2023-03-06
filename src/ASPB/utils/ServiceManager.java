package ASPB.utils;

import ASPB.utils.interfaces.MyService;

/**
 * This class is used to register a the active service to be used by the rest of
 * the code.
 * 
 * @author Philipp Bonin
 */
public abstract class ServiceManager {

    private static MyService service = null;

    /**
     * Returns the active service or null if no service is registered.
     * 
     * @return the active service or null if no service is registered.
     */
    public static MyService getInstance() {
        return service;
    }

    /**
     * Registers the given service as the active service.
     * 
     * @param service the service to be registered.
     */
    public static void registerService(MyService service) {
        ServiceManager.service = service;
    }

    /**
     * Unregisters the active service.
     */
    public static void unregisterService() {
        ServiceManager.service = null;
    }

    /**
     * Logs the given message to the active service.
     * 
     * @param message the message to be logged.
     */
    public static void logInfo(String message) {
        if (service != null) {
            service.logInfo(message);
        }
    }

    /**
     * Logs the given message as an error to the active service.
     * 
     * @param message the error message to be logged.
     */
    public static void logError(String message) {
        if (service != null) {
            service.logError(message);
        }
    }
}
