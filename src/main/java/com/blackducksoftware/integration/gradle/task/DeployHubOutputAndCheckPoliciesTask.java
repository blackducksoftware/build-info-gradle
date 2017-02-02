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

import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.CHECK_POLICIES_ERROR;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.CREATE_HUB_OUTPUT_ERROR;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.DEPLOY_HUB_OUTPUT_AND_CHECK_POLICIES_FINISHED;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.DEPLOY_HUB_OUTPUT_AND_CHECK_POLICIES_STARTING;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.DEPLOY_HUB_OUTPUT_ERROR;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.FAILED_TO_CREATE_REPORT;

import java.io.File;
import java.io.IOException;

import org.gradle.api.GradleException;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.gradle.DependencyGatherer;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusItem;
import com.blackducksoftware.integration.hub.buildtool.DependencyNode;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;

public class DeployHubOutputAndCheckPoliciesTask extends HubTask {

    @Override
    public void performTask() {
        logger.info(String.format(DEPLOY_HUB_OUTPUT_AND_CHECK_POLICIES_STARTING, getBdioFilename()));

        try {
            final DependencyGatherer dependencyGatherer = new DependencyGatherer(getIncludedConfigurations(),
                    getExcludedModules());
            final DependencyNode rootNode = dependencyGatherer.getFullyPopulatedRootNode(getProject(), getHubProjectName(), getHubVersionName());

            BUILD_TOOL_HELPER.createHubOutput(rootNode, getProject().getName(), getHubProjectName(), getHubVersionName(), getOutputDirectory());
        } catch (final IOException e) {
            throw new GradleException(String.format(CREATE_HUB_OUTPUT_ERROR, e.getMessage()), e);
        }

        final HubServerConfig hubServerConfig = getHubServerConfigBuilder().build();
        final RestConnection restConnection;
        HubServicesFactory services;
        try {
            restConnection = new CredentialsRestConnection(hubServerConfig);
            services = new HubServicesFactory(restConnection);
            // FIXME project name???
            BUILD_TOOL_HELPER.deployHubOutput(services, getOutputDirectory(),
                    getProject().getName());
        } catch (HubIntegrationException | IllegalArgumentException | EncryptionException e) {
            throw new GradleException(String.format(DEPLOY_HUB_OUTPUT_ERROR, e.getMessage()), e);
        }

        try {
            BUILD_TOOL_HELPER.waitForHub(services, getHubProjectName(), getHubVersionName(), getHubScanTimeout());
            if (getCreateHubReport()) {
                final File reportOutput = new File(getOutputDirectory(), "report");
                try {
                    BUILD_TOOL_HELPER.createRiskReport(services, reportOutput, getHubProjectName(), getHubVersionName(), getHubScanTimeout());
                } catch (final HubIntegrationException e) {
                    throw new GradleException(String.format(FAILED_TO_CREATE_REPORT, e.getMessage()), e);
                }
            }

            final PolicyStatusItem policyStatusItem = BUILD_TOOL_HELPER.checkPolicies(services, getHubProjectName(),
                    getHubVersionName());
            handlePolicyStatusItem(policyStatusItem);
        } catch (final HubIntegrationException e) {
            throw new GradleException(String.format(CHECK_POLICIES_ERROR, e.getMessage()), e);
        }

        logger.info(String.format(DEPLOY_HUB_OUTPUT_AND_CHECK_POLICIES_FINISHED, getBdioFilename()));
    }

}
