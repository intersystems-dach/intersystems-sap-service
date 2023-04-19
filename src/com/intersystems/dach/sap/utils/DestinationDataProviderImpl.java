package com.intersystems.dach.sap.utils;

import java.util.HashMap;
import java.util.Properties;

import com.sap.conn.jco.ext.DataProviderException;
import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.Environment;

/**
 * Destination Data provider implementation for SAP JCo.
 * 
 * @author Philipp Bonin, Andreas Schütz
 * @version 1.0
 * 
 */
public final class DestinationDataProviderImpl implements DestinationDataProvider {

    /**
     * From these properties all necessary destination
     * data are gathered.
     */
    private static DestinationDataProviderImpl singletonInstance = null;

    private HashMap<String,Properties> destinationProperties = new HashMap<String, Properties>();
    private DestinationDataEventListener destinationDataEventListener = null;


    // Make this a singleton class
    private DestinationDataProviderImpl() { }

    private static DestinationDataProviderImpl getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new DestinationDataProviderImpl();
        }
        return singletonInstance;
    }
    
    /**
     * Registers the destination data handler instance with the SAP JCo environment.
     * @return
     */
    public static boolean tryRegister() {
        if (!Environment.isDestinationDataProviderRegistered()) {
            Environment.registerDestinationDataProvider(getInstance());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Unregisters the destination data handler instance from the SAP JCo environment.
     * @return True if the handler was unregistered, false if not.
     */
    public static boolean unregister() {
        if (Environment.isDestinationDataProviderRegistered()) {
            Environment.unregisterDestinationDataProvider(getInstance());

            while (Environment.isDestinationDataProviderRegistered()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) { }
            }

            return true;
        }
        return false;
    }

    /**
     * Adds properties for the specified destination name to the data provider.
     * @param destinationName destination name whose properties are to be removed from the provider.
     * @param properties properties to be added to this dat provider.
     * @throws IllegalStateException thrown if data provider has not been initialized or properties for the specified server name aleady exist.
     */
    public static void setProperties(String destinationName, Properties properties) throws IllegalStateException{
        if (singletonInstance == null) {
            throw new IllegalStateException("DestionationDataHandler is not initialized.");
        }

        TraceManager.traceMessage("a");
        if (getInstance().destinationProperties.containsKey(destinationName)) {
            throw new IllegalStateException("Properties for destination '" + destinationName + "' already set.");
        }
        TraceManager.traceMessage("b");
        getInstance().destinationProperties.put(destinationName, properties);
        TraceManager.traceMessage("c");
        if (getInstance().destinationDataEventListener != null) {
            getInstance().destinationDataEventListener.updated(destinationName);
        }
    }

    /**
     * Removes properties for the specified destination name from the data provider if present.
     * @param destinationName destination names whose properties are to be removed from the provider.
     * @return the previous properties associated with destination name, or null if there were no properties for the destination name.
     * @throws IllegalStateException thrown if data provider has not been initialized or properties for the specified server name aleady exist.
     */
    public static Properties deleteProperties(String destinationName) throws IllegalStateException {
        if (singletonInstance == null) {
            throw new IllegalStateException("DestionationDataHandler is not initialized.");
        }

        Properties properties = getInstance().destinationProperties.remove(destinationName);
        if (getInstance().destinationDataEventListener != null) {
            getInstance().destinationDataEventListener.deleted(destinationName);
        }
        if (getInstance().destinationProperties.isEmpty()) {
            unregister();
        }

        return properties;
    }

    @Override
    public Properties getDestinationProperties(String destinationName) throws DataProviderException {
        TraceManager.traceMessage("DestionationDataProvider getDestinationProperties called with param:" + destinationName);
        if (destinationProperties.containsKey(destinationName)) {
            return destinationProperties.get(destinationName);
        } else {
            throw new DataProviderException(DataProviderException.Reason.INTERNAL_ERROR, new IllegalArgumentException("Destination data for '" + destinationName + "' not found."));
        }        
    }

    @Override
    public void setDestinationDataEventListener(DestinationDataEventListener listener) { 
        destinationDataEventListener = listener;
    }

    @Override
    public boolean supportsEvents() {
        return true;
    }

    
}
