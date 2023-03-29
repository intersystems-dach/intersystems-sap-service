package com.intersystems.dach.sap.handlers;

/**
 * A callback interface when server server sends a status message.
 * 
 * @author Philipp Bonin, Andreas Schütz
 * @version 1.0
 * 
 */

public interface SAPServerTraceMsgHandler {
    /**
     * SAPServerStateHandler callback method
     * Defined states are: STARTED, DEAD, ALIVE, STOPPED;
     * 
     * @param oldState Old State of server
     * @param newState New state of server
     */
    public void onTraceMSg(String traceMsg);

}
