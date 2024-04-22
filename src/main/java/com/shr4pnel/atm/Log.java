package com.shr4pnel.atm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log {
    private static final Logger logger = LogManager.getLogger("Main");

    // https://docs.oracle.com/javase/8/docs/technotes/guides/language/varargs.html
    public Log() {
        logger.trace("Initialized Log4j2");
    }

    public static void main(final String... args) {
    }

    public static void trace(final String arg) {
        logger.trace(arg);
    }

    public static void error(final String arg) {
        logger.error(arg);
    }

    public static void warn(final String arg) {
        logger.warn(arg);
    }
}
