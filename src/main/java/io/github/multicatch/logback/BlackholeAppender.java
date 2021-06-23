package io.github.multicatch.logback;


import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import io.github.multicatch.BlackholeOutputStream;

import java.io.OutputStream;

public class BlackholeAppender extends OutputStreamAppender<ILoggingEvent> {
    private static final String PATTERN = "%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n";

    @Override
    public void start() {
        OutputStream targetStream = new BlackholeOutputStream();
        setOutputStream(targetStream);
        PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
        patternLayoutEncoder.setContext(this.context);
        patternLayoutEncoder.setPattern(PATTERN);
        patternLayoutEncoder.start();
        setEncoder(patternLayoutEncoder);
        super.start();
    }

    public static void replaceAppendersWithBlackhole(Logger logger) {
        logger.detachAndStopAllAppenders();
        BlackholeAppender blackholeAppender = new BlackholeAppender();
        blackholeAppender.setContext(logger.getLoggerContext());
        blackholeAppender.start();
        logger.addAppender(blackholeAppender);
        logger.setAdditive(false);
    }
}
