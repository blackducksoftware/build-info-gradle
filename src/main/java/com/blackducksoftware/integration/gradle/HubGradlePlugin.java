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

import com.blackducksoftware.integration.gradle.task.CheckHubPolicies;
import com.blackducksoftware.integration.gradle.task.CreateFlatDependencyList;
import com.blackducksoftware.integration.gradle.task.CreateHubOutput;
import com.blackducksoftware.integration.gradle.task.DeployHubOutput;
import com.blackducksoftware.integration.gradle.task.DeployHubOutputAndCheckPolicies;

public class HubGradlePlugin implements Plugin<Project> {
	private final Logger logger = LoggerFactory.getLogger(HubGradlePlugin.class);

	@Override
	public void apply(final Project project) {
		if ("buildSrc".equals(project.getName())) {
			return;
		}

		final TaskHelper taskHelper = new TaskHelper(project);

		if (project.getTasks().findByName("createFlatDependencyList") == null) {
			createFlatDependencyListTask(project, taskHelper);
		}

		if (project.getTasks().findByName("createHubOutput") == null) {
			createCreateHubOutputTask(project, taskHelper);
		}

		if (project.getTasks().findByName("deployHubOutput") == null) {
			createDeployHubOutputTask(project, taskHelper);
		}

		if (project.getTasks().findByName("checkHubPolicies") == null) {
			createCheckHubPoliciesTask(project, taskHelper);
		}

		if (project.getTasks().findByName("deployAndCheckHubPolicies") == null) {
			createDeployAndCheckHubPoliciesTask(project, taskHelper);
		}
	}

	private void createCreateHubOutputTask(final Project project, final TaskHelper taskHelper) {
		logger.info(String.format("Configuring createHubOutput task for project path: %s", project.getPath()));

		final CreateHubOutput createHubOutputTask = project.getTasks().create("createHubOutput", CreateHubOutput.class);
		createHubOutputTask.setDescription("Generate the bdio file.");
		createHubOutputTask.setGroup("reporting");
		createHubOutputTask.taskHelper = taskHelper;

		logger.info("Successfully configured createHubOutput");
	}

	private void createDeployHubOutputTask(final Project project, final TaskHelper taskHelper) {
		logger.info(String.format("Configuring deployHubOutput task for project path: %s", project.getPath()));

		final DeployHubOutput deployHubOutputTask = project.getTasks().create("deployHubOutput", DeployHubOutput.class);
		deployHubOutputTask.setDescription("Deploy the bdio file to the specified Hub server.");
		deployHubOutputTask.setGroup("reporting");
		deployHubOutputTask.taskHelper = taskHelper;

		logger.info("Successfully configured deployHubOutput");
	}

	private void createFlatDependencyListTask(final Project project, final TaskHelper taskHelper) {
		logger.info(String.format("Configuring createHubOutput task for project path: %s", project.getPath()));

		final CreateFlatDependencyList createFlatDependencyListTask = project.getTasks()
				.create("createFlatDependencyList", CreateFlatDependencyList.class);
		createFlatDependencyListTask.setDescription("Generate a flat list of unique dependencies.");
		createFlatDependencyListTask.setGroup("reporting");
		createFlatDependencyListTask.taskHelper = taskHelper;

		logger.info("Successfully configured createFlatDependencyList");
	}

	private void createCheckHubPoliciesTask(final Project project, final TaskHelper taskHelper) {
		logger.info(String.format("Configuring checkHubPolicies task for project path: %s", project.getPath()));

		final CheckHubPolicies checkHubPoliciesTask = project.getTasks().create("checkHubPolicies",
				CheckHubPolicies.class);
		checkHubPoliciesTask.setDescription("Check the project's policies on the Hub.");
		checkHubPoliciesTask.setGroup("reporting");
		checkHubPoliciesTask.taskHelper = taskHelper;

		logger.info("Successfully configured checkHubPolicies");
	}

	private void createDeployAndCheckHubPoliciesTask(final Project project, final TaskHelper taskHelper) {
		logger.info(
				String.format("Configuring deployAndCheckHubPolicies task for project path: %s", project.getPath()));

		final DeployHubOutputAndCheckPolicies deployHubOutputAndCheckPolicies = project.getTasks()
				.create("deployHubOutputAndCheckPolicies", DeployHubOutputAndCheckPolicies.class);
		deployHubOutputAndCheckPolicies.setDescription(
				"Deploy the bdio file and wait for completion, then check the project's policies on the Hub.");
		deployHubOutputAndCheckPolicies.setGroup("reporting");
		deployHubOutputAndCheckPolicies.taskHelper = taskHelper;

		logger.info("Successfully configured deployHubOutputAndCheckPolicies");
	}

}
