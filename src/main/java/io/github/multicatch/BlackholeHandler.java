package io.github.multicatch;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

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
