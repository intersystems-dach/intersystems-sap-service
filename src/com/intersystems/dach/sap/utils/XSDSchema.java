package com.intersystems.dach.sap.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that represents a XSD schema
 */
public class XSDSchema {

    private String schema;
    private boolean schemaComplete;
    private List<String> incompleteTableList;

    public XSDSchema(String schema, boolean schemaComplete) {
        this.schema = schema;
        this.schemaComplete = schemaComplete;
        this.incompleteTableList = new ArrayList<String>();
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
     * Set the schema
     * 
     * @param schema the schema
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * Get the schemaCompleteFlag
     * 
     * @return the schemaCompleteFlag
     */
    public boolean isSchemaComplete() {
        return schemaComplete;
    }

    /**
     * Get the incompleteTableList
     * 
     * @return the incompleteTableList
     */
    public List<String> getIncompleteTableList() {
        return incompleteTableList;
    }

    /**
     * Add a table to the incompleteTableList
     * 
     * @param tableName the table name
     * @return true if the table was added, false if the table was already in the
     *         list
     */
    public boolean addIncompleteTable(String tableName) {
        if (incompleteTableList.contains(tableName)) {
            return false;
        }
        return incompleteTableList.add(tableName);
    }

    /**
     * Add a list of tables to the incompleteTableList
     * 
     * @param tableNames the table names
     */
    public void addIncompleteTable(List<String> tableNames) {
        for (String tableName : tableNames) {
            addIncompleteTable(tableName);
        }
    }

    /**
     * Remove a table from the incompleteTableList
     * 
     * @param tableName the table name
     * @return true if the table was removed, false if the table was not in the list
     */
    public boolean removeIncompleteTable(String tableName) {
        boolean r = incompleteTableList.remove(tableName);
        this.schemaComplete = incompleteTableList.isEmpty();
        return r;
    }

}
