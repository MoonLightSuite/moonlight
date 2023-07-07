package io.github.moonlightsuite.moonlight.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;

/**
 * Singleton log4j2 wrapper for Moonlight logging
 */
public final class Logger {
    private static final String LOG_NAME = "Moonlight Log";

    private static volatile Logger instance;
    private final org.apache.logging.log4j.Logger log4jLogger;

    private Logger(String level) {
        log4jLogger = LogManager.getLogger(LOG_NAME);
        setLevel(level);
    }

    /**
     * In decreasing order of restriction, 8 options are available:
     * @param levelName <code>off</code> | <code>fatal</code> |
     *                  <code>error</code> | <code>warning</code> |
     *                  <code>info</code> | <code>debug</code> |
     *                  <code>trace</code> | otherwise defaults to
     *                  <code>all</code>
     */
    public void setLevel(String levelName) {
        Level level;
        switch(levelName)
        {
            case "off":
                level = Level.OFF;
                break;
            case "fatal":
                level = Level.FATAL;
                break;
            case "error":
                level = Level.ERROR;
                break;
            case "warning":
                level = Level.WARN;
                break;
            case "info":
                level = Level.INFO;
                break;
            case "debug":
                level = Level.DEBUG;
                break;
            case "trace":
                level = Level.TRACE;
                break;
            default:
                level = Level.ALL;
        }
        Configurator.setLevel(LOG_NAME, level);
        info("Logger level set to 'ALL'");
    }

    public static Logger getLogger() {
        Logger local = instance;
        if (local != null) {
            return local;
        }
        synchronized(Logger.class) {
            if (instance == null) {
                instance = new Logger("all");
            }
            return instance;
        }
    }

    public void fatal(String message) {
        log4jLogger.fatal(message);
    }

    public void error(String message) {
        log4jLogger.error(message);
    }

    public void warn(String message) {
        log4jLogger.warn(message);
    }

    public void info(String message) {
        log4jLogger.info(message);
    }

    public void debug(String message) {
        log4jLogger.debug(message);
    }

    public void trace(String message) {
        log4jLogger.trace(message);
    }


}

