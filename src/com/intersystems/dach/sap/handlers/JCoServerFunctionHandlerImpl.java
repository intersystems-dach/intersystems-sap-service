package com.intersystems.dach.sap.handlers;

import javax.naming.TimeLimitExceededException;

import com.intersystems.dach.sap.SAPImportData;
import com.intersystems.dach.sap.utils.XMLUtils;
import com.intersystems.dach.sap.utils.XSDUtils;
import com.intersystems.dach.utils.ObjectProvider;
import com.intersystems.dach.utils.TraceManager;
import com.sap.conn.jco.AbapClassException;
import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.server.JCoServerFunctionHandler;

/**
 * Generic function handler that can be used to handle any function.
 * 
 * @author Philipp Bonin, Andreas Schütz
 * @version 1.0
 * 
 */
public class JCoServerFunctionHandlerImpl implements JCoServerFunctionHandler {

    // The callback function to call
    private final SAPServerImportDataHandler importDataHandler;
    private ObjectProvider objectProvider;

    // utils instances
    private XMLUtils xmlUtils;
    private XSDUtils xsdUtils;

    /**
     * Create a new generic function handler that will call the callback function
     * with the XML representation of the import parameter list.
     * 
     * @param importDataHandler     the callback function to call
     * @param useJson               if true the JSON representation will be used,
     * @param confirmationTimeoutMs the timeout for the confirmation
     * @param traceManagerHandle    the trace manager handle
     * @param flattenTablesItems    if true, the tables and items will be flattened
     */
    public JCoServerFunctionHandlerImpl(SAPServerImportDataHandler importDataHandler, ObjectProvider objectProvider) {
        this.importDataHandler = importDataHandler;
        this.objectProvider = objectProvider;
        xmlUtils = new XMLUtils(objectProvider);
        xsdUtils = new XSDUtils(objectProvider);
    }

    @Override
    public void handleRequest(JCoServerContext serverCtx, JCoFunction function)
            throws AbapException, AbapClassException {
        String functionName = function.getName();
        String data = null;
        String schema = null;

        trace("Handle request by function '" + functionName + "'.");

        if (objectProvider.isUseJson()) {
            data = function.getImportParameterList().toJSON();
        } else {
            try {
                String importParameterXML = function.getImportParameterList().toXML();
                trace("ImportParameter: " + importParameterXML);
                trace("Convertig to XML...");
                data = xmlUtils.convert(importParameterXML, functionName);
                trace("XML data: " + data);
            } catch (Exception e) {
                TraceManager.getTraceManager(objectProvider.getWarningTraceManagerHandle())
                        .traceMessage("Could not convert import parameters to XML: " + e.getMessage());
                throw new AbapClassException(e);
            }
            try {
                trace("Generating XSD schema...");
                schema = xsdUtils.createXSDString(function, true, false);
                trace("XSD data: " + schema);
            } catch (Exception e) {
                TraceManager.getTraceManager(objectProvider.getWarningTraceManagerHandle()).traceMessage(
                        "Could not create XSD schema for function '" + functionName + "': " + e.getMessage());
            }
        }
        try {
            // call the import data handler function
            SAPImportData importData = new SAPImportData(functionName, data, objectProvider.isUseJson(), schema);

            trace("Calling import data receiver handler.");
            importDataHandler.onImportDataReceived(importData);

            trace("Waiting for confirmation for message with ID " + importData.getID() + ".");

            importData.waitForConfirmation(objectProvider.getConfirmationTimeoutMs());

            trace("Confirmation received for message with ID " + importData.getID() + ".");
        } catch (TimeLimitExceededException e) {
            throw new AbapException("SYSTEM_FAILURE", "Confirmation Timeout. InputData wasn't handled in time.");
        } catch (Exception e) {
            throw new AbapException("SYSTEM_FAILURE", "Could not process import parameters: " + e.getMessage());
        }
    }

    /**
     * Trace a message.
     * 
     * @param msg The message to trace
     */
    private void trace(String msg) {
        TraceManager.getTraceManager(objectProvider.getTraceManagerHandle()).traceMessage(msg);
    }
}
