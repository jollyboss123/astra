package com.jolly.astra.config;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.testcontainers.containers.JdbcDatabaseContainer;

/**
 * @author jolly
 */
public interface SqlTestContainer extends InitializingBean, DisposableBean {
  JdbcDatabaseContainer<?> getTestContainer();
}
