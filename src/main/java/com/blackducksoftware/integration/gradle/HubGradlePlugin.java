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

import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.CHECK_POLICIES;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.CREATE_FLAT_DEPENDENCY_LIST;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.CREATE_HUB_OUTPUT;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.DEPLOY_HUB_OUTPUT;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.DEPLOY_HUB_OUTPUT_AND_CHECK_POLICIES;

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

        if (project.getTasks().findByName(CREATE_FLAT_DEPENDENCY_LIST) == null) {
            createFlatDependencyListTask(project);
        }

        if (project.getTasks().findByName(CREATE_HUB_OUTPUT) == null) {
            createCreateHubOutputTask(project);
        }

        if (project.getTasks().findByName(DEPLOY_HUB_OUTPUT) == null) {
            createDeployHubOutputTask(project);
        }

        if (project.getTasks().findByName(CHECK_POLICIES) == null) {
            createCheckPoliciesTask(project);
        }

        if (project.getTasks().findByName(DEPLOY_HUB_OUTPUT_AND_CHECK_POLICIES) == null) {
            createDeployHubOutputAndCheckPoliciesTask(project);
        }
    }

    private void createFlatDependencyListTask(final Project project) {
        logger.info(String.format("Configuring %s task for project path: %s", CREATE_FLAT_DEPENDENCY_LIST,
                project.getPath()));

        final CreateFlatDependencyListTask createFlatDependencyList = project.getTasks()
                .create(CREATE_FLAT_DEPENDENCY_LIST, CreateFlatDependencyListTask.class);
        createFlatDependencyList.setDescription("Create a flat list of unique dependencies.");
        createFlatDependencyList.setGroup("reporting");

        logger.info(String.format("Successfully configured %s", CREATE_FLAT_DEPENDENCY_LIST));
    }

    private void createCreateHubOutputTask(final Project project) {
        logger.info(String.format("Configuring %s task for project path: %s", CREATE_HUB_OUTPUT,
                project.getPath()));

        final CreateHubOutputTask createHubOutput = project.getTasks().create(CREATE_HUB_OUTPUT,
                CreateHubOutputTask.class);
        createHubOutput.setDescription("Create the bdio file.");
        createHubOutput.setGroup("reporting");

        logger.info(String.format("Successfully configured %s", CREATE_HUB_OUTPUT));
    }

    private void createDeployHubOutputTask(final Project project) {
        logger.info(String.format("Configuring %s task for project path: %s", DEPLOY_HUB_OUTPUT,
                project.getPath()));

        final DeployHubOutputTask deployHubOutput = project.getTasks().create(DEPLOY_HUB_OUTPUT,
                DeployHubOutputTask.class);
        deployHubOutput.setDescription("Deploy the bdio file to the Hub server.");
        deployHubOutput.setGroup("reporting");

        logger.info(String.format("Successfully configured %s", DEPLOY_HUB_OUTPUT));
    }

    private void createCheckPoliciesTask(final Project project) {
        logger.info(String.format("Configuring %s task for project path: %s", CHECK_POLICIES,
                project.getPath()));

        final CheckPoliciesTask checkPolicies = project.getTasks().create(CHECK_POLICIES, CheckPoliciesTask.class);
        checkPolicies.setDescription(String.format(
                "Check the project's policies on the Hub. *Note: This will check ONLY the current policy status, NOT the updated status after a deploy of bdio output. For that, you would use %s.",
                DEPLOY_HUB_OUTPUT_AND_CHECK_POLICIES));
        checkPolicies.setGroup("reporting");

        logger.info(String.format("Successfully configured %s", CHECK_POLICIES));
    }

    private void createDeployHubOutputAndCheckPoliciesTask(final Project project) {
        logger.info(String.format("Configuring %s task for project path: %s", DEPLOY_HUB_OUTPUT_AND_CHECK_POLICIES,
                project.getPath()));

        final DeployHubOutputAndCheckPoliciesTask deployHubOutputAndCheckPolicies = project.getTasks()
                .create(DEPLOY_HUB_OUTPUT_AND_CHECK_POLICIES, DeployHubOutputAndCheckPoliciesTask.class);
        deployHubOutputAndCheckPolicies.setDescription(
                "Create, then deploy the bdio file and wait for completion, then check the project's policies on the Hub.");
        deployHubOutputAndCheckPolicies.setGroup("reporting");

        logger.info(String.format("Successfully configured %s", DEPLOY_HUB_OUTPUT_AND_CHECK_POLICIES));
    }

}
