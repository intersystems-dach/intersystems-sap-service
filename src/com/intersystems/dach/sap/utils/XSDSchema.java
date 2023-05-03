package com.intersystems.dach.sap.utils;

/**
 * A class that represents a XSD schema
 */
public class XSDSchema {

    private final String schema;
    private final boolean schemaComplete;

    public XSDSchema(String schema, boolean schemaComplete) {
        this.schema = schema;
        this.schemaComplete = schemaComplete;
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
     * Get the schemaCompleteFlag
     * 
     * @return the schemaCompleteFlag
     */
    public boolean isSchemaComplete() {
        return schemaComplete;
    }

}
