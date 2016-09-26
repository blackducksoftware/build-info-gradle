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

import com.blackducksoftware.integration.gradle.task.CheckPoliciesTask;
import com.blackducksoftware.integration.gradle.task.CreateFlatDependencyListTask;
import com.blackducksoftware.integration.gradle.task.CreateHubOutputTask;
import com.blackducksoftware.integration.gradle.task.DeployHubOutputAndCheckPoliciesTask;
import com.blackducksoftware.integration.gradle.task.DeployHubOutputTask;

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

		if (project.getTasks().findByName("checkPolicies") == null) {
			createCheckPoliciesTask(project, taskHelper);
		}

		if (project.getTasks().findByName("deployHubOutputAndCheckPolicies") == null) {
			createDeployHubOutputAndCheckPoliciesTask(project, taskHelper);
		}
	}

	private void createFlatDependencyListTask(final Project project, final TaskHelper taskHelper) {
		logger.info(String.format("Configuring createFlatDependencyList task for project path: %s", project.getPath()));

		final CreateFlatDependencyListTask createFlatDependencyList = project.getTasks()
				.create("createFlatDependencyList", CreateFlatDependencyListTask.class);
		createFlatDependencyList.setDescription("Create a flat list of unique dependencies.");
		createFlatDependencyList.setGroup("reporting");
		createFlatDependencyList.taskHelper = taskHelper;

		logger.info("Successfully configured createFlatDependencyList");
	}

	private void createCreateHubOutputTask(final Project project, final TaskHelper taskHelper) {
		logger.info(String.format("Configuring createHubOutput task for project path: %s", project.getPath()));

		final CreateHubOutputTask createHubOutput = project.getTasks().create("createHubOutput",
				CreateHubOutputTask.class);
		createHubOutput.setDescription("Create the bdio file.");
		createHubOutput.setGroup("reporting");
		createHubOutput.taskHelper = taskHelper;

		logger.info("Successfully configured createHubOutput");
	}

	private void createDeployHubOutputTask(final Project project, final TaskHelper taskHelper) {
		logger.info(String.format("Configuring deployHubOutput task for project path: %s", project.getPath()));

		final DeployHubOutputTask deployHubOutput = project.getTasks().create("deployHubOutput",
				DeployHubOutputTask.class);
		deployHubOutput.setDescription("Deploy the bdio file to the Hub server.");
		deployHubOutput.setGroup("reporting");
		deployHubOutput.taskHelper = taskHelper;

		logger.info("Successfully configured deployHubOutput");
	}

	private void createCheckPoliciesTask(final Project project, final TaskHelper taskHelper) {
		logger.info(String.format("Configuring checkPolicies task for project path: %s", project.getPath()));

		final CheckPoliciesTask checkPolicies = project.getTasks().create("checkPolicies", CheckPoliciesTask.class);
		checkPolicies.setDescription(
				"Check the project's policies on the Hub. *Note: This will check ONLY the current policy status, NOT the updated status after a deploy of bdio output. For that, you would use deployHubOutputAndCheckPolicies.");
		checkPolicies.setGroup("reporting");
		checkPolicies.taskHelper = taskHelper;

		logger.info("Successfully configured checkPolicies");
	}

	private void createDeployHubOutputAndCheckPoliciesTask(final Project project, final TaskHelper taskHelper) {
		logger.info(String.format("Configuring deployHubOutputAndCheckPolicies task for project path: %s",
				project.getPath()));

		final DeployHubOutputAndCheckPoliciesTask deployHubOutputAndCheckPolicies = project.getTasks()
				.create("deployHubOutputAndCheckPolicies", DeployHubOutputAndCheckPoliciesTask.class);
		deployHubOutputAndCheckPolicies.setDescription(
				"Create, then deploy the bdio file and wait for completion, then check the project's policies on the Hub.");
		deployHubOutputAndCheckPolicies.setGroup("reporting");
		deployHubOutputAndCheckPolicies.taskHelper = taskHelper;

		logger.info("Successfully configured deployHubOutputAndCheckPolicies");
	}

}
