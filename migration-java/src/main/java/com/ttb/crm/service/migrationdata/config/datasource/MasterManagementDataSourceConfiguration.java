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
        basePackages = "com.ttb.crm.service.migrationdata.repository.masterManagement",
        entityManagerFactoryRef = "masterManagementEntityManagerFactory",
        transactionManagerRef = "masterManagementTransactionManager"
)
@EntityScan(basePackages = "com.ttb.crm.batch.migrationdata.model.masterManagement")
public class MasterManagementDataSourceConfiguration {

    @Bean
    @ConfigurationProperties("spring.datasource.master-management")
    public DataSourceProperties masterManagementDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "masterManagementDataSource")
    @ConfigurationProperties("spring.datasource.master-management.hikari")
    public DataSource masterManagementDataSource(
            @Qualifier("masterManagementDataSourceProperties") DataSourceProperties properties
    ) {
        return properties
                .initializeDataSourceBuilder()
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .build();
    }

    @Bean(name = "masterManagementEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean masterManagementEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("masterManagementDataSource") DataSource dataSource
    ) {
        return builder
                .dataSource(dataSource)
                .packages("com.ttb.crm.service.migrationdata.model.masterManagement")
                .persistenceUnit("masterManagementPU")
                .build();
    }

    @Bean(name = "masterManagementTransactionManager")
    public PlatformTransactionManager masterManagementTransactionManager(
            @Qualifier("masterManagementEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory
    ) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
    }
}
