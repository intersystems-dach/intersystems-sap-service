package com.intersystems.dach.ens.sap.utils;

import java.util.ArrayList;
import java.util.Collection;

import com.intersystems.dach.sap.handlers.SAPServerTraceMsgHandler;

/**
 * This class manages the trace message handlers.
 * 
 * @author Philipp Bonin
 * @version 1.0
 */
public final class TraceManager {

    private static Collection<SAPServerTraceMsgHandler> traceHandlers = new ArrayList<SAPServerTraceMsgHandler>();

    // Make this a static class
    private TraceManager() {
    }

    /**
     * Register a trace message handler.
     * 
     * @param traceMsgHandler
     * @return true, if registration was successful.
     */
    public static boolean registerTraceMsgHandler(SAPServerTraceMsgHandler traceMsgHandler) {
        return traceHandlers.add(traceMsgHandler);
    }

    /**
     * Unregister a trace message handler.
     * 
     * @param traceMsgHandler
     * @return true, if unregistration was successful.
     */
    public static boolean unregisterTraceMsgHandler(SAPServerTraceMsgHandler traceMsgHandler) {
        return traceHandlers.remove(traceMsgHandler);
    }

    /**
     * Write a trace message to all registered handlers.
     * 
     * @param message The message to write.
     */
    public static void traceMessage(String message) {
        for (SAPServerTraceMsgHandler handler : traceHandlers) {
            handler.onTraceMSg(message);
        }
    }
}
