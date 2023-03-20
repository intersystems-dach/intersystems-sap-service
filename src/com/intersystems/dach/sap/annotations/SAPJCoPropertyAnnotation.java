package com.intersystems.dach.sap.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for JCo properties
 * 
 * @author Philipp Bonin
 * @version 1.0
 * 
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SAPJCoPropertyAnnotation {

    /**
     * The name of the JCO property refered by
     * {@link com.sap.conn.jco.ext.DestinationDataProvider} or
     * {@link com.sap.conn.jco.ext.ServerDataProvider}.
     * 
     * @return the name of the JCO property
     */
    String jCoName();
}
