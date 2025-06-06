package com.prolinkli.core.app.components.buildinfo.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * BuildInfoController
 */
@RestController()
@RequestMapping("buildinfo")
public class BuildInfoController {

	@RequestMapping("info")
	public String getBuildInfo() {
		return "Prolinkli Build Info: Version 1.0.0, Build Date: 2025-06-05";
	}

}
