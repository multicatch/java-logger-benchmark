package io.github.multicatch.java;

import io.github.multicatch.BlackholeOutputStream;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * Handler for java.util.logging.
 * Redirects all output to {@link BlackholeOutputStream}.
 */
public class BlackholeHandler extends ConsoleHandler {

    public BlackholeHandler() {
        this.setOutputStream(new BlackholeOutputStream());
    }

    public static void replaceHandlersWithBlackhole(Logger logger) {
        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }

        logger.addHandler(new BlackholeHandler());
    }
}
