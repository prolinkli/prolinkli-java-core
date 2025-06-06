package com.prolinkli.framework.db.dao;

import java.util.List;

import com.prolinkli.framework.db.base.DbModel;

import org.apache.ibatis.exceptions.PersistenceException;

/**
 * IParentDao
 */
public interface IParentDao<T extends DbModel, PK> {

	public T selectById(PK id) throws PersistenceException;

	public <R> T select(R example) throws PersistenceException;

	public int insert(T entity) throws PersistenceException;

	public int insert(List<T> entities) throws PersistenceException;

	public int update(T entity) throws PersistenceException;

	public int update(List<T> entities) throws PersistenceException;

	public int deleteById(PK id) throws PersistenceException;

	public int delete(T entity) throws PersistenceException;

	public int delete(List<T> entities) throws PersistenceException;

}
