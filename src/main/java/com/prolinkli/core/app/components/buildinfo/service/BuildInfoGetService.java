package com.prolinkli.core.app.components.buildinfo.service;

import java.time.LocalDate;
import java.time.ZoneId;

import com.prolinkli.core.app.components.buildinfo.model.BuildInfo;
import com.prolinkli.core.app.db.mapper.generated.BuildInfoDbMapper;
import com.prolinkli.core.app.db.model.generated.BuildInfoDb;
import com.prolinkli.core.app.db.model.generated.BuildInfoDbExample;
import com.prolinkli.core.app.db.model.generated.BuildInfoDbKey;
import com.prolinkli.framework.db.dao.Dao;
import com.prolinkli.framework.db.dao.DaoFactory;
import com.prolinkli.framework.util.LocalDateUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * BuildInfoGetService
 */
@Service
public class BuildInfoGetService {

	private final Dao<BuildInfoDb, BuildInfoDbKey> dao;

	@Autowired
	public BuildInfoGetService(
			DaoFactory daoFactory // Injecting the DAO factory to get the DAO instance
	) {
		this.dao = daoFactory.getDao(BuildInfoDb.class, BuildInfoDbKey.class);
	}

	/**
	 * Constructs and returns the current build information.
	 * <p>
	 * This method creates a
	 * {@link com.prolinkli.core.app.components.buildinfo.model.BuildInfo}
	 * object populated with the application's build metadata. The data may be
	 * sourced
	 * from static configuration, environment variables, or database queries via
	 * MyBatis
	 * or DAO frameworks.
	 * </p>
	 * <p>
	 * Example use case: Called by a REST controller to provide build info to API
	 * consumers.
	 * </p>
	 *
	 * instance
	 * 
	 * @return a {@link com.prolinkli.core.app.components.buildinfo.model.BuildInfo}
	 *         containing build name, version, and date
	 */
	public BuildInfo getBuildInfo() {

		BuildInfoDbExample example = new BuildInfoDbExample();
		example.setOrderByClause("build_date DESC");
		example.createCriteria()
				.andCreatedAtIsNotNull()
				.andBuildDateIsNotNull();

		BuildInfoDb db = dao.select(example).stream()
				.findFirst()
				.orElseThrow(() -> new RuntimeException("No build info found in the database"));

		BuildInfo buildInfo = new BuildInfo();
		buildInfo.setBuildName(db.getEnvironment());
		buildInfo.setBuildVersion(db.getVersion());
		buildInfo.setCommitHash(db.getCommitHash());
		buildInfo.setBuildDate(LocalDateUtil.toLocalDate(db.getBuildDate()));

		return buildInfo;
	}

}
