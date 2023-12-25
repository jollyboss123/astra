package com.jolly.astra.config;

import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.AsyncTaskExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @author jolly
 */
public class ExceptionHandlingAsyncTaskExecutor implements AsyncTaskExecutor, InitializingBean, DisposableBean {
  private static final String EXCEPTION_MESSAGE = "caught async exception";
  private final Logger log = LoggerFactory.getLogger(ExceptionHandlingAsyncTaskExecutor.class);
  private final AsyncTaskExecutor executor;

  public ExceptionHandlingAsyncTaskExecutor(AsyncTaskExecutor executor) {
    this.executor = executor;
  }

  @Override
  public void destroy() throws Exception {
    if (this.executor instanceof DisposableBean bean) {
      bean.destroy();
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (this.executor instanceof InitializingBean bean) {
      bean.afterPropertiesSet();
    }
  }

  @Override
  public void execute(@Nonnull Runnable task) {
    this.executor.execute(createWrappedRunnable(task));
  }

  @Override
  public Future<?> submit(@Nonnull Runnable task) {
    return this.executor.submit(createWrappedRunnable(task));
  }

  @Override
  public <T> Future<T> submit(@Nonnull Callable<T> task) {
    return this.executor.submit(createCallable(task));
  }

  private Runnable createWrappedRunnable(Runnable task) {
    return () -> {
      try {
        task.run();
      } catch (Exception ex) {
        log.error(EXCEPTION_MESSAGE, ex);
      }
    };
  }

  private <T> Callable<T> createCallable(Callable<T> task) {
    return () -> {
      try {
        return task.call();
      } catch (Exception ex) {
        log.error(EXCEPTION_MESSAGE, ex);
        throw ex;
      }
    };
  }
}
