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
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.ttb.crm.service.migrationdata.repository.userManagement",
        entityManagerFactoryRef = "userManagementEntityManagerFactory",
        transactionManagerRef = "userManagementTransactionManager"
)
@EntityScan(basePackages = "com.ttb.crm.batch.migrationdata.model.userManagement")
public class UserManagementDataSourceConfiguration {

    @Bean
    @ConfigurationProperties("spring.datasource.user-management")
    public DataSourceProperties userManagementDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "userManagementDataSource")
    @ConfigurationProperties("spring.datasource.user-management.hikari")
    public DataSource userManagementDataSource(
            @Qualifier("userManagementDataSourceProperties") DataSourceProperties properties
    ) {
        return properties
                .initializeDataSourceBuilder()
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .build();
    }

    @Bean(name = "userManagementEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean userManagementEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("userManagementDataSource") DataSource dataSource
    ) {
        return builder
                .dataSource(dataSource)
                .packages("com.ttb.crm.service.migrationdata.model.userManagement")
                .persistenceUnit("userManagementPU")
                .build();
    }

    @Bean(name = "userManagementTransactionManager")
    public PlatformTransactionManager userManagementTransactionManager(
            @Qualifier("userManagementEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory
    ) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
    }
}
