package io.github.multicatch.log4j;

import io.github.multicatch.BlackholeOutputStream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.OutputStreamAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class BlackholeLoggers {

    public static Logger createDefault(String name) {
        final LoggerContext context = createContext(name);
        final Configuration config = context.getConfiguration();
        final PatternLayout layout = PatternLayout.createDefaultLayout(config);
        final BlackholeOutputStream blackholeOutputStream = new BlackholeOutputStream();
        final Appender appender = OutputStreamAppender.createAppender(layout, null, blackholeOutputStream, "blackhole", false, true);
        appender.start();
        config.addAppender(appender);
        updateLoggers(appender, config, name);
        return context.getLogger(name);
    }

    private static LoggerContext createContext(String name) {
        ConfigurationBuilder<BuiltConfiguration> configurationBuilder = ConfigurationBuilderFactory.newConfigurationBuilder();

        configurationBuilder.add(configurationBuilder.newLogger(name, org.apache.logging.log4j.Level.INFO)
                .addAttribute("additivity", false));

        return Configurator.initialize(configurationBuilder.build());
    }

    private static void updateLoggers(final Appender appender, final Configuration config, final String loggerName) {
        LoggerConfig loggerConfig = config.getLoggers().get(loggerName);
        loggerConfig.addAppender(appender, Level.INFO, null);
        loggerConfig.setAdditive(false);
    }

}

