package com.intersystems.dach.sap;

import java.util.Properties;

import com.intersystems.dach.utils.TraceManager;

/**
 * A class to privide objects from the InboundAdapter to the other classes.
 */
public class SAPServerArgs {

    private final TraceManager traceManager;
    private final boolean flattenTablesItems;
    private final boolean completeSchema;
    private final int confirmationTimeoutMs;
    private final boolean useJson;
    private final Properties sapProperties;
    private final String xmlNamespace;

    public SAPServerArgs(Properties sapProperties) {
        this(sapProperties, new TraceManager(), false, 20000, false, "", false);
    }

    public SAPServerArgs(
            Properties sapProperties,
            TraceManager traceManager,
            boolean flattenTablesItems,
            int confirmationTimeoutMs,
            boolean useJson,
            String xmlNamespace,
            boolean completeSchema) {
        this.flattenTablesItems = flattenTablesItems;
        this.completeSchema = completeSchema;
        this.confirmationTimeoutMs = confirmationTimeoutMs;
        this.useJson = useJson;
        this.sapProperties = sapProperties;
        this.traceManager = traceManager;

        if (xmlNamespace == null) {
            this.xmlNamespace = "";
        } else {
            this.xmlNamespace = xmlNamespace;
        }
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
     * Get the complete schema flag.
     * 
     * @return The complete schema flag
     */
    public boolean isCompleteSchema() {
        return completeSchema;
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

    /**
     * Get the XML namespace.
     * 
     * @return The XML namespace
     */
    public String getXmlNamespace() {
        return xmlNamespace;
    }

    /**
     * Get the converted XML namespace. Where the placeholder {functionName} is set
     * to the given function name.
     * 
     * @param functionName The function name
     * @return The converted XML namespace
     */
    public String getConvertedXMLNamespace(String functionName) {
        return xmlNamespace.replace("{functionName}", functionName);
    }

}
