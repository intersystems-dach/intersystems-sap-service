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

    // Register this data provider in the static block
    static {
        if (!Environment.isDestinationDataProviderRegistered()) {
            try {
                singletonInstance = new DestinationDataProviderImpl();
                Environment.registerDestinationDataProvider(singletonInstance);
            } catch (Exception e) {
                // Disable Data provider by setting instance to null
                singletonInstance = null;
            }            
        }
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

        if (singletonInstance.destinationProperties.containsKey(destinationName)) {
            throw new IllegalStateException("Properties for destination '" + destinationName + "' already set.");
        }
        singletonInstance.destinationProperties.put(destinationName, properties);
        if (singletonInstance.destinationDataEventListener != null) {
            singletonInstance.destinationDataEventListener.updated(destinationName);
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

        Properties properties = singletonInstance.destinationProperties.remove(destinationName);
        if (singletonInstance.destinationDataEventListener != null) {
            singletonInstance.destinationDataEventListener.deleted(destinationName);
        }

        return properties;
    }

    @Override
    public Properties getDestinationProperties(String destinationName) throws DataProviderException {
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
