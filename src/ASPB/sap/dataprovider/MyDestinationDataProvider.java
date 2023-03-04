package ASPB.sap.dataprovider;

import java.util.Properties;

import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.Environment;

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
     * Flag that indicates if the method was already called.
     */
    private static boolean registered = false;

    /**
     * Registers the given {@code provider} as destination data provider at the
     * {@link Environment}.
     * 
     * @param provider
     *                 the destination data provider to register
     */
    private static void register(MyDestinationDataProvider provider) {
        // Check if a registration has already been performed.
        if (registered == false) {
            // Register the destination data provider.
            Environment.registerDestinationDataProvider(provider);
            registered = true;
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