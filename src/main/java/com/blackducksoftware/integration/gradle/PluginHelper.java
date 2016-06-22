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

import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Project;

public class PluginHelper {
	public static final String BUILD_ID_PROPERTY = "BuildId";
	public static final String INCLUDED_CONFIGURATIONS_PROPERTY = "IncludedConfigurations";
	public static final String BASE_OUTPUT_DIRECTORY_PROPERTY = "BlackDuckOutputDirectory";
	public static final String DEPENDENCY_REPORT_OUTPUT = "DepedencyReportOutput";

	private final File blackDuckDirectory;

	public PluginHelper(final Project project) {
		File file;
		final String baseDirectory = System.getProperty(PluginHelper.BASE_OUTPUT_DIRECTORY_PROPERTY);
		if (StringUtils.isNotBlank(baseDirectory)) {
			file = new File(baseDirectory);
		} else if (project.getRootProject() != null) {
			final Project rootProject = project.getRootProject();
			file = rootProject.getProjectDir();
		} else {
			file = project.getProjectDir();
		}

		blackDuckDirectory = new File(file, "BlackDuck");
		blackDuckDirectory.mkdirs();
	}

	public String getGAV(final String group, final String artifact, final String version) {
		final StringBuilder gavBuilder = new StringBuilder();
		gavBuilder.append(group);
		gavBuilder.append(":");
		gavBuilder.append(artifact);
		gavBuilder.append(":");
		gavBuilder.append(version);
		return gavBuilder.toString();
	}

	public File getBlackDuckDirectory() {
		return blackDuckDirectory;
	}

}
