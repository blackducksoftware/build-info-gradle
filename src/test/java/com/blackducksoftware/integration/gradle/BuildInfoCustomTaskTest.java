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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.After;
import org.junit.Test;

import com.blackducksoftware.integration.build.BuildDependency;
import com.blackducksoftware.integration.build.BuildInfo;
import com.google.gson.Gson;

public class BuildInfoCustomTaskTest {
	@After
	public void testCleanup() {
		System.clearProperty(GradleUtil.INCLUDED_CONFIGURATIONS_PROPERTY);
	}

	public void cleanup(final File file) {
		if (file.isDirectory()) {
			for (final File innerFile : file.listFiles()) {
				cleanup(innerFile);
			}
		} else {
			file.delete();
		}
	}

	@Test
	public void canAddTaskToProject() {
		final Project project = ProjectBuilder.builder().build();
		try {
			project.getTasks().create("buildInfoCustomTask", BuildInfoCustomTask.class);

			final Task task = project.getTasks().getByName("buildInfoCustomTask");

			assertNotNull(task);

			assertTrue(task instanceof BuildInfoCustomTask);
		} finally {
			cleanup(project.getBuildDir());
		}
	}

	@Test
	public void canCreateBuildInfoFile() throws IOException {
		final Project project = ProjectBuilder.builder().build();
		try {
			project.getTasks().create("buildInfoCustomTask", BuildInfoCustomTask.class);

			final BuildInfoCustomTask task = (BuildInfoCustomTask) project.getTasks().getByName("buildInfoCustomTask");

			assertNotNull(task);

			task.gatherDeps();

			final File buildDir = project.getBuildDir();
			final File buildInfoFile = new File(buildDir, "BlackDuck/build-info.json");
			assertTrue(buildInfoFile.exists());
			System.out.println(buildInfoFile.getAbsolutePath());
			final String buildInfoContent = FileUtils.readFileToString(buildInfoFile);
			assertTrue(buildInfoContent.contains("type"));
			assertTrue(buildInfoContent.contains("org.gradle"));
			assertTrue(buildInfoContent.contains("group"));
			assertTrue(buildInfoContent.contains("artifact"));
			assertTrue(buildInfoContent.contains("version"));
			assertTrue(buildInfoContent.contains("id"));
			assertTrue(buildInfoContent.contains("dependencies"));
		} finally {
			cleanup(project.getBuildDir());
		}
	}

	@Test
	public void canCreateBuildInfoFileWithDependencies() throws IOException {
		System.setProperty(GradleUtil.INCLUDED_CONFIGURATIONS_PROPERTY, "testingConfig,testingConfig2");
		final Project project = ProjectBuilder.builder().build();
		try {
			project.getRepositories().add(project.getRepositories().mavenCentral());
			final Configuration testingConf1 = project.getConfigurations().create("testingConfig");
			final Configuration testingConf2 = project.getConfigurations().create("testingConfig2");
			final Configuration testingConf3 = project.getConfigurations().create("testingConfig3");
			final Dependency dep = project.getDependencies().create("jaxen:jaxen:1.1.1");
			final Dependency dep2 = project.getDependencies().create("junit:junit:4.10");
			testingConf1.getDependencies().add(dep);
			testingConf2.getDependencies().add(dep);
			testingConf3.getDependencies().add(dep);
			testingConf3.getDependencies().add(dep2);

			project.getTasks().create("buildInfoCustomTask", BuildInfoCustomTask.class);

			final BuildInfoCustomTask task = (BuildInfoCustomTask) project.getTasks().getByName("buildInfoCustomTask");

			assertNotNull(task);

			System.setProperty(GradleUtil.BUILD_ID_PROPERTY, "TestBuildId");

			task.gatherDeps();

			final File buildDir = project.getBuildDir();
			final File buildInfoFile = new File(buildDir, "BlackDuck/build-info.json");
			assertTrue(buildInfoFile.exists());
			final String buildInfoContent = FileUtils.readFileToString(buildInfoFile);
			final Gson gson = new Gson();
			final BuildInfo buildInfo = gson.fromJson(buildInfoContent, BuildInfo.class);
			System.out.println(buildInfo);
			assertEquals("TestBuildId", buildInfo.getBuildId());
			assertEquals("org.gradle", buildInfo.getBuildArtifact().getType());
			assertEquals(9, buildInfo.getDependencies().size());
			final BuildDependency dependency = buildInfo.getDependencies().iterator().next();

			assertTrue(dependency.getScopes().contains("testingConfig"));
			assertTrue(dependency.getScopes().contains("testingConfig2"));
			assertTrue(dependency.getScopes().contains("testingConfig3"));
		} finally {
			cleanup(project.getBuildDir());
		}
	}

	@Test
	public void configurationsNotIncluded() throws IOException {
		final Project project = ProjectBuilder.builder().build();
		try {
			project.getRepositories().add(project.getRepositories().mavenCentral());
			final Configuration testingConf1 = project.getConfigurations().create("testingConfig");
			final Configuration testingConf2 = project.getConfigurations().create("testingConfig2");
			final Configuration testingConf3 = project.getConfigurations().create("testingConfig3");
			final Dependency dep = project.getDependencies().create("jaxen:jaxen:1.1.1");
			final Dependency dep2 = project.getDependencies().create("junit:junit:4.10");
			testingConf1.getDependencies().add(dep);
			testingConf2.getDependencies().add(dep);
			testingConf3.getDependencies().add(dep);
			testingConf3.getDependencies().add(dep2);

			project.getTasks().create("buildInfoCustomTask", BuildInfoCustomTask.class);

			final BuildInfoCustomTask task = (BuildInfoCustomTask) project.getTasks().getByName("buildInfoCustomTask");

			assertNotNull(task);

			System.setProperty(GradleUtil.BUILD_ID_PROPERTY, "TestBuildId");

			task.gatherDeps();

			final File buildDir = project.getBuildDir();
			final File buildInfoFile = new File(buildDir, "BlackDuck/build-info.json");
			assertTrue(buildInfoFile.exists());
			final String buildInfoContent = FileUtils.readFileToString(buildInfoFile);
			final Gson gson = new Gson();
			final BuildInfo buildInfo = gson.fromJson(buildInfoContent, BuildInfo.class);
			System.out.println(buildInfo);
			assertEquals("TestBuildId", buildInfo.getBuildId());
			assertEquals("org.gradle", buildInfo.getBuildArtifact().getType());
			assertEquals(11, buildInfo.getDependencies().size());
		} finally {
			cleanup(project.getBuildDir());
		}
	}

}
