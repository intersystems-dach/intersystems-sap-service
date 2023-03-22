package com.intersystems.dach.sap.handlers;

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
    public JCoServerFunctionHandlerImpl(SAPServerImportDataHandler importDataHandler, 
            boolean useJson, long confirmationTimeoutMs) {
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

        if (useJson) {
            data = function.getImportParameterList().toJSON();
        } else {
            try {
                data = XMLUtils.convert(function.getImportParameterList().toXML(), functionName);
                schema = XSDUtils.createXSDString(function, true, false);
            } catch (Exception e) {
                throw new AbapClassException(e);
            }
        }
        try {
            // call the import data handler function
            SAPImportData importData = new SAPImportData(functionName, data, useJson, schema);
            importDataHandler.onImportDataReceived(importData);
            importData.waitForConfirmation(confirmationTimeoutMs);
        } catch (InterruptedException e) {
            throw new AbapException("SYSTEM_FAILURE", "Confirmation Timeout. InputData wasn't handled in time.");
        } catch (Exception e) {
            throw new AbapException("SYSTEM_FAILURE", "Could not process import parameters: " + e.getMessage());
        }
    }

}
