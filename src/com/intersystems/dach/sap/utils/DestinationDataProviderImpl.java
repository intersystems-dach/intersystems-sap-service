package com.intersystems.dach.sap.utils;

import java.util.Properties;

import com.sap.conn.jco.ext.DataProviderException;
import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;

/**
 * Destination Data provider implementation for SAP JCo.
 * 
 * @author Philipp Bonin, Andreas Schütz
 * @version 1.0
 * 
 */
public class DestinationDataProviderImpl implements DestinationDataProvider {

    /**
     * From these properties all necessary destination
     * data are gathered.
     */
    private Properties destinationProperties;

    public DestinationDataProviderImpl(Properties destinationProperties) {
        this.destinationProperties = destinationProperties;
    }

    @Override
    public Properties getDestinationProperties(String arg0) throws DataProviderException {
        return destinationProperties;
    }

    @Override
    public void setDestinationDataEventListener(DestinationDataEventListener arg0) { }

    @Override
    public boolean supportsEvents() {
        return false;
    }

    
}
