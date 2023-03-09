package ASPB.utils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public abstract @interface MyClassMetadata {

    public abstract java.lang.String Description()

    default "";

    public abstract java.lang.String InfoURL()

    default "";

    public abstract java.lang.String IconURL() default "";
}