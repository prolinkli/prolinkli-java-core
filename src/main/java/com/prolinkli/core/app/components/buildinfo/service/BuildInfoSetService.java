package com.prolinkli.core.app.components.buildinfo.service;

import java.time.LocalDate;

import com.prolinkli.core.app.db.mapper.generated.BuildInfoDbMapper;
import com.prolinkli.core.app.db.model.generated.BuildInfoDb;
import com.prolinkli.framework.db.dao.Dao;
import com.prolinkli.framework.db.dao.DaoFactory;
import com.prolinkli.framework.util.LocalDateUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * BuildInfoSetService
 */
@Service
public class BuildInfoSetService {

	private final Dao<BuildInfoDb, BuildInfoDbMapper> dao;

	@Autowired
	public BuildInfoSetService(
			DaoFactory daoFactory) {
		this.dao = daoFactory.getDao(BuildInfoDb.class, BuildInfoDbMapper.class);
	}

	/**
	 * Sets the build information in the database.
	 * <p>
	 * This method updates or inserts the build information into the database using
	 * the provided {@link BuildInfoDb} object.
	 * </p>
	 *
	 * @param buildInfoDb the build information to set
	 */
	public void insertCurrentBuildInfo() {
		// TODO: add logic to retrieve the current build information from the
		// environment

		try {
		} catch (Exception e) {
			throw new RuntimeException("Failed to insert or update build info", e);
		}
	}

}
