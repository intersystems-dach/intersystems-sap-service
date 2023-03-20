package com.intersystems.dach.sap.handlers;
/**
 * A callback interface when server has an error or exception.
 * 
 * @author Philipp Bonin, Andreas Schütz
 * @version 1.0
 * 
 */

public interface SAPServerExceptionHandler {
    /**
     * SAPServerErrorHandler callback method
     * 
     * @param e exception object
     */
    public void OnExceptionOccured(Exception e);

}
