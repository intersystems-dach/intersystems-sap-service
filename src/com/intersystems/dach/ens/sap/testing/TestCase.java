package com.intersystems.dach.ens.sap.testing;

import com.intersystems.dach.sap.SAPImportData;

/**
 * A test case to test InterSystems IRIS SAPService.
 * 
 * @author Andreas Schütz
 * @version 1.0
 */
public class TestCase {

    private SAPImportData testData;
    private long waitTimeMs;

    public TestCase(SAPImportData testData, long waitTimeMs) {
        this.testData = testData;
        this.waitTimeMs = waitTimeMs;
    }

    /**
     * @return SAPImportData
     */
    public SAPImportData getTestData() {
        return testData;
    }

    /**
     * @return Waiting time in milliseconds
     */
    public long getWaitTimeMs() {
        return waitTimeMs;
    }
    
}
