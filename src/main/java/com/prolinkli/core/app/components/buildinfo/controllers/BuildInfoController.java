package com.prolinkli.core.app.components.buildinfo.controllers;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * BuildInfoController
 */
@RestController
@RequestMapping(path = "buildinfo")
public class BuildInfoController {

	@GetMapping("/")
	public Map<String, String> getBuildInfo() {
		return Map.of("name", "Prolinkli", "version", "1.0.0", "buildDate", "2025-06-06");
	}

}
