package com.intersystems.dach.sap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.Map.Entry;

import com.intersystems.dach.sap.handlers.JCoServerFunctionHandlerImpl;
import com.intersystems.dach.sap.handlers.SAPServerImportDataHandler;
import com.intersystems.dach.sap.handlers.SAPServerErrorHandler;
import com.intersystems.dach.sap.handlers.SAPServerExceptionHandler;
import com.intersystems.dach.sap.handlers.SAPServerStateHandler;
import com.intersystems.dach.sap.utils.DestinationDataProviderImpl;
import com.intersystems.dach.sap.utils.ServerDataProviderImpl;
import com.intersystems.dach.utils.TraceManager;
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
    private boolean useJson;
    private int confirmationTimeoutMs = 20000;
    private Properties settings;

    // Event handlers
    private SAPServerImportDataHandler importDataHandler;
    private Collection<SAPServerErrorHandler> errorHandlers;
    private Collection<SAPServerExceptionHandler> exceptionHandlers;
    private Collection<SAPServerStateHandler> stateHandlers;

    // Tracing
    private Object traceManagerHandle;

    /**
     * Initializes the server.
     * 
     * @param settingsProvider   SAP server settings provider.
     * @param useJson            Use JSON format instead of XML format.
     * @param traceManagerHandle Trace Manager handle
     */
    public SAPServer(Properties settings, boolean useJson, Object traceManagerHandle) {
        // Create handler lists
        this.errorHandlers = new ArrayList<SAPServerErrorHandler>();
        this.exceptionHandlers = new ArrayList<SAPServerExceptionHandler>();
        this.stateHandlers = new ArrayList<SAPServerStateHandler>();

        this.settings = settings;

        this.jCoServer = null;
        this.useJson = useJson;

        this.traceManagerHandle = traceManagerHandle;
    }

    /**
     * Set the confirmation timeout. This is the time the function handler waits
     * till the processing of the input data has been confirmed.
     * 
     * @param confirmationTimeoutMs Must be at least 200 ms.
     */
    public boolean setConfirmationTimeoutMs(int confirmationTimeoutMs) {
        if (confirmationTimeoutMs >= 200) {
            this.confirmationTimeoutMs = confirmationTimeoutMs;
            return true;
        }

        return false;
    }

    /**
     * Start SAP Server
     * 
     * @throws Exception if SAP server can't be started.
     */
    public void start() throws Exception {
        TraceManager.getTraceManager(traceManagerHandle).traceMessage("Starting SAP server.");

        // Pre checks
        if (importDataHandler == null) {
            throw new Exception("ImportDataHandler is null.");
        }
        if (isRunning()) {
            throw new Exception("Server is already running.");
        }

        StringBuilder sb = new StringBuilder();
        for (Entry<Object, Object> e : settings.entrySet()) {
            if (e.getKey().toString().equals(DestinationDataProvider.JCO_PASSWD)) {
                continue;
            }
            sb.append(e);
        }
        TraceManager.getTraceManager(traceManagerHandle).traceMessage("Settings: " + sb.toString());

        TraceManager.getTraceManager(traceManagerHandle).traceMessage("Registering settings with data provider.");

        // Set server and destination name
        serverName = settings.getProperty(ServerDataProvider.JCO_PROGID);
        destinationName = settings.getProperty(ServerDataProvider.JCO_REP_DEST);

        try {
            DestinationDataProviderImpl.setProperties(destinationName, settings);
            ServerDataProviderImpl.setProperties(serverName, settings);
        } catch (IllegalStateException e) {
            throw new Exception("Yolo" + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Yolo2" + e.getClass() + e.toString());
        }

        TraceManager.getTraceManager(traceManagerHandle).traceMessage("Settings registered.");

        // Create jCoServer object
        this.jCoServer = JCoServerFactory.getServer(serverName);

        // Add generic Function handler
        TraceManager.getTraceManager(traceManagerHandle).traceMessage("Adding handlers and listeners.");
        DefaultServerHandlerFactory.FunctionHandlerFactory factory = new DefaultServerHandlerFactory.FunctionHandlerFactory();
        factory.registerGenericHandler(new JCoServerFunctionHandlerImpl(
                importDataHandler, useJson, confirmationTimeoutMs, traceManagerHandle));
        jCoServer.setCallHandlerFactory(factory);

        // Add TID handler
        JCoServerTIDHandler tidHandler = new JCoServerTIDHandlerImpl();
        jCoServer.setTIDHandler(tidHandler);

        // Add event listeners
        jCoServer.addServerErrorListener(this);
        jCoServer.addServerExceptionListener(this);
        jCoServer.addServerStateChangedListener(this);

        TraceManager.getTraceManager(traceManagerHandle).traceMessage("Handlers and listeners added.");

        // Start the server
        try {
            jCoServer.start();
        } catch (Exception e) {
            deleteDataProviders();
            throw e;
        }

        TraceManager.getTraceManager(traceManagerHandle).traceMessage("Server started.");
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
        TraceManager.getTraceManager(traceManagerHandle).traceMessage("Stopping SAP server.");

        if (jCoServer != null) {
            jCoServer.stop();

            while (!jCoServer.getState().equals(JCoServerState.STOPPED)) {
                Thread.sleep(500);
            }
        }

        TraceManager.getTraceManager(traceManagerHandle).traceMessage("SAP server stopped.");

        deleteDataProviders();

        TraceManager.getTraceManager(traceManagerHandle).traceMessage("Settings removed.");
    }

    public void deleteDataProviders() {
        TraceManager.getTraceManager(traceManagerHandle).traceMessage("Removing settings from data provider.");
        DestinationDataProviderImpl.deleteProperties(destinationName);
        ServerDataProviderImpl.deleteProperties(serverName);
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
