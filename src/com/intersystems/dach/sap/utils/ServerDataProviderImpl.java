package com.intersystems.dach.sap.utils;

import java.util.Properties;

import com.sap.conn.jco.ext.DataProviderException;
import com.sap.conn.jco.ext.ServerDataEventListener;
import com.sap.conn.jco.ext.ServerDataProvider;

public class ServerDataProviderImpl implements ServerDataProvider{
    /**
     * From these properties all necessary destination
     * data are gathered.
     */
    private Properties serverProperties;

    public ServerDataProviderImpl(Properties serverProperties){
        this.serverProperties = serverProperties;
    }

    @Override
    public Properties getServerProperties(String arg0) throws DataProviderException {
        return serverProperties;
    }

    @Override
    public void setServerDataEventListener(ServerDataEventListener arg0) { }

    @Override
    public boolean supportsEvents() {
        return false;
    }
    
}
