package com.prolinkli.framework.db.dao;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.prolinkli.framework.db.base.DbModel;

/**
 * Factory for creating DAO instances.
 * This factory can dynamically create DAOs for any entity type by looking up
 * the corresponding mapper in the Spring application context.
 */
@Component
public class DaoFactory {

	@Autowired
	private ApplicationContext applicationContext;

	private static final Logger LOGGER = LoggerFactory.getLogger(DaoFactory.class);

	// Cache for created DAOs to avoid recreating them
	private final Map<String, Dao<?, ?>> daoCache = new ConcurrentHashMap<>();

	/**
	 * Creates or retrieves a DAO for the specified entity type.
	 * 
	 * @param <T>             the entity type
	 * @param <PK>            the primary key type
	 * @param entityClass     the entity class
	 * @param primaryKeyClass the primary key class
	 * @param mapperClass     the mapper interface class
	 * @return a DAO instance for the entity type
	 */
	@SuppressWarnings("unchecked")
	public <T extends DbModel, PK> Dao<T, PK> getDao(Class<T> entityClass, Class<PK> primaryKeyClass,
			Class<?> mapperClass) {
		String key = entityClass.getName() + "_" + mapperClass.getName();

		return (Dao<T, PK>) daoCache.computeIfAbsent(key, k -> {
			try {
				// Get the mapper bean from Spring context
				Object mapper = applicationContext.getBean(mapperClass);
				return new Dao<>(mapper, entityClass, mapperClass, primaryKeyClass);
			} catch (Exception e) {
				throw new RuntimeException("Failed to create DAO for " + entityClass.getSimpleName(), e);
			}
		});
	}

	/**
	 * Creates or retrieves a DAO for the specified entity type using naming
	 * conventions.
	 * This assumes the mapper follows the pattern: EntityNameMapper
	 * 
	 * @param <T>             the entity type
	 * @param <PK>            the primary key type
	 * @param entityClass     the entity class
	 * @param primaryKeyClass the primary key class
	 * @return a DAO instance for the entity type
	 */
	public <T extends DbModel, PK> Dao<T, PK> getDao(Class<T> entityClass, Class<PK> primaryKeyClass) {
		try {
			// Construct mapper class name based on entity name
			String mapperClassName = entityClass.getName().replace(".model.", ".mapper.") + "Mapper";
			LOGGER.debug("Looking for mapper class: {}", mapperClassName);
			Class<?> mapperClass = Class.forName(mapperClassName);
			return getDao(entityClass, primaryKeyClass, mapperClass);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Mapper class not found for " + entityClass.getSimpleName(), e);
		}
	}

}
