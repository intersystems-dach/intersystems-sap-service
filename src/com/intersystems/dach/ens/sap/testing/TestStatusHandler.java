package com.intersystems.dach.ens.sap.testing;
/**
 * A callback interface when testing has completed..
 * 
 * @author Philipp Bonin, Andreas Schütz
 * @version 1.0
 * 
 */

public interface TestStatusHandler {
    /**
     * SAPServerErrorHandler callback method
     * 
     * @param string status message
     */
    public void onTestStatus(String msg);

}