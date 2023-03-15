package ASPB.utils;

/**
 * A class to store a String and a schema
 * 
 * @author Philipp Bonin
 * @version 1.0
 * 
 */
public class StringWithSchema {

    private final String text;

    private final String schema;

    /**
     * Create a new StringWithSchema object
     * 
     * @param text   - the String
     * @param schema - the schema
     */
    public StringWithSchema(String text, String schema) {
        this.text = text;
        this.schema = schema;
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
     * Get the String
     * 
     * @return the String
     */
    public String getText() {
        return text;
    }

}
