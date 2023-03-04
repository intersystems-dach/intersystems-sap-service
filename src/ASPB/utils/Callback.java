package ASPB.utils;

/**
 * A callback interface to call a method with a single argument
 */
public interface Callback<T> {

    boolean call(T arg0) throws Exception;

}
