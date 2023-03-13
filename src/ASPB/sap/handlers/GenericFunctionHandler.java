package ASPB.sap.handlers;

import com.sap.conn.jco.AbapClassException;
import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.server.JCoServerFunctionHandler;

import ASPB.utils.Logger;
import ASPB.utils.XMLConverter;
import ASPB.utils.interfaces.Callback;

/**
 * Generic function handler that can be used to handle any function.
 * 
 * @author Philipp Bonin
 * @version 1.0
 * 
 */
public class GenericFunctionHandler implements JCoServerFunctionHandler {

    // If true the JSON representation will be used, otherwise the XML
    private final boolean toJSON;

    // The callback function to call
    private final Callback<String> callback;

    /**
     * Create a new generic function handler that will call the callback function
     * with the XML representation of the import parameter list.
     * 
     * @param callback The callback function to call
     */
    public GenericFunctionHandler(Callback<String> callback) {
        this.callback = callback;
        this.toJSON = false;
    }

    /**
     * Create a new generic function handler that will call the callback function
     * with the XML or JSON representation of the import parameter list.
     * 
     * @param callback The callback function to call
     * @param toJSON   If true the JSON representation will be used, otherwise the
     *                 XML representation will be used.
     */
    public GenericFunctionHandler(Callback<String> callback, boolean toJSON) {
        this.callback = callback;
        this.toJSON = toJSON;
    }

    @Override
    public void handleRequest(JCoServerContext serverCtx, JCoFunction function)
            throws AbapException, AbapClassException {

        printRequestInformation(serverCtx, function);

        String result = "";

        if (toJSON) {
            // return json
            result = function.getImportParameterList().toJSON();

        } else {
            // return xml
            try {
                result = XMLConverter.convert(function.getImportParameterList().toXML(), function.getName());
            } catch (Exception e) {
                // TODO handle exception when parsing the xml
            }
        }

        // call the callback function
        callback.call(result);

        /*
         * Iterator<JCoField> i = function.getImportParameterList().iterator();
         * while (i.hasNext()) {
         * JCoField f = i.next();
         * Logger.logInfo(f.getName() + " " + f.getTypeAsString() + " " + f.getValue());
         * }
         * JCoTable jcoTable = function.getImportParameterList().getTable("TABLE");
         * String tableName = "SAP.Test.Table1";// jcoTable.getString("TABLE_NAME");
         * makeRows(jcoTable, getTable(jcoTable, tableName));
         */
        /*
         * // Get the URL provided from Abap.
         * String url = function.getImportParameterList().getString("URL");
         * 
         * HttpCaller main = new HttpCaller();
         * main.initializeSslContext();
         * main.initializeClient();
         * String payload = null;
         * try {
         * payload = main.invokeGet(url);
         * } catch (IOException | InterruptedException e) {
         * // Provide the exception as payload.
         * payload = e.getMessage();
         * }
         * // Provide the payload as exporting parameter.
         * function.getExportParameterList().setValue("RESPONSE_PAYLOAD", payload);
         */

        // TODO
        // In sample 3 (tRFC Server) we also set the status to executed:
        /*
         * if (myTIDHandler != null)
         * myTIDHandler.execute(serverCtx);
         */
    }

    /**
     * Print the request information to the console.
     * 
     * @param serverCtx The server context
     * @param function  The function
     */
    private void printRequestInformation(JCoServerContext serverCtx, JCoFunction function) {
        Logger.log("----------------------------------------------------------------");
        Logger.log("call              : " + function.getName());
        Logger.log("ConnectionId      : " + serverCtx.getConnectionID());
        Logger.log("SessionId         : " + serverCtx.getSessionID());
        Logger.log("TID               : " + serverCtx.getTID());
        Logger.log("repository name   : " + serverCtx.getRepository().getName());
        Logger.log("is in transaction : " + serverCtx.isInTransaction());
        Logger.log("is stateful       : " + serverCtx.isStatefulSession());
        Logger.log("----------------------------------------------------------------");
        Logger.log("gwhost: " + serverCtx.getServer().getGatewayHost());
        Logger.log("gwserv: " + serverCtx.getServer().getGatewayService());
        Logger.log("progid: " + serverCtx.getServer().getProgramID());
        Logger.log("----------------------------------------------------------------");
        Logger.log("attributes  : ");
        Logger.log(serverCtx.getConnectionAttributes().toString());
        Logger.log("----------------------------------------------------------------");
        Logger.log("Import parameter: " + function.getImportParameterList().toString());
        Logger.log("JSON Import parameter: " + function.getImportParameterList().toJSON());
        Logger.log("----------------------------------------------------------------");
    }

}
