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

  private final Class<T> entityType;
  private final Class<PK> primaryKeyType;
  private final Object mapper;

  /**
   * Constructs a new Dao instance.
   *
   * @param sqlSessionFactory the SqlSessionFactory to use for database operations
   * @param entityType        the class type of the entity this DAO manages
   * @param mapperClass       the MyBatis mapper interface class
   */
  public Dao(Object mapper, Class<T> entityType, Class<?> mapperClass, Class<PK> primaryKeyType) {
    this.mapper = mapper;
    this.entityType = entityType;
    this.primaryKeyType = primaryKeyType;
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
  public <R extends DbExample<T>> int delete(R example) throws PersistenceException {

    Method deleteMethod = getMapperMethod(MethodNames.DELETE_BY_EXAMPLE_METHOD, example.getClass());
    if (deleteMethod == null) {
      LOGGER.error("Mapper method not found: {}", MethodNames.DELETE_BY_EXAMPLE_METHOD);
      return 0; // Handle method not found appropriately
    }

    Integer result = invokeMethod(deleteMethod, example);
    if (result == null) {
      LOGGER.error("Delete method returned null for example: {}", example);
      return 0; // Handle null result appropriately
    }

    if (result instanceof Integer) {
      return result.intValue();
    } else {
      LOGGER.error("Delete method did not return an Integer: {}", result);
      return 0; // Handle unexpected return type
    }

  }

  @Override
  public int delete(PK id) throws PersistenceException {
    Method deleteMethod = getMapperMethod(MethodNames.DELETE_BY_PRIMARY_KEY_METHOD, primaryKeyType);
    if (deleteMethod == null) {
      LOGGER.error("Mapper method not found: {}", MethodNames.DELETE_BY_PRIMARY_KEY_METHOD);
      return 0; // Handle method not found appropriately
    }

    Integer result = invokeMethod(deleteMethod, id);
    if (result == null) {
      LOGGER.error("Delete method returned null for id: {}", id);
      return 0; // Handle null result appropriately
    }

    if (result instanceof Integer) {
      return result.intValue();
    } else {
      LOGGER.error("Delete method did not return an Integer: {}", result);
      return 0; // Handle unexpected return type
    }

  }

  @Override
  public int insert(T entity) throws PersistenceException {

    Method insertMethod = getMapperMethod(MethodNames.INSERT_METHOD, entity.getClass());
    if (insertMethod == null) {
      LOGGER.error("Mapper method not found: {}", MethodNames.INSERT_METHOD);
      throw new PersistenceException("Insert method not found for entity: " + entity.getClass().getName());
    }

    Integer result = invokeMethod(insertMethod, entity);
    if (result == null) {
      LOGGER.error("Insert method returned null for entity: {}", entity);
      throw new PersistenceException("Insert method returned null for entity: " + entity);
    }

    return result.intValue();
  }

  @Override
  public int insert(List<T> entities) throws PersistenceException {

    if (entities == null || entities.isEmpty()) {
      return 0; // Handle empty list appropriately
    }

    return (int) entities.stream().reduce(0, (count, entity) -> {
      try {
        return count + insert(entity);
      } catch (PersistenceException e) {
        LOGGER.error("Error inserting entity: {}", entity, e);
        throw new PersistenceException("Error inserting entity: " + entity, e);
      }
    }, Integer::sum);
  }

  @Override
  @SuppressWarnings("unchecked")
  public T select(PK id) throws PersistenceException {

    Method selectMethod = getMapperMethod(MethodNames.SELECT_BY_PRIMARY_KEY_METHOD, primaryKeyType);

    if (selectMethod == null) {
      LOGGER.error("Mapper method not found: {}", MethodNames.SELECT_BY_PRIMARY_KEY_METHOD);
      return null; // Handle method not found appropriately
    }

    Object result = invokeMethod(selectMethod, id);
    if (result == null) {
      LOGGER.warn("Select method returned null for id: {}", id);
      return null; // Handle null result appropriately
    }

    if (result instanceof DbModel) {
      return (T) result; // Cast to the entity type
    } else {
      LOGGER.error("Select method did not return an instance of DbModel: {}", result);
      return null; // Handle unexpected return type
    }

  }

  @Override
  public <R extends DbExample<T>> List<T> select(R example) throws PersistenceException {
    Method selectMethod = getMapperMethod(MethodNames.SELECT_BY_EXAMPLE_METHOD, example.getClass());

    if (selectMethod == null) {
      return null; // Method not found, return null or handle as needed
    }

    return invokeMethod(selectMethod, example);

  }

  public <R extends DbExample<T>> int update(T entity, R example) throws PersistenceException {
    Method updateMethod = getMapperMethod(MethodNames.UPDATE_BY_EXAMPLE_METHOD, entity.getClass(), example.getClass());
    if (updateMethod == null) {
      LOGGER.error("Mapper method not found: {}", MethodNames.UPDATE_BY_EXAMPLE_METHOD);
      return 0; // Handle method not found appropriately
    }
    Integer result = invokeMethod(updateMethod, entity, example);
    if (result == null) {
      LOGGER.error("Update method returned null for entity: {}, example: {}", entity, example);
      return 0; // Handle null result appropriately
    }
    if (result instanceof Integer) {
      return result.intValue();
    } else {
      LOGGER.error("Update method did not return an Integer: {}", result);
      return 0; // Handle unexpected return type
    }
  }

  @Override
  public int update(T entity) throws PersistenceException {
    Method updateMethod = getMapperMethod(MethodNames.UPDATE_BY_PRIMARY_KEY_METHOD, entity.getClass());
    if (updateMethod == null) {
      LOGGER.error("Mapper method not found: {}", MethodNames.UPDATE_BY_PRIMARY_KEY_METHOD);
      return 0; // Handle method not found appropriately
    }
    Integer result = invokeMethod(updateMethod, entity);
    if (result == null) {
      LOGGER.error("Update method returned null for entity: {}", entity);
      return 0; // Handle null result appropriately
    }
    if (result instanceof Integer) {
      return result.intValue();
    } else {
      LOGGER.error("Update method did not return an Integer: {}", result);
      return 0; // Handle unexpected return type
    }
  }

  @Override
  public int update(List<T> entities) throws PersistenceException {
    return (entities != null) ? entities.stream().reduce(0, (count, entity) -> {
      try {
        return count + update(entity);
      } catch (PersistenceException e) {
        LOGGER.error("Error updating entity: {}", entity, e);
        return count; // Return the current count if update fails
      }
    }, Integer::sum) : 0;
  }

  @SuppressWarnings("unchecked")
  private <Z> Z invokeMethod(Method method, Object... args) {
    try {
      Object result = method.invoke(mapper, args);
      if (result == null) {
        return null; // Handle null result appropriately
      }
      return (Z) result;
    } catch (IllegalAccessException e) {
      LOGGER.error("Error invoking method: {}", method.getName(), e);
      return null;
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      LOGGER.error("Internal Error invoking method: {}", method.getName(), cause);
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
