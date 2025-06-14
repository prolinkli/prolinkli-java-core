package com.prolinkli.framework.config.mybatis;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis configuration for Spring integration.
 * This configuration automatically scans for mapper interfaces and creates
 * beans for them.
 */
@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = "com.prolinkli.core.app.db.mapper.generated")
public class MyBatisConfig {

	/**
	 * Creates the SqlSessionFactory bean.
	 * This is the core MyBatis component that creates SqlSession instances.
	 */
	@Bean
	public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
		SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
		factoryBean.setDataSource(dataSource);

		// Set mapper XML locations if you have XML mappers
		factoryBean.setMapperLocations(
				new PathMatchingResourcePatternResolver().getResources(
						"classpath*:mapper/**/*.xml"));

		// Set type aliases package for your model classes
		factoryBean.setTypeAliasesPackage("com.prolinkli.core.app.db.model.generated");

		return factoryBean.getObject();
	}

	/**
	 * Creates the transaction manager for database operations.
	 */
	@Bean
	public PlatformTransactionManager transactionManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}
}
