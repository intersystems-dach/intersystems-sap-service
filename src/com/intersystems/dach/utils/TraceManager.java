package com.intersystems.dach.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.intersystems.dach.sap.handlers.SAPServerTraceMsgHandler;

/**
 * This class manages the trace message handlers.
 * 
 * @author Philipp Bonin
 * @version 1.0
 */
public final class TraceManager {
    private static HashMap<Object ,TraceManager> instances = new HashMap<Object, TraceManager>();

    private Collection<SAPServerTraceMsgHandler> traceHandlers = new ArrayList<SAPServerTraceMsgHandler>();

    public static TraceManager getTraceManager(Object handle) {
        if (!instances.containsKey(handle)) {
            instances.put(handle, new TraceManager());
        }

        return instances.get(handle);
    }

    // make this class static
    private TraceManager() {}

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
