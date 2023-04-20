package com.intersystems.dach.sap.handlers;

import javax.naming.TimeLimitExceededException;

import com.intersystems.dach.sap.SAPImportData;
import com.intersystems.dach.sap.utils.XMLUtils;
import com.intersystems.dach.sap.utils.XSDUtils;
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

    // If true the JSON representation will be used, otherwise the XML
    private final boolean useJson;

    // The callback function to call
    private final SAPServerImportDataHandler importDataHandler;
    private long confirmationTimeoutMs;
    private Object traceManagerHandle;

    /**
     * Create a new generic function handler that will call the callback function
     * with the XML representation of the import parameter list.
     * 
     * @param callback The callback function to call
     */
    public JCoServerFunctionHandlerImpl(SAPServerImportDataHandler importDataHandler, boolean useJson,
            long confirmationTimeoutMs, Object traceManagerHandle) {
        this.confirmationTimeoutMs = confirmationTimeoutMs;
        this.importDataHandler = importDataHandler;
        this.useJson = useJson;
        this.traceManagerHandle = traceManagerHandle;
    }

    @Override
    public void handleRequest(JCoServerContext serverCtx, JCoFunction function)
            throws AbapException, AbapClassException {
        String functionName = function.getName();
        String data = null;
        String schema = null;

        TraceManager.getTraceManager(traceManagerHandle).traceMessage("Handle request by function '" + functionName + "'.");

        if (useJson) {
            data = function.getImportParameterList().toJSON();
        } else {
            try {
                String importParameterXML = function.getImportParameterList().toXML();
                TraceManager.getTraceManager(traceManagerHandle).traceMessage("ImportParameter: " + importParameterXML);
                TraceManager.getTraceManager(traceManagerHandle).traceMessage("Convertig to XML...");
                data = XMLUtils.convert(importParameterXML, functionName);
                TraceManager.getTraceManager(traceManagerHandle).traceMessage("XML data: " + data);
            } catch (Exception e) {
                TraceManager.getTraceManager(traceManagerHandle).traceMessage("Could not convert import parameters to XML: " + e.getMessage());
                throw new AbapClassException(e);
            }
            try {
                TraceManager.getTraceManager(traceManagerHandle).traceMessage("Generating XSD schema...");
                schema = XSDUtils.createXSDString(function, true, false);
                TraceManager.getTraceManager(traceManagerHandle).traceMessage("XSD data: " + schema);
            } catch (Exception e) {
                TraceManager.getTraceManager(traceManagerHandle).traceMessage(
                        "Could not create XSD schema for function '" + functionName + "': " + e.getMessage());
            }
        }
        try {
            // call the import data handler function
            SAPImportData importData = new SAPImportData(functionName, data, useJson, schema);

            TraceManager.getTraceManager(traceManagerHandle).traceMessage("Calling import data receiver handler.");
            importDataHandler.onImportDataReceived(importData);

            TraceManager.getTraceManager(traceManagerHandle).traceMessage("Waiting for confirmation for message with ID " + importData.getID() + ".");

            importData.waitForConfirmation(confirmationTimeoutMs);

            TraceManager.getTraceManager(traceManagerHandle).traceMessage("Confirmation received for message with ID " + importData.getID() + ".");
        } catch (TimeLimitExceededException e) {
            throw new AbapException("SYSTEM_FAILURE", "Confirmation Timeout. InputData wasn't handled in time.");
        } catch (Exception e) {
            throw new AbapException("SYSTEM_FAILURE", "Could not process import parameters: " + e.getMessage());
        }
    }
}
