package com.intersystems.dach.sap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import com.intersystems.dach.sap.handlers.JCoServerFunctionHandlerImpl;
import com.intersystems.dach.sap.handlers.SAPServerImportDataHandler;
import com.intersystems.dach.sap.handlers.SAPServerErrorHandler;
import com.intersystems.dach.sap.handlers.SAPServerExceptionHandler;
import com.intersystems.dach.sap.handlers.SAPServerStateHandler;
import com.intersystems.dach.sap.utils.DestinationDataProviderImpl;
import com.intersystems.dach.sap.utils.ServerDataProviderImpl;
import com.intersystems.dach.sap.handlers.JCoServerTIDHandlerImpl;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.Environment;
import com.sap.conn.jco.ext.ServerDataProvider;
import com.sap.conn.jco.server.DefaultServerHandlerFactory;
import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerContextInfo;
import com.sap.conn.jco.server.JCoServerErrorListener;
import com.sap.conn.jco.server.JCoServerExceptionListener;
import com.sap.conn.jco.server.JCoServerFactory;
import com.sap.conn.jco.server.JCoServerState;
import com.sap.conn.jco.server.JCoServerStateChangedListener;
import com.sap.conn.jco.server.JCoServerTIDHandler;

/**
 * A Server to receive messages from a SAP system using the
 * {@link com.sap.conn.jco.server.JCoServer}.
 * 
 * @author Philipp Bonin, Andreas Schütz
 * @version 1.0
 * 
 */
public class SAPServer implements JCoServerErrorListener,
        JCoServerExceptionListener,
        JCoServerStateChangedListener {

    private SAPServerImportDataHandler importDataHandler;

    private JCoServer jCoServer;

    private static DestinationDataProvider destinationDataProvider = null;
    private static ServerDataProvider serverDataProvider = null;

    private boolean useJson;

    // Event handlers
    private Collection<SAPServerErrorHandler> errorHandlers;
    private Collection<SAPServerExceptionHandler> exceptionHandlers;
    private Collection<SAPServerStateHandler> stateHandlers;

    /**
     * Initializes the server in XML mode.
     * 
     * @param settings  SAP server settings.
     * @param importDataHandler Import data handler.
     */
    public SAPServer(Properties settings, SAPServerImportDataHandler importDataHandler) {
        this(settings, importDataHandler, false);
    }

    /**
     * Initializes the server.
     * 
     * @param settingsProvider  SAP server settings provider.
     * @param importDataHandler Import data handler.
     * @param useJson           Use JSON format instead of XML format.
     */
    public SAPServer(Properties settings, SAPServerImportDataHandler importDataHandler, boolean useJson) {
        // Create handler lists
        this.errorHandlers = new ArrayList<SAPServerErrorHandler>();
        this.exceptionHandlers = new ArrayList<SAPServerExceptionHandler>();
        this.stateHandlers = new ArrayList<SAPServerStateHandler>();

        // Create data providers
        if (SAPServer.destinationDataProvider == null) {
            SAPServer.destinationDataProvider = new DestinationDataProviderImpl(settings);
            Environment.registerDestinationDataProvider(SAPServer.destinationDataProvider);
        }
        
        if (SAPServer.serverDataProvider == null) {
            SAPServer.serverDataProvider = new ServerDataProviderImpl(settings);
            Environment.registerServerDataProvider(SAPServer.serverDataProvider);
        }

        this.jCoServer = null;
        this.importDataHandler = importDataHandler;
        this.useJson = useJson;
    }

    public void start() throws Exception {
        if (importDataHandler == null) {
            throw new Exception("ImportDataHandler is null.");
        }

        if (isRunning()) {
            throw new Exception("Server is already running.");
        }

        this.jCoServer = JCoServerFactory.getServer(serverDataProvider.getServerProperties("").getProperty(ServerDataProvider.JCO_PROGID));

        // Add generic Function handler
        DefaultServerHandlerFactory.FunctionHandlerFactory factory = new DefaultServerHandlerFactory.FunctionHandlerFactory();
        factory.registerGenericHandler(new JCoServerFunctionHandlerImpl(importDataHandler, useJson));
        jCoServer.setCallHandlerFactory(factory);

        // Add TID handler
        JCoServerTIDHandler tidHandler = new JCoServerTIDHandlerImpl();
        jCoServer.setTIDHandler(tidHandler);

        // Add event listeners
        jCoServer.addServerErrorListener(this);
        jCoServer.addServerExceptionListener(this);
        jCoServer.addServerStateChangedListener(this);

        // Start the server
        jCoServer.start();
    }

    public boolean registerErrorHandler(SAPServerErrorHandler errorHandler) {
        return errorHandlers.add(errorHandler);
    }

    public boolean unregisterErrorHandler(SAPServerErrorHandler errorHandler) {
        return errorHandlers.remove(errorHandler);
    }

    public boolean registerExceptionHandler(SAPServerExceptionHandler exceptionHandler) {
        return exceptionHandlers.add(exceptionHandler);
    }

    public boolean unregisterExceptionHandler(SAPServerExceptionHandler exceptionHandler) {
        return exceptionHandlers.remove(exceptionHandler);
    }

    public boolean registerStateHandler(SAPServerStateHandler stateHandler) {
        return stateHandlers.add(stateHandler);
    }

    public boolean unregisterStateHandler(SAPServerStateHandler stateHandler) {
        return stateHandlers.remove(stateHandler);
    }

    public boolean isRunning() {
        if (jCoServer == null)
            return false;
        return jCoServer.getState().equals(JCoServerState.ALIVE);
    }

    public void stop() throws Exception {
        if (jCoServer != null) {
            jCoServer.stop();
        }

        while (!jCoServer.getState().equals(JCoServerState.STOPPED)) {
            Thread.sleep(1000);
        }

        Environment.unregisterServerDataProvider(SAPServer.serverDataProvider);
        Environment.unregisterDestinationDataProvider(SAPServer.destinationDataProvider);

        SAPServer.serverDataProvider = null;
        SAPServer.destinationDataProvider = null;
    }

    @Override
    public void serverStateChangeOccurred(JCoServer jcoServer, JCoServerState oldState, JCoServerState newState) {
        for (SAPServerStateHandler handler : stateHandlers) {
            handler.onStateChanged(oldState, newState);
        }
    }

    @Override
    public void serverExceptionOccurred(
            JCoServer jcoServer,
            String connectionId,
            JCoServerContextInfo ctxInfo,
            Exception exception) {

        for (SAPServerExceptionHandler handler : exceptionHandlers) {
            handler.onExceptionOccured(exception);
        }

    }

    @Override
    public void serverErrorOccurred(
            JCoServer jcoServer,
            String connectionId,
            JCoServerContextInfo ctxInfo,
            Error error) {

        for (SAPServerErrorHandler handler : errorHandlers) {
            handler.onErrorOccured(error);
        }
    }
}
