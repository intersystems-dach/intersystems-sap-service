package com.intersystems.dach.utils;

import java.util.ArrayList;
import java.util.Collection;

import com.intersystems.dach.sap.handlers.SAPServerTraceMsgHandler;

/**
 * This class manages the trace message handlers.
 * 
 * @author Philipp Bonin
 * @version 1.0
 */
public class TraceManager {

    private Collection<SAPServerTraceMsgHandler> traceHandlers = new ArrayList<SAPServerTraceMsgHandler>();

    /**
     * Register a trace message handler.
     * 
     * @param traceMsgHandler
     * @return true, if registration was successful.
     */
    public boolean registerTraceMsgHandler(SAPServerTraceMsgHandler traceMsgHandler) {
        return traceHandlers.add(traceMsgHandler);
    }

    /**
     * Unregister a trace message handler.
     * 
     * @param traceMsgHandler
     * @return true, if unregistration was successful.
     */
    public boolean unregisterTraceMsgHandler(SAPServerTraceMsgHandler traceMsgHandler) {
        return traceHandlers.remove(traceMsgHandler);
    }

    /**
     * Write a trace message to all registered handlers.
     * 
     * @param message The message to write.
     */
    public void traceMessage(String message) {
        for (SAPServerTraceMsgHandler handler : traceHandlers) {
            handler.onTraceMSg(message);
        }
    }
}
