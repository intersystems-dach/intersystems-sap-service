package ASPB.sap.handlers;

import com.sap.conn.jco.AbapClassException;
import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.server.JCoServerFunctionHandler;

import ASPB.utils.Callback;
import ASPB.utils.XMLParser;

/**
 * Generic function handler that can be used to handle any function.
 * 
 * @author Philipp Bonin
 * @version 1.0
 * 
 */
public class GenericFunctionHandler implements JCoServerFunctionHandler {

    public boolean toJSON;

    private Callback<String> callback;

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

        if (toJSON) {
            try {
                callback.call(function.getImportParameterList().toJSON());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            try {
                callback.call(XMLParser.parse(function.getImportParameterList().toXML(), function.getName()));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
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
        System.out.println("----------------------------------------------------------------");
        System.out.println("call              : " + function.getName());
        System.out.println("ConnectionId      : " + serverCtx.getConnectionID());
        System.out.println("SessionId         : " + serverCtx.getSessionID());
        System.out.println("TID               : " + serverCtx.getTID());
        System.out.println("repository name   : " + serverCtx.getRepository().getName());
        System.out.println("is in transaction : " + serverCtx.isInTransaction());
        System.out.println("is stateful       : " + serverCtx.isStatefulSession());
        System.out.println("----------------------------------------------------------------");
        System.out.println("gwhost: " + serverCtx.getServer().getGatewayHost());
        System.out.println("gwserv: " + serverCtx.getServer().getGatewayService());
        System.out.println("progid: " + serverCtx.getServer().getProgramID());
        System.out.println("----------------------------------------------------------------");
        System.out.println("attributes  : ");
        System.out.println(serverCtx.getConnectionAttributes().toString());
        System.out.println("----------------------------------------------------------------");
        System.out.println("Import parameter: " + function.getImportParameterList().toString());
        System.out.println("JSON Import parameter: " + function.getImportParameterList().toJSON());
        System.out.println("----------------------------------------------------------------");
    }

}
