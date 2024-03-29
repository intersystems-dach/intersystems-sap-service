package com.intersystems.dach.sap.handlers;

import com.intersystems.dach.sap.SAPImportData;

/**
 * A callback interface when server receives import data.
 * 
 * @author Philipp Bonin, Andreas Schütz
 * @version 1.0
 * 
 */
public interface SAPServerImportDataHandler {
    /**
     * SAPImportDataHandler callback method
     * 
     * @param args the SAPImportData
     * @return the result of the method
     */
    void onImportDataReceived(SAPImportData data);

}
