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
