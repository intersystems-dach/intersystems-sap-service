package ASPB.utils.annotations;

public abstract @interface MyClassMetadata {

    public abstract java.lang.String Description()

    default "";

    public abstract java.lang.String InfoURL()

    default "";

    public abstract java.lang.String IconURL() default "";
}