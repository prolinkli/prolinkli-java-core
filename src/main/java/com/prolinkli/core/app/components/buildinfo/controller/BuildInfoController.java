package com.prolinkli.core.app.components.buildinfo.controller;

import com.prolinkli.core.app.components.buildinfo.model.BuildInfo;
import com.prolinkli.core.app.components.buildinfo.service.BuildInfoGetService;
import com.prolinkli.core.app.components.buildinfo.service.BuildInfoSetService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * BuildInfoController
 */
@RestController
@RequestMapping(path = "buildinfo")
public class BuildInfoController {

	// Injecting the BuildInfoGetService to retrieve build information
	@Autowired
	private BuildInfoGetService buildInfoGetService;

	@Autowired
	private BuildInfoSetService buildInfoSetService;

	@GetMapping("")
	@PreAuthorize("isAuthenticated()")
	public BuildInfo getBuildInfo() {
		return buildInfoGetService.getBuildInfo();
	}

	@PostMapping("")
	public Boolean insertCurrentBuildInfo() {
		try {
			buildInfoSetService.insertCurrentBuildInfo();
			return true;
		} catch (Exception e) {
			// Log the exception (not shown here for brevity)
			return false;
		}
	}

}
