package com.intersystems.dach.sap;

import javax.naming.TimeLimitExceededException;

/**
 * A class that represents SAP Import Data
 * 
 * @author Philipp Bonin, Andreas Schütz
 * @version 1.0
 * 
 */
public class SAPImportData {

    private static long counter = 0;

    private final long ID;

    private final String functionName;

    private final String data;

    private final String schema;

    private final boolean isJSON;

    private boolean confirmed;

    /**
     * Create a new SAPImportData object without schema
     * 
     * @param data   - the data
     * @param schema - the schema
     */
    public SAPImportData(String functionName, String data, boolean isJSON) {
        this(functionName, data, isJSON, null);
    }

    /**
     * Create a new SAPImportData object with schema
     * 
     * @param data   - the data
     * @param schema - the schema
     */
    public SAPImportData(String functionName, String data, boolean isJSON, String schema) {
        this.functionName = functionName;
        this.data = data;
        this.isJSON = isJSON;
        this.schema = schema;
        counter++;
        ID = counter;
    }

    /**
     * Get the name of the SAP function
     * 
     * @return the function name.
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * Get the schema
     * 
     * @return the schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Get the data
     * 
     * @return the String
     */
    public String getData() {
        return data;
    }

    /**
     * Indicates if the data is in JSON format instead of XML.
     * 
     * @return ture if data is in JSON format, false if in XML format.
     */
    public boolean isJSON() {
        return isJSON;
    }

    /**
     * Causes current thread to wait till the processing of the data has been
     * confirmed.
     * 
     * @param timeoutMs the timeout in milliseconds.
     * @throws TimeLimitExceededException
     * @throws InterruptedException
     */
    public void waitForConfirmation(long timeoutMs) throws TimeLimitExceededException, InterruptedException {
        confirmed = false;
        synchronized (this) {
            this.wait(timeoutMs);
        }
        if (!confirmed) {
            throw new TimeLimitExceededException("Confirmation timeout " + timeoutMs + " expired");
        }
    }

    /**
     * Confirm that the data has been processed. All waiting threads will be
     * released.
     */
    public void confirmProcessed() {
        confirmed = true;
        synchronized (this) {
            this.notify();
        }
    }

    /**
     * Get the ID of the data
     * 
     * @return the ID
     */
    public long getID() {
        return ID;
    }

}
