package ru.fogstream.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@EnableTransactionManagement
@PropertySource("classpath:application.properties")
public class DataSourceConfig {

    @Bean(name = "dataSource")
    public DataSource dataSource(@Value("${spring.datasource.driver}") String dataSourceDriver,
                                 @Value("${spring.datasource.url}") String dataSourceUrl,
                                 @Value("${spring.datasource.username}") String dataSourceUser,
                                 @Value("${spring.datasource.password}") String dataSourcePassword) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(dataSourceDriver);
        dataSource.setUrl(dataSourceUrl);
        dataSource.setUsername(dataSourceUser);
        dataSource.setPassword(dataSourcePassword);
        return dataSource;
    }

    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(@Qualifier("dataSource") DataSource dataSource,
                                                                       @Value("${hibernate.max_fetch_depth}") int maxFetchDepth,
                                                                       @Value("${hibernate.jdbc.fetch_size}") int fetchSize,
                                                                       @Value("${hibernate.jdbc.batch_size}") int batchSize,
                                                                       @Value("${hibernate.show_sql}") boolean showSql,
                                                                       @Value("${hibernate.hbm2ddl.auto}") String tableStrategy) {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factoryBean.setPackagesToScan("ru.fogstream.entity");
        Properties properties = new Properties();
        properties.put("hibernate.max_fetch_depth", maxFetchDepth);
        properties.put("hibernate.jdbc.fetch_size", fetchSize);
        properties.put("hibernate.jdbc.batch_size", batchSize);
        properties.put("hibernate.show_sql", showSql);
        properties.put("hibernate.hbm2ddl.auto", tableStrategy);
        factoryBean.setJpaProperties(properties);
        return factoryBean;
    }

    @Bean(name = "transactionManager")
    public JpaTransactionManager transactionManager(@Qualifier("entityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }
}
