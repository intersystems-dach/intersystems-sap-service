package ASPB.utils.interfaces;

/**
 * A callback interface to call a method with a single argument
 * 
 * @author Philipp Bonin
 * @version 1.0
 * 
 */
public interface Callback<T> {

    /**
     * Call the method
     * 
     * @param arg0 the argument to pass to the method
     * @return the result of the method
     * @throws Exception if an error occurs
     */
    boolean call(T arg0);

}
