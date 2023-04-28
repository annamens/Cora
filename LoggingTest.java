/**
 * 
 */
package com.seleniumfy.utils;

/**
 * @author Annameni Srinivas
 *         <a href="mailto:sannameni@gmail.com">sannameni@gmail.com</a>
 */
public class LoggingTest extends Log {

    public synchronized static void testLog (String message) 
    {
        System.out.println ("test: " + message);

    }
    public synchronized static void errorLog (String message) {
        System.err.println ("error: " + message);

    }

}
