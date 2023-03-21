package com.intersystems.dach.ens.bs.testing;

import java.util.Collection;

import com.intersystems.dach.ens.bs.utils.Buffer;
import com.intersystems.dach.sap.SAPServerImportData;
import com.intersystems.dach.sap.handlers.SAPServerImportDataHandler;

/**
 * A test tool to test InterSystems IRIS SAPService.
 * 
 * @author Andreas Schütz
 * @version 1.0
 */
public class SAPServiceTestRunner implements Runnable {

    Buffer<SAPServiceTestCase> testCaseBuffer = new Buffer<SAPServiceTestCase>();
    SAPServerImportDataHandler importDataHandler = null;
    Boolean isRunning = false;


    /**
     * Add a single test case to the test runner.
     * 
     * @param testCase  The test case to be added to the runner.
     * @return          True if test case was added successfully, false if not.
     */
    public boolean addTestCase(SAPServiceTestCase testCase) {
        return this.testCaseBuffer.add(testCase);
    }

    /**
     * Add a collection of test cases to the test runner.
     * 
     * @param testCases The test cases to be added to the runner.
     * @return          True if test all cases were added successfully, false if not.
     */
    public boolean addTestCases(Collection<SAPServiceTestCase> testCases) {
        if (testCases != null) {
            for (SAPServiceTestCase testCase : testCases) {
                if (!this.testCaseBuffer.add(testCase)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Run the tests asynchronously.
     * 
     * @param importDataHandler      The handler the tests will be send to. 
     * @throws IllegalStateException Thrown if test is alrady running.
     */
    public void runTestsAsync(SAPServerImportDataHandler importDataHandler) throws IllegalStateException {
        if (isRunning) {
            throw new IllegalStateException("SAP Service test is already running.");
        }

        this.importDataHandler = importDataHandler;

        Runnable runnable = this;
        Thread thread = new Thread(runnable);
        isRunning = true;
        thread.start();
    }

    @Override
    public void run() {
        SAPServiceTestCase testCase = this.testCaseBuffer.poll();
        while(testCase != null) {
            try {
                Thread.sleep(testCase.getWaitTimeMs());
            } catch (Exception e) { }
            SAPServerImportData importData = testCase.getTestData();
            importDataHandler.onImportDataReceived(importData);
            testCase = this.testCaseBuffer.poll();
        }
        isRunning = false;                
    }
}
