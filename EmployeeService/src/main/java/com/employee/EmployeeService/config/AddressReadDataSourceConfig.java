package com.employee.EmployeeService.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
public class AddressReadDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "app.datasource.address")
    public DataSource addressDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean addressEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("addressDataSource") DataSource dataSource) {
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "validate");
        props.put("hibernate.show_sql", "true");
        return builder
                .dataSource(dataSource)
                .packages("com.employee.EmployeeService.model.read")
                .persistenceUnit("addressPU")
                .properties(props)
                .build();
    }

    @Bean
    public PlatformTransactionManager addressTransactionManager(@Qualifier("addressEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}