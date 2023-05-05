package com.intersystems.dach.sap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;

import com.intersystems.dach.sap.handlers.JCoServerFunctionHandlerImpl;
import com.intersystems.dach.sap.handlers.SAPServerImportDataHandler;
import com.intersystems.dach.sap.handlers.SAPServerErrorHandler;
import com.intersystems.dach.sap.handlers.SAPServerExceptionHandler;
import com.intersystems.dach.sap.handlers.SAPServerStateHandler;
import com.intersystems.dach.sap.utils.DestinationDataProviderImpl;
import com.intersystems.dach.sap.utils.ServerDataProviderImpl;
import com.intersystems.dach.sap.handlers.JCoServerTIDHandlerImpl;
import com.sap.conn.jco.ext.DestinationDataProvider;
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

    private String serverName;
    private String destinationName;
    private JCoServer jCoServer;

    // Event handlers
    private SAPServerImportDataHandler importDataHandler;
    private Collection<SAPServerErrorHandler> errorHandlers;
    private Collection<SAPServerExceptionHandler> exceptionHandlers;
    private Collection<SAPServerStateHandler> stateHandlers;

    // Tracing
    private SAPServerArgs objectProvider;

    /**
     * Initializes the server.
     * 
     * @param settingsProvider SAP server settings provider.
     * @param useJson          Use JSON format instead of XML format.
     * @param objectProvider   Trace Manager handle
     */
    public SAPServer(SAPServerArgs objectProvider) {
        // Create handler lists
        this.errorHandlers = new ArrayList<SAPServerErrorHandler>();
        this.exceptionHandlers = new ArrayList<SAPServerExceptionHandler>();
        this.stateHandlers = new ArrayList<SAPServerStateHandler>();

        this.jCoServer = null;

        this.objectProvider = objectProvider;
    }

    /**
     * Start SAP Server
     * 
     * @throws Exception if SAP server can't be started.
     */
    public void start() throws Exception {
        trace("Starting SAP server.");

        // Pre checks
        if (importDataHandler == null) {
            throw new Exception("ImportDataHandler is null.");
        }
        if (isRunning()) {
            throw new Exception("Server is already running.");
        }

        StringBuilder sb = new StringBuilder();
        for (Entry<Object, Object> e : objectProvider.getSapProperties().entrySet()) {
            if (e.getKey().toString().equals(DestinationDataProvider.JCO_PASSWD)) {
                continue;
            }
            sb.append(e);
        }
        trace("Settings: " + sb.toString());

        trace("Registering settings with data provider.");

        // Set server and destination name
        serverName = objectProvider.getSapProperties().getProperty(ServerDataProvider.JCO_PROGID);
        destinationName = objectProvider.getSapProperties().getProperty(ServerDataProvider.JCO_REP_DEST);

        try {
            DestinationDataProviderImpl.setProperties(destinationName, objectProvider.getSapProperties());
            ServerDataProviderImpl.setProperties(serverName, objectProvider.getSapProperties());
        } catch (IllegalStateException e) {
            throw new Exception(e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getClass() + e.toString());
        }

        trace("Settings registered.");

        // Create jCoServer object
        this.jCoServer = JCoServerFactory.getServer(serverName);

        // Add generic Function handler
        trace("Adding handlers and listeners.");
        DefaultServerHandlerFactory.FunctionHandlerFactory factory = new DefaultServerHandlerFactory.FunctionHandlerFactory();
        factory.registerGenericHandler(new JCoServerFunctionHandlerImpl(importDataHandler, objectProvider));
        jCoServer.setCallHandlerFactory(factory);

        // Add TID handler
        JCoServerTIDHandler tidHandler = new JCoServerTIDHandlerImpl();
        jCoServer.setTIDHandler(tidHandler);

        // Add event listeners
        jCoServer.addServerErrorListener(this);
        jCoServer.addServerExceptionListener(this);
        jCoServer.addServerStateChangedListener(this);

        trace("Handlers and listeners added.");

        // Start the server
        try {
            jCoServer.start();
        } catch (Exception e) {
            deleteDataProviders();
            throw e;
        }

        trace("Server started.");
    }

    /**
     * Register an import data handler (required).
     * 
     * @param importDataHandler the SAPServerImportDataHandler instance.
     */
    public void registerImportDataHandler(SAPServerImportDataHandler importDataHandler) {
        this.importDataHandler = importDataHandler;
    }

    /**
     * Register an error handler.
     * 
     * @param errorHandler
     * @return true, if registration was successful.
     */
    public boolean registerErrorHandler(SAPServerErrorHandler errorHandler) {
        return errorHandlers.add(errorHandler);
    }

    /**
     * Unregister an error handler.
     * 
     * @param errorHandler
     * @return true, if unregistration was successful.
     */
    public boolean unregisterErrorHandler(SAPServerErrorHandler errorHandler) {
        return errorHandlers.remove(errorHandler);
    }

    /**
     * Register an exception handler.
     * 
     * @param exceptionHandler
     * @return true, if registration was successful.
     */
    public boolean registerExceptionHandler(SAPServerExceptionHandler exceptionHandler) {
        return exceptionHandlers.add(exceptionHandler);
    }

    /**
     * Unregister an exception handler.
     * 
     * @param exceptionHandler
     * @return true, if unregistration was successful.
     */
    public boolean unregisterExceptionHandler(SAPServerExceptionHandler exceptionHandler) {
        return exceptionHandlers.remove(exceptionHandler);
    }

    /**
     * Register a state handler.
     * 
     * @param stateHandler
     * @return true, if registration was successful.
     */
    public boolean registerStateHandler(SAPServerStateHandler stateHandler) {
        return stateHandlers.add(stateHandler);
    }

    /**
     * Unregister a state handler.
     * 
     * @param stateHandler
     * @return true, if unregistration was successful.
     */
    public boolean unregisterStateHandler(SAPServerStateHandler stateHandler) {
        return stateHandlers.remove(stateHandler);
    }

    /**
     * Check if SAP server is running.
     * 
     * @return true, if server is running, false, if not.
     */
    public boolean isRunning() {
        if (jCoServer == null)
            return false;
        return jCoServer.getState().equals(JCoServerState.ALIVE);
    }

    /**
     * Stop the SAP server.
     * 
     * @throws Exception if server can't be stopped.
     */
    public void stop() throws Exception {
        trace("Stopping SAP server.");

        if (jCoServer != null) {
            try {
                jCoServer.stop();
                while (!jCoServer.getState().equals(JCoServerState.STOPPED)) {
                    Thread.sleep(500);
                }
                trace("SAP server stopped.");
            } catch (Exception e) {
                for (SAPServerExceptionHandler handler : exceptionHandlers) {
                    handler.onExceptionOccured(e);
                }
            } finally {
                jCoServer = null;
                deleteDataProviders();
                trace("Settings removed.");
            }

        }

    }

    /**
     * Delete the settings from the data providers.
     */
    private void deleteDataProviders() {
        trace("Removing settings from data provider.");
        DestinationDataProviderImpl.deleteProperties(destinationName);
        ServerDataProviderImpl.deleteProperties(serverName);
    }

    /**
     * Trace a message.
     * 
     * @param msg The message to trace
     */
    private void trace(String msg) {
        objectProvider.getTraceManager().traceMessage(msg);
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
