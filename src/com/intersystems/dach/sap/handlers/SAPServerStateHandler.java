package com.intersystems.dach.sap.handlers;

import com.sap.conn.jco.server.JCoServerState;

/**
 * A callback interface when server has an error or exception.
 * 
 * @author Philipp Bonin, Andreas Schütz
 * @version 1.0
 * 
 */

public interface SAPServerStateHandler {
    /**
     * SAPServerStateHandler callback method
     * Defined states are: STARTED, DEAD, ALIVE, STOPPED;
     * 
     * @param oldState Old State of server
     * @param newState New state of server
     */
    public void onStateChanged(JCoServerState oldState, JCoServerState newState);

}
