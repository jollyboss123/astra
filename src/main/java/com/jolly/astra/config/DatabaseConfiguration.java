package com.jolly.astra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author jolly
 */
@Configuration
@EnableJpaRepositories({"com.jolly.astra.repository"})
@EnableTransactionManagement
public class DatabaseConfiguration {
}
