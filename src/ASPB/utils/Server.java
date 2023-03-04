package ASPB.utils;

/**
 * An Interface for a server
 */
public interface Server {

    void registerCallback(Callback<?> callback);

    boolean start();

    boolean stop();

    boolean isRunning();

    void setProperty(String key, String value);

}
