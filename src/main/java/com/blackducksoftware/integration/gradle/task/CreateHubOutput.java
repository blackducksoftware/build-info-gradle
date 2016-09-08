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
package com.blackducksoftware.integration.gradle.task;

import java.io.IOException;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import com.blackducksoftware.integration.gradle.BdioHelper;
import com.blackducksoftware.integration.gradle.DependencyGatherer;

public class CreateHubOutput extends DefaultTask {
	private BdioHelper bdioHelper;
	private String hubProjectName;
	private String hubProjectVersion;

	@TaskAction
	public void gatherDependencies() throws IOException {
		bdioHelper.ensureReportsDirectoryExists();
		final Project project = getProject();

		final DependencyGatherer dependencyGatherer = new DependencyGatherer(bdioHelper, project, hubProjectName,
				hubProjectVersion);
		dependencyGatherer.handleBdioOutput();
	}

	public BdioHelper getBdioHelper() {
		return bdioHelper;
	}

	public void setBdioHelper(final BdioHelper bdioHelper) {
		this.bdioHelper = bdioHelper;
	}

	public String getHubProjectName() {
		return hubProjectName;
	}

	public void setHubProjectName(final String hubProjectName) {
		this.hubProjectName = hubProjectName;
	}

	public String getHubProjectVersion() {
		return hubProjectVersion;
	}

	public void setHubProjectVersion(final String hubProjectVersion) {
		this.hubProjectVersion = hubProjectVersion;
	}

}
