package eu.quanticol.moonlight.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoggerTest {

    @Test
    void getLogger() {
        Logger logger = Logger.getLogger();
        logger.fatal("fatal message");
        logger.error("error message");
        logger.warn("warning message");
        logger.info("info message");
        logger.debug("debug message");
        logger.trace("trace message");

        Logger logger2 = Logger.getLogger();
        assertEquals(logger, logger2);
    }
}