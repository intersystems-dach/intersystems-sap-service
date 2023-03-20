package com.intersystems.dach.sap;

/**
 * A class that represents SAP Import Data
 * 
 * @author Philipp Bonin, Andreas Schütz
 * @version 1.0
 * 
 */
public class SAPServerImportData {

    private final String functionName;

    private final String data;

    private final String schema;

    /**
     * Create a new SAPImportData object without schema
     * 
     * @param data   - the data
     * @param schema - the schema
     */
    public SAPServerImportData(String functionName, String data) {
        this.functionName = functionName;
        this.data = data;
        this.schema = null;
    }

    /**
     * Create a new SAPImportData object with schema
     * 
     * @param data   - the data
     * @param schema - the schema
     */
    public SAPServerImportData(String functionName, String data, String schema) {
        this.functionName = functionName;
        this.data = data;
        this.schema = schema;
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

}
