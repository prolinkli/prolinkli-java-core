package com.prolinkli.framework.db.dao;

import java.util.List;

import com.prolinkli.framework.db.base.DbExample;
import com.prolinkli.framework.db.base.DbModel;

import org.apache.ibatis.exceptions.PersistenceException;

/**
 * IParentDao
 */
public interface IParentDao<T extends DbModel, PK> {

	public T select(PK id) throws PersistenceException;

	public <R extends DbExample<T>> List<T> select(R example) throws PersistenceException;

	public int insert(T entity) throws PersistenceException;

	public int insert(List<T> entities) throws PersistenceException;

	public <R extends DbExample<T>> int update(T entity, R example) throws PersistenceException;

	public int update(T entity) throws PersistenceException;

	public int update(List<T> entities) throws PersistenceException;

	public <R extends DbExample<T>> int delete(R example) throws PersistenceException;

	public int delete(PK id) throws PersistenceException;

}
