package ASPB.sap.dataprovider;

import java.util.Properties;

import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.Environment;

import ASPB.utils.Logger;

public class MyDestinationDataProvider implements DestinationDataProvider {

    /**
     * From these properties all necessary destination
     * data are gathered.
     */
    private Properties properties;

    /**
     * Initializes this instance with the given {@code properties}.
     * Performs a self-registration in case no instance of a
     * {@link MyDestinationDataProvider} is registered so far
     * (see {@link #register(MyDestinationDataProvider)}).
     * 
     * @param properties
     *                   the {@link #properties}
     * 
     */
    public MyDestinationDataProvider(Properties properties) {
        super();
        this.properties = properties;
        // Try to register this instance (in case there is not already another
        // instance registered).
        register(this);
    }

    /**
     * Registers the given {@code provider} as destination data provider at the
     * {@link Environment}.
     * 
     * @param provider
     *                 the destination data provider to register
     */
    private static void register(MyDestinationDataProvider provider) {
        // Check if a registration has already been performed.
        // Register the destination data provider.
        try {
            DataProviderManager.register(provider);
        } catch (Exception e) {
            // This exception is thrown in case the destination data provider
            // is already registered.
            // In this case we can ignore the exception.
            Logger.error("Could not register Destination Data Provider:" + e.getMessage());
        }
    }

    @Override
    public Properties getDestinationProperties(String destinationName) {
        return properties;
    }

    @Override
    public void setDestinationDataEventListener(DestinationDataEventListener listener) {
    }

    @Override
    public boolean supportsEvents() {
        return false;
    }
}