package ASPB.sap.dataprovider;

import java.util.Properties;

import com.sap.conn.jco.ext.Environment;
import com.sap.conn.jco.ext.ServerDataEventListener;
import com.sap.conn.jco.ext.ServerDataProvider;

import ASPB.utils.Logger;

public class MyServerDataProvider implements ServerDataProvider {

    /**
     * From these properties all necessary destination
     * data are gathered.
     */
    private Properties properties;

    /**
     * Initializes this instance with the given {@code properties}.
     * Performs a self-registration in case no instance of a
     * {@link MyServerDataProvider} is registered so far
     * (see {@link #register(MyServerDataProvider)}).
     * 
     * @param properties
     *                   the {@link #properties}
     * 
     */
    public MyServerDataProvider(Properties properties) {
        super();
        this.properties = properties;
        // Try to register this instance (in case there is not already another
        // instance registered).
        register(this);
    }

    /**
     * Registers the given {@code provider} as server data provider at the
     * {@link Environment}.
     * 
     * @param provider
     *                 the server data provider to register
     */
    private static void register(MyServerDataProvider provider) {
        // Check if a registration has already been performed.
        // Register the destination data provider.
        try {
            DataProviderManager.register(provider);
        } catch (Exception e) {
            // This exception is thrown in case the destination data provider
            // is already registered.
            // In this case we can ignore the exception.
            Logger.error("Could not register Server Data Provider:" + e.getMessage());
        }
    }

    @Override
    public Properties getServerProperties(String serverName) {
        return properties;
    }

    @Override
    public void setServerDataEventListener(ServerDataEventListener listener) {
    }

    @Override
    public boolean supportsEvents() {
        return false;
    }
}