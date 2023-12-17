package com.jolly.astra.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.spi.ContextAwareBase;
import com.jolly.astra.config.ApplicationProperties;
import net.logstash.logback.appender.LogstashTcpSocketAppender;
import net.logstash.logback.composite.ContextJsonProvider;
import net.logstash.logback.composite.GlobalCustomFieldsJsonProvider;
import net.logstash.logback.composite.loggingevent.ArgumentsJsonProvider;
import net.logstash.logback.composite.loggingevent.LogLevelJsonProvider;
import net.logstash.logback.composite.loggingevent.LoggerNameJsonProvider;
import net.logstash.logback.composite.loggingevent.LoggingEventFormattedTimestampJsonProvider;
import net.logstash.logback.composite.loggingevent.LoggingEventJsonProviders;
import net.logstash.logback.composite.loggingevent.LoggingEventPatternJsonProvider;
import net.logstash.logback.composite.loggingevent.LoggingEventThreadNameJsonProvider;
import net.logstash.logback.composite.loggingevent.MdcJsonProvider;
import net.logstash.logback.composite.loggingevent.MessageJsonProvider;
import net.logstash.logback.composite.loggingevent.StackTraceJsonProvider;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;
import net.logstash.logback.encoder.LogstashEncoder;
import net.logstash.logback.stacktrace.ShortenedThrowableConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * @author jolly
 */
public final class LoggingUtils {
  private static final Logger log = LoggerFactory.getLogger(LoggingUtils.class);
  private static final String CONSOLE_APPENDER_NAME = "CONSOLE";
  private static final String ASYNC_LOGSTASH_APPENDER_NAME = "ASYNC_LOGSTASH";

  private LoggingUtils() {
  }

  public static void addJsonConsoleAppender(LoggerContext context, String customFields) {
    log.info("Initializing Console loggingProperties");
    ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
    consoleAppender.setContext(context);
    consoleAppender.setEncoder(compositeJsonEncoder(context, customFields));
    consoleAppender.setName(CONSOLE_APPENDER_NAME);
    consoleAppender.start();
    context.getLogger("ROOT").detachAppender(CONSOLE_APPENDER_NAME);
    context.getLogger("ROOT").addAppender(consoleAppender);
  }

  public static void addLogstashTcpSocketAppender(LoggerContext context, String customFields, ApplicationProperties.Logging.Logstash logstashProperties) {
    log.info("Initializing Logstash loggingProperties");
    LogstashTcpSocketAppender logstashAppender = new LogstashTcpSocketAppender();
    logstashAppender.addDestinations(new InetSocketAddress(logstashProperties.getHost(), logstashProperties.getPort()));
    logstashAppender.setContext(context);
    logstashAppender.setEncoder(logstashEncoder(customFields));
    logstashAppender.setName(ASYNC_LOGSTASH_APPENDER_NAME);
    logstashAppender.setRingBufferSize(logstashProperties.getRingBufferSize());
    logstashAppender.start();
    context.getLogger("ROOT").addAppender(logstashAppender);
  }

  public static void addContextListener(LoggerContext context, String customFields, ApplicationProperties.Logging properties) {
    LogbackLoggerContextListener loggerContextListener = new LogbackLoggerContextListener(properties, customFields);
    loggerContextListener.setContext(context);
    context.addListener(loggerContextListener);
  }

  private static LoggingEventCompositeJsonEncoder compositeJsonEncoder(LoggerContext context, String customFields) {
    LoggingEventCompositeJsonEncoder compositeJsonEncoder = new LoggingEventCompositeJsonEncoder();
    compositeJsonEncoder.setContext(context);
    compositeJsonEncoder.setProviders(jsonProviders(context, customFields));
    compositeJsonEncoder.start();
    return compositeJsonEncoder;
  }

  private static LogstashEncoder logstashEncoder(String customFields) {
    LogstashEncoder logstashEncoder = new LogstashEncoder();
    logstashEncoder.setThrowableConverter(throwableConverter());
    logstashEncoder.setCustomFields(customFields);
    return logstashEncoder;
  }

