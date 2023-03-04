package ASPB.sap.handlers;

import com.sap.conn.jco.AbapClassException;
import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.server.JCoServerFunctionHandler;

public class FunctionHandler implements JCoServerFunctionHandler {

    private final String FUNCTION_NAME;

    public FunctionHandler(String functionName) {
        this.FUNCTION_NAME = functionName;
    }

    @Override
    public void handleRequest(JCoServerContext serverCtx, JCoFunction function)
            throws AbapException, AbapClassException {

        /*
         * if (!function.getName().equals(FUNCTION_NAME)) {
         * Logger("Function '" + function.getName() +
         * "' is no supported to be handled!");
         * return;
         * }
         */

        printRequestInformation(serverCtx, function);
        function.getImportParameterList().toXML();
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
    }

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

    /**
     * Returns the name of the function that is handled by this class.
     * 
     * @return the name of the function that is handled by this class.
     */
    public String getFunctionName() {
        return FUNCTION_NAME;
    }

}
