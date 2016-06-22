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

import com.blackducksoftware.integration.gradle.task.BuildInfoCustomTask;
import com.blackducksoftware.integration.gradle.task.HubBdioGenerationTask;

public class HubGradlePlugin implements Plugin<Project> {
	@Override
	public void apply(final Project project) {
		if ("buildSrc".equals(project.getName())) {
			return;
		}

		final PluginHelper pluginHelper = new PluginHelper(project);

		if (null == project.getTasks().findByName("buildInfoCustomTask")) {
			createBuildInfoCustomTask(project, pluginHelper);
		}

		if (null == project.getTasks().findByName("hubBdioGenerationTask")) {
			createHubBdioGenerationTask(project, pluginHelper);
		}
	}

	private void createBuildInfoCustomTask(final Project project, final PluginHelper pluginHelper) {
		System.out
				.println(String.format("Configuring buildInfoCustomTask task for project path: %s", project.getPath()));

		final BuildInfoCustomTask buildInfoCustomTask = project.getTasks().create("buildInfoCustomTask",
				BuildInfoCustomTask.class);
		buildInfoCustomTask.setDescription("Generate build-info file, using project configurations.");
		buildInfoCustomTask.setGroup("reporting");
		buildInfoCustomTask.setPluginHelper(pluginHelper);

		System.out.println("Successfully configured buildInfoCustomTask");
	}

	private void createHubBdioGenerationTask(final Project project, final PluginHelper pluginHelper) {
		System.out.println(
				String.format("Configuring hubBdioGenerationTask task for project path: %s", project.getPath()));

		final HubBdioGenerationTask hubBdioGenerationTask = project.getTasks().create("hubBdioGenerationTask",
				HubBdioGenerationTask.class);
		hubBdioGenerationTask.setDescription("Generate bdio file, using project configurations.");
		hubBdioGenerationTask.setGroup("reporting");
		hubBdioGenerationTask.setPluginHelper(pluginHelper);

		System.out.println("Successfully configured hubBdioGenerationTask");
	}

}