  private static LoggingEventJsonProviders jsonProviders(LoggerContext context, String customFields) {
    LoggingEventJsonProviders jsonProviders = new LoggingEventJsonProviders();
    jsonProviders.addArguments(new ArgumentsJsonProvider());
    jsonProviders.addContext(new ContextJsonProvider<>());
    jsonProviders.addGlobalCustomFields(customFieldsJsonProvider(customFields));
    jsonProviders.addLogLevel(new LogLevelJsonProvider());
    jsonProviders.addLoggerName(loggerNameJsonProvider());
    jsonProviders.addMdc(new MdcJsonProvider());
    jsonProviders.addMessage(new MessageJsonProvider());
    jsonProviders.addPattern(new LoggingEventPatternJsonProvider());
    jsonProviders.addStackTrace(stackTraceJsonProvider());
    jsonProviders.addThreadName(new LoggingEventThreadNameJsonProvider());
    jsonProviders.addTimestamp(timestampJsonProvider());
    jsonProviders.setContext(context);
    return jsonProviders;
  }

  private static GlobalCustomFieldsJsonProvider<ILoggingEvent> customFieldsJsonProvider(String customFields) {
    GlobalCustomFieldsJsonProvider<ILoggingEvent> customFieldsJsonProvider = new GlobalCustomFieldsJsonProvider<>();
    customFieldsJsonProvider.setCustomFields(customFields);
    return customFieldsJsonProvider;
  }

  private static LoggerNameJsonProvider loggerNameJsonProvider() {
    LoggerNameJsonProvider loggerNameJsonProvider = new LoggerNameJsonProvider();
    loggerNameJsonProvider.setShortenedLoggerNameLength(20);
    return loggerNameJsonProvider;
  }

  private static StackTraceJsonProvider stackTraceJsonProvider() {
    StackTraceJsonProvider stackTraceJsonProvider = new StackTraceJsonProvider();
    stackTraceJsonProvider.setThrowableConverter(throwableConverter());
    return stackTraceJsonProvider;
  }

  private static ShortenedThrowableConverter throwableConverter() {
    ShortenedThrowableConverter throwableConverter = new ShortenedThrowableConverter();
    throwableConverter.setRootCauseFirst(true);
    return throwableConverter;
  }

  private static LoggingEventFormattedTimestampJsonProvider timestampJsonProvider() {
    LoggingEventFormattedTimestampJsonProvider timestampJsonProvider = new LoggingEventFormattedTimestampJsonProvider();
    timestampJsonProvider.setTimeZone("UTC");
    timestampJsonProvider.setFieldName("timestamp");
    return timestampJsonProvider;
  }

  private static class LogbackLoggerContextListener extends ContextAwareBase implements LoggerContextListener {
    private final ApplicationProperties.Logging loggingProperties;
    private final String customFields;

    private LogbackLoggerContextListener(ApplicationProperties.Logging loggingProperties, String customFields) {
      this.loggingProperties = loggingProperties;
      this.customFields = customFields;
    }

    public boolean isResetResistant() {
      return true;
    }

    public void onStart(LoggerContext context) {
      if (this.loggingProperties.isUseJsonFormat()) {
        LoggingUtils.addJsonConsoleAppender(context, this.customFields);
      }

      if (this.loggingProperties.getLogstash().isEnabled()) {
        LoggingUtils.addLogstashTcpSocketAppender(context, this.customFields, this.loggingProperties.getLogstash());
      }

    }

    public void onReset(LoggerContext context) {
      if (this.loggingProperties.isUseJsonFormat()) {
        LoggingUtils.addJsonConsoleAppender(context, this.customFields);
      }

      if (this.loggingProperties.getLogstash().isEnabled()) {
        LoggingUtils.addLogstashTcpSocketAppender(context, this.customFields, this.loggingProperties.getLogstash());
      }

    }

    public void onStop(LoggerContext context) {
    }

    public void onLevelChange(ch.qos.logback.classic.Logger logger, Level level) {
    }
  }
}
