package com.intersystems.dach.utils;

/**
 * A class to privide objects from the InboundAdapter to the other classes.
 */
public class ObjectProvider {

    private Object traceManagerHandle;
    private Object warningTraceManagerHandle;
    private boolean flattenTablesItems;
    private int confirmationTimeoutMs = 20000;
    private boolean useJson;

    public ObjectProvider() {
    }

    public ObjectProvider(Object traceManagerHandle,
            Object warningTraceManagerHandle,
            boolean flattenTablesItems,
            int confirmationTimeoutMs,
            boolean useJson) {
        this.traceManagerHandle = traceManagerHandle;
        this.warningTraceManagerHandle = warningTraceManagerHandle;
        this.flattenTablesItems = flattenTablesItems;
        this.confirmationTimeoutMs = confirmationTimeoutMs;
        this.useJson = useJson;
    }

    /*
     * ************
     * ***SETTER***
     * ************
     */

    /**
     * Set the flatten tables items flag. If this flag is set to true, the tables
     * items are flattened. This means that the table items are not returned as
     * tables, but as a list of objects.
     * 
     * @param flattenTablesItems The flatten tables items flag
     */
    public void setFlattenTablesItems(boolean flattenTablesItems) {
        this.flattenTablesItems = flattenTablesItems;
    }

    /**
     * Set the trace manager handle. This is the handle to the trace
     * 
     * @param traceManagerHandle The trace manager handle
     */
    public void setTraceManagerHandle(Object traceManagerHandle) {
        this.traceManagerHandle = traceManagerHandle;
    }

    /**
     * Set the warning trace manager handle. This is the handle to the warning trace
     * 
     * @param warningTraceManagerHandle The warning trace manager handle
     */
    public void setWarningTraceManagerHandle(Object warningTraceManagerHandle) {
        this.warningTraceManagerHandle = warningTraceManagerHandle;
    }

    /**
     * Set the confirmation timeout. This is the time the function handler waits
     * till the processing of the input data has been confirmed.
     * 
     * @param confirmationTimeoutMs Must be at least 200 ms.
     */
    public boolean setConfirmationTimeoutMs(int confirmationTimeoutMs) {
        if (confirmationTimeoutMs >= 200) {
            this.confirmationTimeoutMs = confirmationTimeoutMs;
            return true;
        }

        return false;
    }

    /**
     * Set the use JSON flag. If this flag is set to true, the input data is
     * converted to JSON.
     * 
     * @param useJson The use JSON flag
     */
    public void setUseJson(boolean useJson) {
        this.useJson = useJson;
    }

    /*
     * ************
     * ***GETTER***
     * ************
     */

    /**
     * Get the trace manager handle. This is the handle to the trace
     * 
     * @return The trace manager handle
     */
    public Object getTraceManagerHandle() {
        return traceManagerHandle;
    }

    /**
     * Get the warning trace manager handle. This is the handle to the warning trace
     * 
     * @return The warning trace manager handle
     */
    public Object getWarningTraceManagerHandle() {
        return warningTraceManagerHandle;
    }

    /**
     * Get the flatten tables items flag. If this flag is set to true, the tables
     * items are flattened. This means that the table items are not returned as
     * tables, but as a list of objects.
     * 
     * @return The flatten tables items flag
     */
    public boolean isFlattenTablesItems() {
        return flattenTablesItems;
    }

    /**
     * Get the confirmation timeout. This is the time the function handler waits
     * till the processing of the input data has been confirmed.
     * 
     * @return The confirmation timeout
     */
    public int getConfirmationTimeoutMs() {
        return confirmationTimeoutMs;
    }

    /**
     * Get the use JSON flag. If this flag is set to true, the input data is
     * converted to JSON.
     * 
     * @return The use JSON flag
     */
    public boolean isUseJson() {
        return useJson;
    }

}
