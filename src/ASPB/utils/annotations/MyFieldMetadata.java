package ASPB.utils.annotations;

public abstract @interface MyFieldMetadata {

    public abstract boolean IsRequired() default false;

    public abstract java.lang.String DataType() default "";

    public abstract java.lang.String Category() default "Additional";

    public abstract java.lang.String Description() default "";

    public abstract boolean ExcludeFromSettings() default false;
}
