package ASPB.sap;

import java.lang.reflect.Field;
import java.util.Properties;

import com.sap.conn.jco.JCoException;
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

import ASPB.sap.dataprovider.MyDestinationDataProvider;
import ASPB.sap.dataprovider.MyServerDataProvider;
import ASPB.sap.handlers.GenericFunctionHandler;
import ASPB.sap.handlers.TIDHandler;
import ASPB.utils.Logger;
import ASPB.utils.ServiceManager;
import ASPB.utils.annotations.JCOPropertyAnnotation;
import ASPB.utils.annotations.MyFieldMetadata;
import ASPB.utils.interfaces.Callback;
import ASPB.utils.interfaces.MyServer;

/**
 * A Server to receive messages from a SAP system using the
 * {@link com.sap.conn.jco.server.JCoServer}.
 * 
 * @author Philipp Bonin
 * @version 1.0
 * 
 */
public class SAPServer implements MyServer,
        JCoServerErrorListener,
        JCoServerExceptionListener,
        JCoServerStateChangedListener {

    // The callback to the service
    private Callback<String> callback;

    // The server
    private JCoServer server;

    // The properties
    private Properties properties;

    // If the data should be converted to JSON
    private boolean toJSON;

    /**
     * Initializes the server.
     * 
     * @param toJSON If the data should be converted to JSON.
     */
    public SAPServer(boolean toJSON) {
        this.callback = null;
        this.server = null;
        this.properties = new Properties();
        this.toJSON = toJSON;
    }

    /**
     * Initializes the server.
     * 
     * @param toJSON   If the data should be converted to JSON.
     * @param callback The callback to the service.
     */
    public SAPServer(boolean toJSON, Callback<String> callback) {
        this.callback = callback;
        this.server = null;
        this.properties = new Properties();
        this.toJSON = toJSON;
    }

    /**
     * Initializes the server.
     * 
     * @param toJSON     If the data should be converted to JSON.
     * @param properties The properties to be used.
     * @param callback   The callback to the service.
     */
    public SAPServer(boolean toJSON, Properties properties, Callback<String> callback) {
        this.callback = callback;
        this.server = null;
        this.properties = properties;
        this.toJSON = toJSON;
    }

    /**
     * Initializes the server.
     * 
     * @param toJSON     If the data should be converted to JSON.
     * @param properties The properties to be used.
     */
    public SAPServer(boolean toJSON, Properties properties) {
        this.callback = null;
        this.server = null;
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

        // Add generic Function handler
        DefaultServerHandlerFactory.FunctionHandlerFactory factory = new DefaultServerHandlerFactory.FunctionHandlerFactory();
        factory.registerGenericHandler(new GenericFunctionHandler(callback, toJSON));
        server.setCallHandlerFactory(factory);

        // Add TID handler
        JCoServerTIDHandler tidHandler = new TIDHandler();
        server.setTIDHandler(tidHandler);

        // Add listener for errors.
        server.addServerErrorListener(this);

        // Add listener for exceptions.
        server.addServerExceptionListener(this);

        // Add server state change listener.
        server.addServerStateChangedListener(this);

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

        // register destination provider
        new MyDestinationDataProvider(properties);
        // register server provider
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

    /**
     * Checks if all properties are set
     * 
     * @return true if all properties are set
     */
    /*
     * private boolean checkProperties() {
     * 
     * String[] keyNames = {
     * ServerDataProvider.JCO_PROGID,
     * ServerDataProvider.JCO_GWHOST,
     * ServerDataProvider.JCO_GWSERV,
     * ServerDataProvider.JCO_CONNECTION_COUNT,
     * DestinationDataProvider.JCO_ASHOST,
     * DestinationDataProvider.JCO_CLIENT,
     * DestinationDataProvider.JCO_SYSNR,
     * DestinationDataProvider.JCO_USER,
     * DestinationDataProvider.JCO_PASSWD,
     * DestinationDataProvider.JCO_LANG
     * };
     * 
     * boolean allSet = true;
     * for (String key : keyNames) {
     * if (properties.getProperty(key) == null
     * || properties.getProperty(key).isEmpty()) {
     * Logger.error("Could not start server: " + key + "not set");
     * allSet = false;
     * }
     * }
     * 
     * return allSet;
     * }
     */

    @Override
    public boolean checkIfAllPropertiesAreSet() {

        if (ServiceManager.getInstance() == null)
            return false;

        boolean allSet = true;

        for (Field f : ServiceManager.getInstance().getAllSettings()) {
            if (f.isAnnotationPresent(MyFieldMetadata.class) && f.isAnnotationPresent(JCOPropertyAnnotation.class)) {
                if (f.getAnnotation(MyFieldMetadata.class).IsRequired()) {
                    String name = f.getAnnotation(JCOPropertyAnnotation.class).jcoName();
                    if (properties.getProperty(name) == null || properties.getProperty(name).isEmpty()) {
                        String msg = "Property not set: " + f.getName();
                        Logger.error(msg);
                        ServiceManager.logError(msg);
                        allSet = false;
                    }
                }
            }
        }

        return allSet;
    }

    @Override
    public void serverStateChangeOccurred(JCoServer jcoServer, JCoServerState oldState, JCoServerState newState) {
        // TODO State change handling

        Logger.log("Server " + jcoServer.getProgramID() + " state changed from " + oldState.toString() + " to "
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

    }

    @Override
    public void serverExceptionOccurred(
            JCoServer jcoServer,
            String connectionId,
            JCoServerContextInfo ctxInfo,
            Exception exception) {

        // TODO Exception handling

        Logger.error("An exception occured on server " + jcoServer.getProgramID() + ": " + exception.getMessage());

    }

    @Override
    public void serverErrorOccurred(
            JCoServer jcoServer,
            String connectionId,
            JCoServerContextInfo ctxInfo,
            Error error) {

        // TODO Error handling

        Logger.error("An error occured on server " + jcoServer.getProgramID() + ": " + error.getMessage());

    }
}
