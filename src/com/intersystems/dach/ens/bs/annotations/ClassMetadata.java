package com.intersystems.dach.ens.bs.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * This Field Annotation is used to make to code compatible with version 
 * of <= intersystems-utiles-3.2.0 library. The field annotaton is used 
 * to add additional information like the category to an IRIS property.
 * In the older versions of the intersystems-utils library this annotation does
 * not exist yet. To compile the project using newer version simply comment this
 * file out and add com.intersystems.enslib.pex.FieldMetadata to the imports.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public abstract @interface ClassMetadata {

    public abstract java.lang.String Description() default "";

    public abstract java.lang.String InfoURL() default "";

    public abstract java.lang.String IconURL() default "";
}