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

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.gradle.task.BuildInfoCustomTask;
import com.blackducksoftware.integration.gradle.task.CreateHubOutput;
import com.blackducksoftware.integration.gradle.task.DeployHubOutput;

public class HubGradlePlugin implements Plugin<Project> {
	private final Logger logger = LoggerFactory.getLogger(HubGradlePlugin.class);

	@Override
	public void apply(final Project project) {
		if ("buildSrc".equals(project.getName())) {
			return;
		}

		final PluginHelper pluginHelper = new PluginHelper(project);

		if (null == project.getTasks().findByName("buildInfoCustomTask")) {
			createBuildInfoCustomTask(project, pluginHelper);
		}

		if (null == project.getTasks().findByName("createHubOutput")) {
			createCreateHubOutputTask(project, pluginHelper);
		}

		if (null == project.getTasks().findByName("deployHubOutput")) {
			createDeployHubOutputTask(project, pluginHelper);
		}
	}

	private void createBuildInfoCustomTask(final Project project, final PluginHelper pluginHelper) {
		logger.info(String.format("Configuring buildInfoCustomTask task for project path: %s", project.getPath()));

		final BuildInfoCustomTask buildInfoCustomTask = project.getTasks().create("buildInfoCustomTask",
				BuildInfoCustomTask.class);
		buildInfoCustomTask.setDescription("Generate build-info file, using project configurations.");
		buildInfoCustomTask.setGroup("reporting");
		buildInfoCustomTask.setPluginHelper(pluginHelper);

		logger.info("Successfully configured buildInfoCustomTask");
	}

	private void createCreateHubOutputTask(final Project project, final PluginHelper pluginHelper) {
		logger.info(String.format("Configuring createHubOutput task for project path: %s", project.getPath()));

		final CreateHubOutput createHubOutputTask = project.getTasks().create("createHubOutput", CreateHubOutput.class);
		createHubOutputTask.setDescription("Generate the bdio file.");
		createHubOutputTask.setGroup("reporting");
		createHubOutputTask.setPluginHelper(pluginHelper);

		logger.info("Successfully configured createHubOutput");
	}

	private void createDeployHubOutputTask(final Project project, final PluginHelper pluginHelper) {
		logger.info(String.format("Configuring deployHubOutput task for project path: %s", project.getPath()));

		final DeployHubOutput deployHubOutputTask = project.getTasks().create("deployHubOutput", DeployHubOutput.class);
		deployHubOutputTask.setDescription("Deploy the bdio file to the specified Hub server.");
		deployHubOutputTask.setGroup("reporting");
		deployHubOutputTask.setPluginHelper(pluginHelper);

		logger.info("Successfully configured deployHubOutput");
	}

}
