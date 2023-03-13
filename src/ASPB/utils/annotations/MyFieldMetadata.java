package ASPB.utils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public abstract @interface MyFieldMetadata {

    public abstract boolean IsRequired() default false;

    public abstract java.lang.String DataType() default "";

    public abstract java.lang.String Category() default "Additional";

    public abstract java.lang.String Description() default "";

    public abstract boolean ExcludeFromSettings() default false;
}
