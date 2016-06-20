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

public class BuildInfoGradlePlugin implements Plugin<Project> {
	@Override
	public void apply(final Project project) {
		if ("buildSrc".equals(project.getName())) {
			return;
		}

		if (null == project.getTasks().findByName("buildInfoCustomTask")) {
			final boolean isRoot = project.equals(project.getRootProject());
			System.out.println(
					String.format("Configuring buildInfoCustomTask task for project path: %s", project.getPath()));
			System.out.println(String.format("is root: %s", isRoot));
			createBuildInfoCustomTask(project);
		}

		System.out.println("Using Black Duck Build Info Plugin for " + project.getPath());
	}

	private void createBuildInfoCustomTask(final Project project) {
		System.out.println("creating build info custom task");
		final BuildInfoCustomTask buildInfoCustomTask = project.getTasks().create("buildInfoCustomTask",
				BuildInfoCustomTask.class);
		buildInfoCustomTask.setDescription("'Generate build-info file, using project configurations.'");
		buildInfoCustomTask.setGroup("reporting");
	}

}
