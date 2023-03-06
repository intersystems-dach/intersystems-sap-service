package ASPB.sap;

import java.util.Properties;

import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.ext.ServerDataProvider;
import com.sap.conn.jco.server.DefaultServerHandlerFactory;
import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerContextInfo;
import com.sap.conn.jco.server.JCoServerFactory;
import com.sap.conn.jco.server.JCoServerState;
import com.sap.conn.jco.server.JCoServerTIDHandler;

import ASPB.sap.dataprovider.MyDestinationDataProvider;
import ASPB.sap.dataprovider.MyServerDataProvider;
import ASPB.sap.handlers.GenericFunctionHandler;
import ASPB.sap.handlers.TIDHandler;
import ASPB.utils.Callback;
import ASPB.utils.Logger;
import ASPB.utils.Server;

/**
 * A Server to receive messages from a SAP system.
 * 
 * @author Philipp Bonin
 * @version 1.0
 * 
 */
public class SAPServer implements Server {

    private Callback<String> callback;

    private JCoServer server;

    private Properties properties;

    private boolean toJSON;

    public SAPServer(boolean toJSON) {
        callback = null;
        server = null;
        properties = new Properties();
        this.toJSON = toJSON;
    }

    public SAPServer(boolean toJSON, Callback<String> callback) {
        this.callback = callback;
        server = null;
        properties = new Properties();
        this.toJSON = toJSON;
    }

    public SAPServer(boolean toJSON, Properties properties, Callback<String> callback) {
        this.callback = callback;
        server = null;
        this.properties = properties;
        this.toJSON = toJSON;
    }

    public SAPServer(boolean toJSON, Properties properties) {
        this.callback = null;
        server = null;
        this.properties = properties;
        this.toJSON = toJSON;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void registerCallback(Callback<?> callback) {
        // TODO check if the callback is of the correct type

        this.callback = (Callback<String>) callback;
    }

    @Override
    public boolean start() {
        // Dont start if no callback is registered
        if (callback == null) {
            Logger.error("Could not start server: No callback registered");
            return false;
        }

        // chek if all required properties are set
        if (!checkProperties()) {
            Logger.error("Could not start server: Not all properties are set");
            return false;
        }

        // Stop the server if it is already running
        if (isRunning())
            stop();

        // initialize the server
        try {
            initialize();
        } catch (Exception e) {
            // could not initialize server
            Logger.error("Could not initialize the server: " + e.getMessage());
            return false;
        }

        // init server
        try {
            server = JCoServerFactory.getServer(properties.getProperty(ServerDataProvider.JCO_PROGID));
        } catch (JCoException e) {
            // could not create server
            Logger.error("Could not initialize the server: " + e.getMessage());
            return false;
        }

        // Add Function handler
        DefaultServerHandlerFactory.FunctionHandlerFactory factory = new DefaultServerHandlerFactory.FunctionHandlerFactory();

        factory.registerGenericHandler(new GenericFunctionHandler(callback, toJSON));

        server.setCallHandlerFactory(factory);

        // Add TID handler
        JCoServerTIDHandler tidHandler = new TIDHandler();
        server.setTIDHandler(tidHandler);

        // Add listener for errors.
        server.addServerErrorListener(
                (JCoServer jcoServer, String connectionId, JCoServerContextInfo arg2, Error error) -> {
                    // TODO Error handling
                    Logger.error("An error occured on server " + server.getProgramID() + ": " + error.getMessage());
                });

        // Add listener for exceptions.
        server.addServerExceptionListener(
                (JCoServer jcoServer, String connectionId, JCoServerContextInfo arg2, Exception exception) -> {
                    // TODO Exception handling
                    Logger.error(
                            "An exception occured on server " + server.getProgramID() + ": " + exception.getMessage());

                });

        // Add server state change listener.
        server.addServerStateChangedListener((JCoServer server, JCoServerState oldState, JCoServerState newState) -> {
            // TODO State change handling
            Logger.log("Server " + server.getProgramID() + " state changed from " + oldState.toString() + " to "
                    + newState.toString());
            // Defined states are: STARTED, DEAD, ALIVE, STOPPED;
            // see JCoServerState class for details.
            // Details for connections managed by a server instance
            // are available via JCoServerMonitor
            if (newState.equals(JCoServerState.ALIVE)) {
                // server is running
            }
            if (newState.equals(JCoServerState.STOPPED)) {
                // server stopped
            }
        });

        // Start the server
        try {
            server.start();
        } catch (Exception e) {
            // could not start server
            Logger.error("Could not start the server: " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean stop() {
        if (server != null) {
            server.stop();
        }
        return true;
    }

    @Override
    public boolean isRunning() {
        if (server == null)
            return false;
        return server.getState().equals(JCoServerState.ALIVE);
    }

    @Override
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * Initialize the server
     */
    private void initialize() {

        // Set runtime arguments since some of the JCo-properties need to be passed the
        // the VM
        // and simply passing them to JCo won't have any effects.
        // TODO check if this is still needed
        /*
         * System.setProperty("jco.trace_path",
         * properties.getProperty("jco.trace_path"));
         * 
         * System.setProperty("jco.trace_level",
         * properties.getProperty("jco.trace_level").toString());
         * 
         * System.setProperty("jrfc.trace",
         * properties.getProperty("jrfc.trace").toString());
         */

        new MyDestinationDataProvider(properties);
        new MyServerDataProvider(properties);
    }

    /**
     * Set if the response should be converted to JSON or not. Default is false.
     * 
     * @param b true if the response should be converted to JSON
     */
    public void setToJSON(boolean b) {
        this.toJSON = b;
    }

    private boolean checkProperties() {
        // TODO check if all properties are set
        return true;
    }
}
