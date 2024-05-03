package com.shr4pnel.atm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Used to handle calls to log4j.
 * @author <a href="https://github.com/shrapnelnet">Tyler</a>
 * @version 1.4.0
 * @since 1.0.0
 */
public class Log {
    /** An instance of Logger, used to log to the console */
    private static final Logger logger = LogManager.getLogger("Main");

    /**
     * Logs a message to the console as TRACE
     * @param arg The message to be logged
     */
    public static void trace(final String arg) {
        logger.trace(arg);
    }

    /**
     * Logs a message to the console as ERROR
     * @param arg The message to be logged
     */
    public static void error(final String arg) {
        logger.error(arg);
    }

    /**
     * Logs a message to the console as WARN
     * @param arg The message to be logged
     */
    public static void warn(final String arg) {
        logger.warn(arg);
    }
}
