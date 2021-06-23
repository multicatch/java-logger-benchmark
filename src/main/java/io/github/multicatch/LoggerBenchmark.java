package io.github.multicatch;

import ch.qos.logback.classic.LoggerContext;
import io.github.multicatch.java.BlackholeHandler;
import io.github.multicatch.log4j.BlackholeLoggers;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.slf4j.impl.StaticLoggerBinder;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.multicatch.logback.BlackholeAppender.replaceAppendersWithBlackhole;

public class LoggerBenchmark {

    private static final String SIMPLE_MESSAGE = "Hello there!";
    private static final String MESSAGE_WITH_PARAMS = "Hello there, {}, {}, {}";
    private static final String MESSAGE_WITH_PARAMS_JAVA_UTIL = "Hello there, {0}, {1}, {2}";
    private static final Object[] PARAMS = new Object[] { "Jane", 2, new DummyType() };

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(LoggerBenchmark.class.getSimpleName())
                .mode(Mode.Throughput)
                .timeUnit(TimeUnit.MILLISECONDS)
                .warmupTime(TimeValue.seconds(10))
                .warmupIterations(5)
                .measurementIterations(10)
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Benchmark
    public void javaLogger(BenchmarkState state) {
        Logger javaLogger = state.getJavaLogger();
        javaLogger.log(Level.INFO, MESSAGE_WITH_PARAMS_JAVA_UTIL, PARAMS);
    }

    @Benchmark
    public void javaLoggerWithoutParams(BenchmarkState state) {
        Logger javaLogger = state.getJavaLogger();
        javaLogger.log(Level.INFO, SIMPLE_MESSAGE);
    }

    @Benchmark
    public void logbackLogger(BenchmarkState state) {
        org.slf4j.Logger logbackLogger = state.getLogbackLogger();
        logbackLogger.info(MESSAGE_WITH_PARAMS, PARAMS);
    }

    @Benchmark
    public void logbackLoggerWithoutParams(BenchmarkState state) {
        org.slf4j.Logger logbackLogger = state.getLogbackLogger();
        logbackLogger.info(SIMPLE_MESSAGE);
    }

    @Benchmark
    public void log4jLogger(BenchmarkState state) {
        org.apache.logging.log4j.Logger log4jLogger = state.getLog4jLogger();
        log4jLogger.info(MESSAGE_WITH_PARAMS, PARAMS);
    }

    @Benchmark
    public void log4jLoggerWithoutParams(BenchmarkState state) {
        org.apache.logging.log4j.Logger log4jLogger = state.getLog4jLogger();
        log4jLogger.info(SIMPLE_MESSAGE);
    }

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        private final Logger javaLogger = Logger.getLogger(BenchmarkState.class.getName());
        {
            BlackholeHandler.replaceHandlersWithBlackhole(javaLogger);
        }

        private final ch.qos.logback.classic.Logger logbackLogger;
        {
            LoggerContext loggerFactory = (LoggerContext) StaticLoggerBinder.getSingleton().getLoggerFactory();
            logbackLogger = loggerFactory.getLogger(BenchmarkState.class);
            replaceAppendersWithBlackhole(logbackLogger);
        }

        private final org.apache.logging.log4j.Logger log4jLogger = BlackholeLoggers.createDefault(BenchmarkState.class.getName());

        public Logger getJavaLogger() {
            return javaLogger;
        }

        public org.slf4j.Logger getLogbackLogger() {
            return logbackLogger;
        }

        public org.apache.logging.log4j.Logger getLog4jLogger() {
            return log4jLogger;
        }
    }

    private static class DummyType {
        // dummy type for tricky message evaluation
    }
}
