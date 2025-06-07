package com.prolinkli.core.app.components.buildinfo.model;

import java.time.LocalDate;

import lombok.Data;

/**
 * Represents build information for the application including build name,
 * version, and date.
 * 
 * <p>
 * This class encapsulates metadata about a specific build of the application,
 * providing essential information for tracking and identifying different builds
 * in development, testing, and production environments.
 * </p>
 * 
 * @author Kevin Erdogan
 * @since 1.0.0
 * @version 1.0.0
 */
@Data
public class BuildInfo {

	/**
	 * The name of the build.
	 * 
	 * <p>
	 * This typically represents a descriptive name or identifier
	 * for the build, which may include branch names, feature identifiers,
	 * or other meaningful build designations.
	 * </p>
	 */
	private String buildName;

	/**
	 * The version number of the build.
	 * 
	 * <p>
	 * This field contains the semantic version number following
	 * standard versioning conventions (e.g., "1.2.3" or "2.1.0-SNAPSHOT").
	 * The version helps track the progression and compatibility of builds.
	 * </p>
	 */
	private String buildVersion;

	/**
	 * The commit hash associated with the build.
	 * 
	 * <p>
	 * This is a unique identifier for the specific commit in the
	 * version control system (e.g., Git) that corresponds to this build.
	 * It allows developers to trace back to the exact code state that was
	 * used to create the build.
	 * </p>
	 */
	private String commitHash;

	/**
	 * The date when the build was created.
	 * 
	 * <p>
	 * This timestamp provides information about when the build
	 * was generated, which is useful for tracking build history,
	 * debugging issues, and managing deployments.
	 * </p>
	 */
	private LocalDate buildDate;

}
