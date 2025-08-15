package com.ttb.crm.service.migrationdata.config.datasource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.ttb.crm.service.migrationdata.repository.batch",
        entityManagerFactoryRef = "batchEntityManagerFactory",
        transactionManagerRef = "batchTransactionManager"
)
@EntityScan(basePackages = "com.ttb.crm.service.migrationdata.model.batch")
public class BatchDataSourceConfiguration {
    @Bean
    @ConfigurationProperties("spring.datasource.batch")
    public DataSourceProperties batchDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "batchDataSource")
    @ConfigurationProperties("spring.datasource.batch.hikari")
    public DataSource batchDataSource(
            @Qualifier("batchDataSourceProperties") DataSourceProperties properties
    ) {
        return properties
                .initializeDataSourceBuilder()
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .build();
    }

    @Bean(name = "batchEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean batchEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("batchDataSource") DataSource dataSource
    ) {
        return builder
                .dataSource(dataSource)
                .packages("com.ttb.crm.service.migrationdata.model.batch")
                .persistenceUnit("batchPU")
                .build();
    }
    @Bean(name = "batchJpaTransactionManager")
    public PlatformTransactionManager batchJpaTransactionManager(
            @Qualifier("batchEntityManagerFactory") LocalContainerEntityManagerFactoryBean factory
    ) {
        return new JpaTransactionManager(factory.getObject());
    }
}

