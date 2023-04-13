package com.intersystems.dach.sap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.Map.Entry;

import com.intersystems.dach.ens.sap.utils.TraceManager;
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

    private static DestinationDataProvider destinationDataProvider = null;
    private static ServerDataProvider serverDataProvider = null;

    private JCoServer jCoServer;

    private boolean useJson;

    private int confirmationTimeoutMs = 20000;

    private Properties settings;

    // Event handlers
    private SAPServerImportDataHandler importDataHandler;
    private Collection<SAPServerErrorHandler> errorHandlers;
    private Collection<SAPServerExceptionHandler> exceptionHandlers;
    private Collection<SAPServerStateHandler> stateHandlers;

    /**
     * Initializes the server in XML mode.
     * 
     * @param settings          SAP server settings.
     * @param importDataHandler Import data handler.
     */
    public SAPServer(Properties settings) {
        this(settings, false);
    }

    /**
     * Initializes the server.
     * 
     * @param settingsProvider  SAP server settings provider.
     * @param importDataHandler Import data handler.
     * @param useJson           Use JSON format instead of XML format.
     */
    public SAPServer(Properties settings, boolean useJson) {
        // Create handler lists
        this.errorHandlers = new ArrayList<SAPServerErrorHandler>();
        this.exceptionHandlers = new ArrayList<SAPServerExceptionHandler>();
        this.stateHandlers = new ArrayList<SAPServerStateHandler>();

        this.settings = settings;

        this.jCoServer = null;
        this.useJson = useJson;
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
        TraceManager.traceMessage("Starting SAP server.");

        // Pre checks
        if (importDataHandler == null) {
            throw new Exception("ImportDataHandler is null.");
        }
        if (isRunning()) {
            throw new Exception("Server is already running.");
        }
        if (SAPServer.destinationDataProvider != null ||
                SAPServer.serverDataProvider != null) {
            throw new Exception("Data provider already registered.");
        }

        // TODO is needed?
        StringBuilder sb = new StringBuilder();
        for (Entry<Object, Object> e : settings.entrySet()) {
            if (e.getKey().toString().equals(DestinationDataProvider.JCO_PASSWD)) {
                continue;
            }
            sb.append(e);
        }
        TraceManager.traceMessage("Settings: " + sb.toString());

        // Register data providers
        TraceManager.traceMessage("Registering data providers.");
        SAPServer.destinationDataProvider = new DestinationDataProviderImpl(settings);
        SAPServer.serverDataProvider = new ServerDataProviderImpl(settings);
        Environment.registerDestinationDataProvider(SAPServer.destinationDataProvider);
        Environment.registerServerDataProvider(SAPServer.serverDataProvider);

        TraceManager.traceMessage("Data providers registered.");

        // Create jCoServer object
        this.jCoServer = JCoServerFactory
                .getServer(serverDataProvider.getServerProperties("").getProperty(ServerDataProvider.JCO_PROGID));

        // Add generic Function handler
        TraceManager.traceMessage("Adding handlers and listeners.");
        DefaultServerHandlerFactory.FunctionHandlerFactory factory = new DefaultServerHandlerFactory.FunctionHandlerFactory();
        factory.registerGenericHandler(
                new JCoServerFunctionHandlerImpl(importDataHandler, useJson, confirmationTimeoutMs));
        jCoServer.setCallHandlerFactory(factory);

        // Add TID handler
        JCoServerTIDHandler tidHandler = new JCoServerTIDHandlerImpl();
        jCoServer.setTIDHandler(tidHandler);

        // Add event listeners
        jCoServer.addServerErrorListener(this);
        jCoServer.addServerExceptionListener(this);
        jCoServer.addServerStateChangedListener(this);

        TraceManager.traceMessage("Handlers and listeners added.");

        // Start the server
        jCoServer.start();

        TraceManager.traceMessage("Server started.");
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
        TraceManager.traceMessage("Stopping SAP server.");

        if (jCoServer != null) {
            jCoServer.stop();
        }

        while (!jCoServer.getState().equals(JCoServerState.STOPPED)) {
            Thread.sleep(1000);
        }
        TraceManager.traceMessage("SAP server stopped.");

        TraceManager.traceMessage("Unregistering data providers.");
        unregisterDataProviders();

        while (Environment.isServerDataProviderRegistered() ||
                Environment.isDestinationDataProviderRegistered()) {
            Thread.sleep(1000);
        }

        SAPServer.serverDataProvider = null;
        SAPServer.destinationDataProvider = null;

        TraceManager.traceMessage("Data providers unregistered.");
    }

    /**
     * Unregister data providers.
     */
    public void unregisterDataProviders() {
        if (SAPServer.serverDataProvider != null) {
            Environment.unregisterServerDataProvider(SAPServer.serverDataProvider);
        }
        if (SAPServer.destinationDataProvider != null) {
            Environment.unregisterDestinationDataProvider(SAPServer.destinationDataProvider);
        }

        destinationDataProvider = null;
        serverDataProvider = null;

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
