package ASPB.sap.old;

import java.util.ArrayList;
import java.util.List;
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
import ASPB.sap.handlers.FunctionHandler;
import ASPB.sap.handlers.GenericFunctionHandler;
import ASPB.sap.handlers.TIDHandler;
import ASPB.utils.Callback;

/**
 * Manager for the connection to SAP
 * 
 * @author Philipp Bonin
 * @version 1.0
 * 
 */
public abstract class SAPConnectionManager {

    private static JCoServer server = null;

    private static List<FunctionHandler> functionHandlers = new ArrayList<FunctionHandler>();

    private static Properties properties = new Properties();

    /**
     * Initialize the connection to SAP
     */
    public static void start() {

        if (isServerAlive())
            stop();

        try {
            initialize();
        } catch (Exception e) {
            // could not initialize server
            return;
        }

        // init server
        try {
            server = JCoServerFactory.getServer(properties.getProperty(ServerDataProvider.JCO_PROGID));
        } catch (JCoException e) {
            // could not create server
            return;
        }

        // Add Function handler
        DefaultServerHandlerFactory.FunctionHandlerFactory factory = new DefaultServerHandlerFactory.FunctionHandlerFactory();

        /*
         * for (FunctionHandler functionHandler : functionHandlers)
         * factory.registerHandler(functionHandler.getFunctionName(), functionHandler);
         */

        factory.registerGenericHandler(new GenericFunctionHandler(new Callback<String>() {

            @Override
            public boolean call(String data) {
                // TODO Auto-generated method stub
                return false;

            }
        }));

        server.setCallHandlerFactory(factory);

        // Add TID handler
        JCoServerTIDHandler tidHandler = new TIDHandler();
        server.setTIDHandler(tidHandler);

        // Add listener for errors.
        server.addServerErrorListener((
                JCoServer jcoServer, String connectionId,
                JCoServerContextInfo arg2, Error error) -> {
            // Error handling
        });

        // Add listener for exceptions.
        server.addServerExceptionListener((
                JCoServer jcoServer, String connectionId,
                JCoServerContextInfo arg2, Exception exception) -> {
            // Exception handling
        });

        // Add server state change listener.
        server.addServerStateChangedListener((
                JCoServer server, JCoServerState oldState,
                JCoServerState newState) -> {
            // Defined states are: STARTED, DEAD, ALIVE, STOPPED;
            // see JCoServerState class for details.
            // Details for connections managed by a server instance
            // are available via JCoServerMonitor
            if (newState.equals(JCoServerState.ALIVE)) {
                // server is running
            }
            if (newState.equals(JCoServerState.STOPPED)) {
                // server stopped
                System.exit(0);
            }
        });

        // Add a stdIn listener.
        // new Thread(stdInListener).start();
        // Start the server
        server.start();

    }

    /**
     * Get the program ID of the server
     * 
     * @return The program ID of the server
     */
    public static String getProgID() {
        return properties.getProperty(ServerDataProvider.JCO_PROGID);
    }

    /**
     * Stop the server
     */
    public static void stop() {
        if (server != null)
            server.stop();
    }

    /**
     * Get the current state of the server
     * 
     * @return The current state of the server
     */
    public static String getServerState() {
        if (server == null)
            return "Server not started";
        return server.getState().toString();
    }

    /**
     * Check if the server is alive
     * 
     * @return True if the server is alive, false otherwise
     */
    public static boolean isServerAlive() {
        if (server == null)
            return false;
        return server.getState().equals(JCoServerState.ALIVE);
    }

    /**
     * Add a function handler to the server
     * 
     * @param functionName The name of the function to handle
     */
    public static void addFunctionHandler(String functionName) {
        functionHandlers.add(new FunctionHandler(functionName));
    }

    /**
     * Initialize the server
     */
    private static void initialize() {

        // Set runtime arguments since some of the JCo-properties need to be passed the
        // the VM
        // and simply passing them to JCo won't have any effects.
        System.setProperty("jco.trace_path", properties.getProperty("jco.trace_path"));

        System.setProperty("jco.trace_level", properties.getProperty("jco.trace_level").toString());

        System.setProperty("jrfc.trace", properties.getProperty("jrfc.trace").toString());

        new MyDestinationDataProvider(properties);
        new MyServerDataProvider(properties);
    }

    /**
     * Set the properties for the connection to SAP
     * 
     * @param properties The properties for the connection to SAP
     */
    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

}
