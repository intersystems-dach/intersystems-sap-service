package ASPB.sap.dataprovider;

import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.Environment;
import com.sap.conn.jco.ext.ServerDataProvider;

public abstract class DataProviderManager {

    private static DestinationDataProvider destinationDataProvider = null;
    private static ServerDataProvider serverDataProvider = null;

    public static void register(ServerDataProvider prov) {

        Environment.registerServerDataProvider(prov);
        serverDataProvider = prov;
    }

    public static void register(DestinationDataProvider prov) {
        Environment.registerDestinationDataProvider(prov);
        destinationDataProvider = prov;
    }

    public static void unregister() {
        Environment.unregisterDestinationDataProvider(destinationDataProvider);

        Environment.unregisterServerDataProvider(serverDataProvider);
    }

}
