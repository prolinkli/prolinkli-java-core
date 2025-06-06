package com.prolinkli.core.app.components.buildinfo.service;

import java.time.LocalDate;

import com.prolinkli.core.app.components.buildinfo.model.BuildInfo;

import org.springframework.stereotype.Service;

/**
 * BuildInfoGetService
 */
@Service
public class BuildInfoGetService {

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
	 * @return a {@link com.prolinkli.core.app.components.buildinfo.model.BuildInfo}
	 *         instance
	 *         containing build name, version, and date
	 */
	public BuildInfo getBuildInfo() {
		BuildInfo buildInfo = new BuildInfo();
		buildInfo.setBuildName("Mock Build Info Object");
		buildInfo.setBuildVersion("1.4.1");
		buildInfo.setBuildDate(LocalDate.now());
		return buildInfo;
	}

}
