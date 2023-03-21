package com.intersystems.dach.ens.bs.testing;

import com.intersystems.dach.sap.SAPServerImportData;

/**
 * A test case to test InterSystems IRIS SAPService.
 * 
 * @author Andreas Schütz
 * @version 1.0
 */
public class SAPServiceTestCase {

    private SAPServerImportData testData;
    private long waitTimeMs;

    public SAPServiceTestCase(SAPServerImportData testData, long waitTimeMs) {
        this.testData = testData;
        this.waitTimeMs = waitTimeMs;
    }

    public SAPServerImportData getTestData() {
        return testData;
    }

    public long getWaitTimeMs() {
        return waitTimeMs;
    }
    
}
