package com.intersystems.dach.sap.handlers;
/**
 * A callback interface when server has an error or exception.
 * 
 * @author Philipp Bonin, Andreas Schütz
 * @version 1.0
 * 
 */

public interface SAPServerErrorHandler {
    /**
     * SAPServerErrorHandler callback method
     * 
     * @param err error object
     */
    public void OnErrorOccured(Error err);

}
