package com.intersystems.dach.ens.sap.testing;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.intersystems.dach.sap.SAPImportData;
import com.intersystems.dach.sap.handlers.SAPServerImportDataHandler;

/**
 * A test tool to test InterSystems IRIS SAP Inbound Adapter.
 * 
 * @author Andreas Schütz
 * @version 1.0
 */
public class TestRunner implements Runnable {

    Queue<TestCase> testCaseQueue = new ConcurrentLinkedQueue<TestCase>();
    SAPServerImportDataHandler importDataHandler = null;
    TestStatusHandler testStatusHandler = null;
    Boolean isRunning = false;

    /**
     * Add a single test case to the test runner.
     * 
     * @param testCase The test case to be added to the runner.
     * @return True if test case was added successfully, false if not.
     */
    public boolean addTestCase(TestCase testCase) {
        return this.testCaseQueue.add(testCase);
    }

    /**
     * Add a collection of test cases to the test runner.
     * 
     * @param testCases The test cases to be added to the runner.
     * @return True if test all cases were added successfully, false if not.
     */
    public boolean addTestCases(Collection<TestCase> testCases) {
        if (testCases != null) {
            for (TestCase testCase : testCases) {
                if (!this.testCaseQueue.add(testCase)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Run the tests asynchronously.
     * 
     * @param importDataHandler The handler the tests will be send to.
     * @throws IllegalStateException Thrown if test is alrady running.
     */
    public void runTestsAsync(SAPServerImportDataHandler importDataHandler) {
        runTestsAsync(importDataHandler, null);
    }

    /**
     * Run the tests asynchronously.
     * 
     * @param importDataHandler The handler the tests will be send to.
     * @param testStatusHandler The handler test status messages.
     * @throws IllegalStateException Thrown if test is alrady running.
     */
    public void runTestsAsync(SAPServerImportDataHandler importDataHandler, TestStatusHandler testStatusHandler)
            throws IllegalStateException {
        if (isRunning) {
            throw new IllegalStateException("SAP Service test is already running.");
        }
        if (this.testCaseQueue.isEmpty()) {
            return; // no test cases in buffer
        }

        this.importDataHandler = importDataHandler;
        this.testStatusHandler = testStatusHandler;
        isRunning = true;
        new Thread(this).start();
    }

    @Override
    public void run() {

        TestCase testCase = testCaseQueue.poll();
        boolean lastTestCase = testCaseQueue.isEmpty();

        try {
            Thread.sleep(testCase.getWaitTimeMs());
        } catch (Exception e) { 
            // Ignore exception
        }

        SAPImportData importData = testCase.getTestData();
        if (testStatusHandler != null) {
            testStatusHandler.onTestStatus("Starting test '" + importData.getFunctionName() + "'.");
        }
        importDataHandler.onImportDataReceived(importData);

        if (!lastTestCase) {
            new Thread(this).start();
        }

        try {
            importData.waitForConfirmation(10000);
            if (testStatusHandler != null) {
                testStatusHandler.onTestStatus("Test '" + importData.getFunctionName() + "' has been confirmed.");
            }
        } catch (Exception e) {
            if (testStatusHandler != null) {
                testStatusHandler.onTestStatus("Test '" + importData.getFunctionName() + "' cofirmation timeout.");
            }
        }

        if (lastTestCase) {
            isRunning = false;
            if (testStatusHandler != null) {
                testStatusHandler.onTestStatus("Testing completed.");
            }
        }
    }
}
