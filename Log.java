package com.seleniumfy.utils;

//import org.slf4j.LoggerFactory;
import static java.lang.String.format;
import static java.util.logging.Level.OFF;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * @author Annameni Srinivas
 *         <a href="mailto:sannameni@gmail.com">sannameni@gmail.com</a>
 */
public class Log {

    private static java.util.logging.Logger selenium = java.util.logging.Logger.getLogger ("org.openqa.selenium.remote");

    public static Logger                    doLog;
    static {
        DOMConfigurator.configure (System.getProperty ("user.dir") + "/src/test/resources/log4j.xml");

    }

    /**
     * Log an info message
     * 
     * @param message
     *            An info message to log
     */
    public synchronized static void info (String message) {
        selenium.setLevel (OFF);
         doLog = LogManager.getLogger (getCallerClass ());
        doLog.info (message);
    }

    /**
     * Log a warning message
     * 
     * @param message
     *            A warning message to log
     */
    public synchronized static void warn (String message) {
        selenium.setLevel (OFF);
        doLog = LogManager.getLogger (getCallerClass ());
        doLog.warn (message);
    }

    /**
     * Log a debug message
     * 
     * @param message
     *            A debug message to log
     */
    public synchronized static void debug (String message) {
        selenium.setLevel (OFF);
        doLog = LogManager.getLogger (getCallerClass ());
        doLog.debug (message);
    }

    /**
     * Log an error message
     * 
     * @param message
     *            An error message to log
     */
    public synchronized static void error (String message) {
        selenium.setLevel (OFF);
        doLog = LogManager.getLogger (getCallerClass ());
        doLog.error (message);
    }

    /**
     * Log an error message and any {@link Throwable} {@link Exception}
     * 
     * @param message
     *            An error message to log
     * @param ex
     *            {@link Throwable} {@link Exception}
     */
    public synchronized static void error (String message, Throwable ex) {
        selenium.setLevel (OFF);
        doLog = LogManager.getLogger (getCallerClass ());
        doLog.error (message, ex);
    }

    private synchronized static String getCallerClass () {
        for (StackTraceElement elem : Thread.currentThread ().getStackTrace ())
            if (elem.getClassName () != Log.class.getName () && elem.getClassName () != Thread.class.getName ())
                return format ("%1$s [%2$s (:%3$d)]",
                               elem.getClassName (),
                               elem.getMethodName (),
                               elem.getLineNumber ());

        return null;
    }
}
