package com.intersystems.dach.sap.utils;

import java.util.HashMap;
import java.util.Properties;

import com.sap.conn.jco.ext.DataProviderException;
import com.sap.conn.jco.ext.Environment;
import com.sap.conn.jco.ext.ServerDataEventListener;
import com.sap.conn.jco.ext.ServerDataProvider;

/**
 * Server Data provider implementation for SAP JCo.
 * 
 * @author Philipp Bonin, Andreas Schütz
 * @version 1.0
 * 
 */
public class ServerDataProviderImpl implements ServerDataProvider{
    /**
     * From these properties all necessary destination
     * data are gathered.
     */
    private static ServerDataProviderImpl singletonInstance = null;

    private HashMap<String,Properties> serverProperties = new HashMap<String, Properties>();
    private ServerDataEventListener serverDataEventListener = null;

    // Make this a singleton class
    private ServerDataProviderImpl() { }

    // Register this data provider in the static block
    static {
        if (!Environment.isServerDataProviderRegistered()) {
            try {
                singletonInstance = new ServerDataProviderImpl();
                Environment.registerServerDataProvider(singletonInstance);
            } catch (Exception e) {
                // Disable Data provider by setting instance to null
                singletonInstance = null;
            }
        }
    }

    /**
     * Adds properties for the specified server name to the data provider.
     * @param serverName server name whose properties are to be removed from the provider.
     * @param properties properties to be added to this dat provider.
     * @throws IllegalStateException thrown if data provider has not been initialized or properties for the specified server name aleady exist.
     */
    public static void setProperties(String serverName, Properties properties) throws IllegalStateException{
        if (singletonInstance == null) {
            throw new IllegalStateException("ServerDataHandler is not initialized.");
        }

        if (singletonInstance.serverProperties.containsKey(serverName)) {
            throw new IllegalStateException("Properties for server '" + serverName + "' already set.");
        }
        singletonInstance.serverProperties.put(serverName, properties);
        if (singletonInstance.serverDataEventListener != null) {
            singletonInstance.serverDataEventListener.updated(serverName);
        }
    }

    /**
     * Removes properties for the specified server name from the data provider if present.
     * @param serverName server names whose properties are to be removed from the provider.
     * @return the previous properties associated with server name, or null if there were no properties for the server name.
     * @throws IllegalStateException thrown if data provider has not been initialized or properties for the specified server name aleady exist.
     */
    public static Properties deleteProperties(String serverName) throws IllegalStateException {
        if (singletonInstance == null) {
            throw new IllegalStateException("ServerDataHandler is not initialized.");
        }

        Properties properties = singletonInstance.serverProperties.remove(serverName);
        if (singletonInstance.serverDataEventListener != null) {
            singletonInstance.serverDataEventListener.deleted(serverName);
        }
        return properties;
    }


    @Override
    public Properties getServerProperties(String serverName) throws DataProviderException {
        if (serverProperties.containsKey(serverName)) {
            return serverProperties.get(serverName);
        } else {
            throw new DataProviderException(DataProviderException.Reason.INTERNAL_ERROR, new IllegalArgumentException("Server data for '" + serverName + "' not found."));
        }  
    }

    @Override
    public void setServerDataEventListener(ServerDataEventListener listener) {
        serverDataEventListener = listener;
     }

    @Override
    public boolean supportsEvents() {
        return true;
    }
    
}
