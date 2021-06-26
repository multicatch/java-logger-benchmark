package io.github.multicatch;

import ch.qos.logback.classic.LoggerContext;
import io.github.multicatch.java.BlackholeHandler;
import io.github.multicatch.log4j.BlackholeLoggers;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.slf4j.impl.StaticLoggerBinder;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.github.multicatch.logback.BlackholeAppender.replaceAppendersWithBlackhole;

public class LoggerBenchmark {

    private static final String SIMPLE_MESSAGE = "Hello there!";
    private static final String MESSAGE_WITH_PARAMS = "Hello there, {}, {}, {}";
    private static final String MESSAGE_WITH_PARAMS_JAVA_UTIL = "Hello there, {0}, {1}, {2}";
    private static final Object[] PARAMS = new Object[] { "Jane", 2, new DummyType() };
    private static final Throwable THROWABLE = new IllegalStateException("Error");

    private static final List<Options> BENCHMARK_CONFIGS = Arrays.asList(
            createOptions(Mode.Throughput, TimeUnit.MILLISECONDS)
                    .result("throughput.json")
                    .build(),
            createOptions(Mode.SampleTime, TimeUnit.MICROSECONDS)
                    .result("sample-time.json")
                    .build(),
            createOptions(Mode.AverageTime, TimeUnit.MICROSECONDS)
                    .result("average.json")
                    .build()
    );

    public static void main(String[] args) throws RunnerException {
        for (Options opt : BENCHMARK_CONFIGS) {
            new Runner(opt).run();
        }
    }

    static ChainedOptionsBuilder createOptions(Mode mode, TimeUnit timeUnit) {
        return new OptionsBuilder()
                .include(LoggerBenchmark.class.getSimpleName())
                .mode(mode)
                .timeUnit(timeUnit)
                .warmupTime(TimeValue.seconds(10))
                .warmupIterations(5)
                .measurementIterations(10)
                .forks(1)
                .jvmArgsAppend("-Djava.lang.invoke.stringConcat=BC_SB")
                .resultFormat(ResultFormatType.JSON);
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
    public void javaLoggerThrowable(BenchmarkState state) {
        Logger javaLogger = state.getJavaLogger();
        javaLogger.log(Level.INFO, THROWABLE, () -> SIMPLE_MESSAGE);
    }

    @Benchmark
    public void javaLoggerDisabled(BenchmarkState state) {
        Logger javaLogger = state.getJavaLogger();
        javaLogger.log(Level.FINEST, MESSAGE_WITH_PARAMS_JAVA_UTIL, PARAMS);
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
    public void logbackLoggerThrowable(BenchmarkState state) {
        org.slf4j.Logger logbackLogger = state.getLogbackLogger();
        logbackLogger.error(SIMPLE_MESSAGE, THROWABLE);
    }

    @Benchmark
    public void logbackLoggerDisabled(BenchmarkState state) {
        org.slf4j.Logger logbackLogger = state.getLogbackLogger();
        logbackLogger.trace(MESSAGE_WITH_PARAMS, PARAMS);
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

    @Benchmark
    public void log4jLoggerThrowable(BenchmarkState state) {
        org.apache.logging.log4j.Logger log4jLogger = state.getLog4jLogger();
        log4jLogger.error(SIMPLE_MESSAGE, THROWABLE);
    }

    @Benchmark
    public void log4jLoggerDisabled(BenchmarkState state) {
        org.apache.logging.log4j.Logger log4jLogger = state.getLog4jLogger();
        log4jLogger.trace(MESSAGE_WITH_PARAMS, PARAMS);
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
