package com.intersystems.dach.sap.handlers;

import javax.naming.TimeLimitExceededException;

import com.intersystems.dach.ens.sap.utils.TraceManager;
import com.intersystems.dach.sap.SAPImportData;
import com.intersystems.dach.sap.utils.XMLUtils;
import com.intersystems.dach.sap.utils.XSDUtils;
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

    /**
     * Create a new generic function handler that will call the callback function
     * with the XML representation of the import parameter list.
     * 
     * @param callback The callback function to call
     */
    public JCoServerFunctionHandlerImpl(SAPServerImportDataHandler importDataHandler, boolean useJson,
            long confirmationTimeoutMs) {
        this.confirmationTimeoutMs = confirmationTimeoutMs;
        this.importDataHandler = importDataHandler;
        this.useJson = useJson;
    }

    @Override
    public void handleRequest(JCoServerContext serverCtx, JCoFunction function)
            throws AbapException, AbapClassException {
        String functionName = function.getName();
        String data = null;
        String schema = null;

        TraceManager.traceMessage("Handle request by function '" + functionName + "'.");

        if (useJson) {
            data = function.getImportParameterList().toJSON();
        } else {
            try {
                data = XMLUtils.convert(function.getImportParameterList().toXML(), functionName);
            } catch (Exception e) {
                TraceManager.traceMessage("Could not convert import parameters to XML: " + e.getMessage());
                throw new AbapClassException(e);
            }
            try {
                schema = XSDUtils.createXSDString(function, true, false);
            } catch (Exception e) {
                TraceManager.traceMessage(
                        "Could not create XSD schema for function '" + functionName + "': " + e.getMessage());
                throw new AbapClassException(e);
            }
        }
        try {
            // call the import data handler function
            SAPImportData importData = new SAPImportData(functionName, data, useJson, schema);

            TraceManager.traceMessage("Calling import data receiver handler.");
            importDataHandler.onImportDataReceived(importData);

            TraceManager.traceMessage("Waiting for confirmation for message with ID " + importData.getID() + ".");

            importData.waitForConfirmation(confirmationTimeoutMs);

            TraceManager.traceMessage("Confirmation received for message with ID " + importData.getID() + ".");
        } catch (TimeLimitExceededException e) {
            throw new AbapException("SYSTEM_FAILURE", "Confirmation Timeout. InputData wasn't handled in time.");
        } catch (Exception e) {
            throw new AbapException("SYSTEM_FAILURE", "Could not process import parameters: " + e.getMessage());
        }
    }
}
