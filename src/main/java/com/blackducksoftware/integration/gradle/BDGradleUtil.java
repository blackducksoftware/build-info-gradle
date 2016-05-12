/*******************************************************************************
 * Black Duck Software Suite SDK
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *******************************************************************************/
package com.blackducksoftware.integration.gradle;

import java.io.File;

import org.gradle.api.Project;

public class BDGradleUtil {
	public static final String BUILD_ID_PROPERTY = "BuildId";

	public static final String INCLUDED_CONFIGURATIONS_PROPERTY = "IncludedConfigurations";

	public static final String DEPENDENCY_REPORT_OUTPUT = "DepedencyReportOutput";

	public String getGAV(final String group, final String artifact, final String version) {
		final StringBuilder gavBuilder = new StringBuilder();
		gavBuilder.append(group);
		gavBuilder.append(":");
		gavBuilder.append(artifact);
		gavBuilder.append(":");
		gavBuilder.append(version);
		return gavBuilder.toString();
	}

	public File findBuildDir(final Project project) {
		File buildDir;

		if (project.getRootProject() != null) {
			final Project rootProject = project.getRootProject();
			buildDir = rootProject.getBuildDir();
		} else {
			buildDir = project.getBuildDir();
		}

		return buildDir;
	}

}
