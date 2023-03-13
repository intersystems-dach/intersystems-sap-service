package ASPB.sap.dataprovider;

import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.Environment;
import com.sap.conn.jco.ext.ServerDataProvider;

/**
 * This class is used to register and unregister the data providers.
 * 
 * @author Philipp Bonin
 * @version 1.0
 * 
 */
public abstract class DataProviderManager {

    private static DestinationDataProvider destinationDataProvider = null;
    private static ServerDataProvider serverDataProvider = null;

    /**
     * Registers the server data providers.
     * 
     * @param prov The server data provider.
     */
    public static void register(ServerDataProvider prov) {

        Environment.registerServerDataProvider(prov);
        serverDataProvider = prov;
    }

    /**
     * Registers the destination data provider.
     * 
     * @param prov The destination data provider.
     */
    public static void register(DestinationDataProvider prov) {
        Environment.registerDestinationDataProvider(prov);
        destinationDataProvider = prov;
    }

    /**
     * Unregisters all data providers.
     */
    public static void unregister() {
        Environment.unregisterDestinationDataProvider(destinationDataProvider);

        Environment.unregisterServerDataProvider(serverDataProvider);
    }

}
