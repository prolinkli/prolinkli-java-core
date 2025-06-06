package com.prolinkli.core.app.components.buildinfo.controller;

import java.util.Map;

import com.prolinkli.core.app.components.buildinfo.model.BuildInfo;
import com.prolinkli.core.app.components.buildinfo.service.BuildInfoGetService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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

	@GetMapping("")
	public BuildInfo getBuildInfo() {
		return buildInfoGetService.getBuildInfo();
	}

}
