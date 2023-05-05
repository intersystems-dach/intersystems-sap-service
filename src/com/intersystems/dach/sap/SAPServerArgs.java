package com.intersystems.dach.sap;

import java.util.Properties;

import com.intersystems.dach.utils.TraceManager;

/**
 * A class to privide objects from the InboundAdapter to the other classes.
 */
public class SAPServerArgs {

    private final TraceManager traceManager;
    private final boolean flattenTablesItems;
    private final int confirmationTimeoutMs;
    private final boolean useJson;
    private final Properties sapProperties;

    public SAPServerArgs(Properties sapProperties) {
        this(sapProperties, new TraceManager(), false, 20000, false);
    }

    public SAPServerArgs(Properties sapProperties,
            TraceManager traceManager,
            boolean flattenTablesItems,
            int confirmationTimeoutMs,
            boolean useJson) {
        this.flattenTablesItems = flattenTablesItems;
        this.confirmationTimeoutMs = confirmationTimeoutMs;
        this.useJson = useJson;
        this.sapProperties = sapProperties;
        this.traceManager = traceManager;
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

    /**
     * Get the SAP properties.
     * 
     * @return The SAP properties
     */
    public Properties getSapProperties() {
        return sapProperties;
    }

    /**
     * Get the trace manager.
     * 
     * @return The trace manager
     */
    public TraceManager getTraceManager() {
        return traceManager;
    }

}
