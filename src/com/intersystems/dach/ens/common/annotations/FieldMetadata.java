package com.intersystems.dach.ens.common.annotations;

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
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public abstract @interface FieldMetadata {

    public abstract boolean IsRequired() default false;

    public abstract java.lang.String DataType() default "";

    public abstract java.lang.String Category() default "Additional";

    public abstract java.lang.String Description() default "";

    public abstract boolean ExcludeFromSettings() default false;
}
