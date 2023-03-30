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
     * This method is called when the server sends a status message.
     * 
     * @param traceMsg The status message.
     */
    public void onTraceMSg(String traceMsg);

}
