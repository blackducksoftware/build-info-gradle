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
import java.io.IOException;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.tasks.diagnostics.DependencyReportTask;

public class BuildInfoGradlePlugin implements Plugin<Project> {
	private final GradleUtil gradleUtil = new GradleUtil();

	@Override
	public void apply(final Project project) {
		if ("buildSrc".equals(project.getName())) {
			return;
		}

		addBuildInfoCustomTask(project);
		System.out.println("Using Black Duck Build Info Plugin for " + project.getPath());

		final File output = getDependencyReportOutputFile(project);
		if (output != null) {
			if (output.exists()) {
				output.delete();
			}
		}

		final DependencyReportTask dependencyReportTask = project.getTasks().create("buildInfoDependencyTree",
				DependencyReportTask.class);
		dependencyReportTask.setOutputFile(output);

		try {
			System.out.println("Dependency report output " + output.getCanonicalPath());
		} catch (final IOException e) {
			System.out.println("Couldn't get the path of the file : " + e.toString());
		}
	}

	public BuildInfoCustomTask addBuildInfoCustomTask(final Project project) {
		BuildInfoCustomTask buildInfoCustomTask = null;
		try {
			buildInfoCustomTask = (BuildInfoCustomTask) project.getTasks().getByName("buildInfoCustomTask");
		} catch (final UnknownTaskException e) {
		}

		if (buildInfoCustomTask == null) {
			final boolean isRoot = project.equals(project.getRootProject());
			System.out.println(
					"Configuring buildInfoCustomTask task for project : " + project.getPath() + ": is root? " + isRoot);
			buildInfoCustomTask = createBuildInfoCustomTask(project);
			buildInfoCustomTask.setGroup("reporting");
		}

		return buildInfoCustomTask;
	}

	private BuildInfoCustomTask createBuildInfoCustomTask(final Project project) {
		final BuildInfoCustomTask buildInfoCustomTask = project.getTasks().create("buildInfoCustomTask",
				BuildInfoCustomTask.class);
		buildInfoCustomTask.setDescription("'Generate build-info file, using project configurations.'");
		return buildInfoCustomTask;
	}

	private File getDependencyReportOutputFile(final Project project) {
		final String dependencyTreeOutputRaw = System.getProperty(GradleUtil.DEPENDENCY_REPORT_OUTPUT);

		if (dependencyTreeOutputRaw == null || dependencyTreeOutputRaw.trim().length() == 0) {
			final File buildDir = gradleUtil.findBuildDir(project);
			final File blackDuckDir = new File(buildDir, "BlackDuck/");
			blackDuckDir.mkdirs();

			File dependencyTreeFile = null;
			if (project.getName() != null && project.getName().trim().length() != 0) {
				dependencyTreeFile = new File(blackDuckDir, "dependencyTree-" + project.getName() + ".txt");
			} else {
				dependencyTreeFile = new File(blackDuckDir, "dependencyTree.txt");
			}

			return dependencyTreeFile;
		}

		return new File(dependencyTreeOutputRaw);
	}

}
