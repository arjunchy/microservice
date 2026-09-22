package com.employee.EmployeeService.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.employee.EmployeeService.repository",
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.REGEX,
                pattern = "com\\.employee\\.EmployeeService\\.repository\\.read\\..*"),
        entityManagerFactoryRef = "employeeEntityManagerFactory",
        transactionManagerRef = "employeeTransactionManager"
)
public class EmployeeDataSourceConfig {

    @Primary
    @Bean
    @ConfigurationProperties(prefix = "app.datasource.employee")
    public DataSource employeeDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean employeeEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("employeeDataSource") DataSource dataSource) {
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.show_sql", "true");
        return builder
                .dataSource(dataSource)
                .packages("com.employee.EmployeeService.model.entity")
                .persistenceUnit("employeePU")
                .properties(props)
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager employeeTransactionManager(@Qualifier("employeeEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}