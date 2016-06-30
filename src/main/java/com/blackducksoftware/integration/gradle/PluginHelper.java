/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.gradle;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Project;

import com.blackducksoftware.integration.build.bdio.Constants;

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

	public File getBdioFile(final File parentDirectory, final String artifactId) {
		return new File(parentDirectory, artifactId + Constants.BDIO_FILE_SUFFIX);
	}

	public File getBlackDuckDirectory() {
		return blackDuckDirectory;
	}

}
