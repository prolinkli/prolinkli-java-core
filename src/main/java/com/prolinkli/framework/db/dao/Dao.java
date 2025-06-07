package com.prolinkli.framework.db.dao;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.prolinkli.framework.db.base.DbExample;
import com.prolinkli.framework.db.base.DbModel;

import org.apache.ibatis.exceptions.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enhanced Generic DAO that integrates with MyBatis generated mappers and
 * supports criteria queries.
 * 
 * <p>
 * This DAO automatically maps to the generated MyBatis mapper classes and
 * provides
 * a generic interface for CRUD operations using both primary keys and
 * criteria-based queries.
 * </p>
 * 
 * @param <T>  the entity type extending DbModel
 * @param <PK> the primary key type
 * 
 * @author Kevin Erdogan
 * @since 1.0.0
 * @version 1.0.0
 */
public class Dao<T extends DbModel, PK> implements IParentDao<T, PK> {

	private static final Logger LOGGER = LoggerFactory.getLogger(Dao.class);

	private final static class MethodNames {
		public static final String SELECT_BY_PRIMARY_KEY_METHOD = "selectByPrimaryKey";
		public static final String SELECT_BY_EXAMPLE_METHOD = "selectByExample";
		public static final String INSERT_METHOD = "insert";
		public static final String UPDATE_METHOD = "update";
		public static final String UPDATE_BY_EXAMPLE_METHOD = "updateByExample";
		public static final String UPDATE_BY_PRIMARY_KEY_METHOD = "updateByPrimaryKey";
		public static final String DELETE_BY_EXAMPLE_METHOD = "deleteByExample";
		public static final String DELETE_BY_PRIMARY_KEY_METHOD = "deleteByPrimaryKey";
	}

	private final static class MethodReturnTypes {
		public static final Class<?> SELECT_BY_EXAMPLE_RETURN_TYPE = List.class;
		public static final Class<?> INSERT_RETURN_TYPE = Integer.class;
		public static final Class<?> UPDATE_RETURN_TYPE = Integer.class;
		public static final Class<?> DELETE_RETURN_TYPE = Integer.class;
	}

	private final Class<T> entityType;
	private final String mapperNamespace;
	private final Object mapper;

	/**
	 * Constructs a new Dao instance.
	 *
	 * @param sqlSessionFactory the SqlSessionFactory to use for database operations
	 * @param entityType        the class type of the entity this DAO manages
	 * @param mapperClass       the MyBatis mapper interface class
	 */
	public Dao(Object mapper, Class<T> entityType, Class<?> mapperClass) {
		this.mapper = mapper;
		this.mapperNamespace = mapperClass.getPackageName() + "." + mapperClass.getSimpleName();
		this.entityType = entityType;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T selectById(PK id) throws PersistenceException {

		return null;
	}

	/**
	 * Gets the entity type managed by this DAO.
	 * 
	 * @return the entity class
	 */
	public Class<T> getEntityType() {
		return entityType;
	}

	@Override
	public int delete(T entity) throws PersistenceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int delete(List<T> entities) throws PersistenceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int deleteById(PK id) throws PersistenceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int insert(T entity) throws PersistenceException {

		Method insertMethod = getMapperMethod(MethodNames.INSERT_METHOD, entity.getClass());
		if (insertMethod == null) {
			LOGGER.error("Mapper method not found: {}", MethodNames.INSERT_METHOD);
			return 0;
		}

		Integer result = invokeMethod(insertMethod, entity);
		if (result == null) {
			LOGGER.error("Insert method returned null for entity: {}", entity);
			return 0; // Handle null result appropriately
		}

		return result.intValue();
	}

	@Override
	public int insert(List<T> entities) throws PersistenceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <R extends DbExample<T>> List<T> select(R example) throws PersistenceException {
		Method selectMethod = getMapperMethod(MethodNames.SELECT_BY_EXAMPLE_METHOD, example.getClass());

		if (selectMethod == null) {
			return null; // Method not found, return null or handle as needed
		}

		return invokeMethod(selectMethod, example);

	}

	@Override
	public int update(T entity) throws PersistenceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(List<T> entities) throws PersistenceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@SuppressWarnings("unchecked")
	private <Z> Z invokeMethod(Method method, Object... args) {
		try {
			Object result = method.invoke(mapper, args);
			if (result == null) {
				return null; // Handle null result appropriately
			}
			return (Z) result;
		} catch (IllegalAccessException | InvocationTargetException e) {
			LOGGER.error("Error invoking method: {}", method.getName(), e);
			return null;
		}
	}

	private Method getMapperMethod(String methodName, Class<?>... parameterTypes) {
		try {
			return mapper.getClass().getMethod(methodName, parameterTypes);
		} catch (Exception e) {
			LOGGER.error("Mapper method not found: {} with parameters {}", methodName, parameterTypes, e);
			return null;
		}
	}

}
