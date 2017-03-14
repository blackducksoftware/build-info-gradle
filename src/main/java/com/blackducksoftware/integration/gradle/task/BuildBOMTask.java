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

import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.BOM_WAIT_ERROR;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.BUILD_TOOL_CONFIGURATION_ERROR;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.CHECK_POLICIES_ERROR;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.CHECK_POLICIES_FINISHED;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.CHECK_POLICIES_STARTING;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.CREATE_FLAT_DEPENDENCY_LIST_ERROR;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.CREATE_FLAT_DEPENDENCY_LIST_FINISHED;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.CREATE_FLAT_DEPENDENCY_LIST_STARTING;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.CREATE_HUB_OUTPUT_ERROR;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.CREATE_HUB_OUTPUT_FINISHED;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.CREATE_HUB_OUTPUT_STARTING;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.CREATE_REPORT_FINISHED;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.CREATE_REPORT_STARTING;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.DEPLOY_HUB_OUTPUT_ERROR;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.DEPLOY_HUB_OUTPUT_FINISHED;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.DEPLOY_HUB_OUTPUT_STARTING;
import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.FAILED_TO_CREATE_REPORT;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.gradle.DependencyGatherer;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.buildtool.BuildToolHelper;
import com.blackducksoftware.integration.hub.buildtool.DependencyNode;
import com.blackducksoftware.integration.hub.buildtool.FlatDependencyListWriter;
import com.blackducksoftware.integration.hub.buildtool.bdio.BdioDependencyWriter;
import com.blackducksoftware.integration.hub.dataservice.policystatus.PolicyStatusDescription;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.model.enumeration.VersionBomPolicyStatusOverallStatusEnum;
import com.blackducksoftware.integration.hub.model.view.VersionBomPolicyStatusView;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

public class BuildBOMTask extends DefaultTask {
    public final Logger logger = LoggerFactory.getLogger(getClass());

    private BuildToolHelper BUILD_TOOL_HELPER;

    private boolean hubIgnoreFailure = false;

    private String hubProjectName;

    private String hubVersionName;

    private String hubUrl;

    private String hubUsername;

    private String hubPassword;

    private int hubTimeout = 120;

    private String hubProxyHost;

    private String hubProxyPort;

    private String hubNoProxyHosts;

    private String hubProxyUsername;

    private String hubProxyPassword;

    private boolean createFlatDependencyList;

    private boolean createHubBdio = true;

    private boolean deployHubBdio = true;

    private boolean createHubReport;

    private boolean checkPolicies;

    private long hubScanTimeout = 300;

    private File outputDirectory;

    private String includedConfigurations = "compile";

    private String excludedModules = "";

    private HubServicesFactory services;

    private boolean waitedForHub;

    public BuildBOMTask() {
        final File buildDir = null == getProject().getRootProject() ? getProject().getBuildDir() : getProject().getRootProject().getBuildDir();
        outputDirectory = new File(buildDir, "blackduck");
    }

    @TaskAction
    public void task() {
        try {
            performTask();
        } catch (final Exception e) {
            if (isHubIgnoreFailure()) {
                logger.warn(String.format(
                        "Your task has failed: %s. Build will NOT be failed due to hub.ignore.failure being true.",
                        e.getMessage()));
            } else {
                throw e;
            }
        }
    }

    public void performTask() {
        try {
            BUILD_TOOL_HELPER = new BuildToolHelper(new Slf4jIntLogger(logger));

            if (getCreateFlatDependencyList()) {
                createFlatDependencyList();
            }
            if (getCreateHubBdio()) {
                createHubBDIO();
            }
            if (getDeployHubBdio()) {
                deployHubBDIO();
            }
            if (getCreateHubReport()) {
                createHubReport();
            }
            if (getCheckPolicies()) {
                checkHubPolicies();
            }
        } catch (final Exception e) {
            if (hubIgnoreFailure) {
                logger.error(e.getMessage(), e);
            } else {
                throw e;
            }
        }
    }

    private RestConnection getRestConnection(final HubServerConfig hubServerConfig) throws EncryptionException {
        final Slf4jIntLogger intLogger = new Slf4jIntLogger(logger);
        return hubServerConfig.createCredentialsRestConnection(intLogger);
    }

    private HubServicesFactory getHubServicesFactory() throws GradleException {
        if (services == null) {
            final RestConnection restConnection;
            try {
                final HubServerConfig hubServerConfig = getHubServerConfigBuilder().build();
                restConnection = getRestConnection(hubServerConfig);
            } catch (final IllegalArgumentException e) {
                throw new GradleException(String.format(BUILD_TOOL_CONFIGURATION_ERROR, e.getMessage()), e);
            } catch (final EncryptionException e) {
                throw new GradleException(String.format(BUILD_TOOL_CONFIGURATION_ERROR, e.getMessage()), e);
            }
            services = new HubServicesFactory(restConnection);
        }
        return services;
    }

    private void waitForHub() throws GradleException {
        if (getDeployHubBdio() && !waitedForHub) {
            try {
                BUILD_TOOL_HELPER.waitForHub(getHubServicesFactory(), getHubProjectName(), getHubVersionName(), getHubScanTimeout());
                waitedForHub = true;
            } catch (final IntegrationException e) {
                throw new GradleException(String.format(BOM_WAIT_ERROR, e.getMessage()), e);
            }
        }
    }

    private void createFlatDependencyList() throws GradleException {
        logger.info(String.format(CREATE_FLAT_DEPENDENCY_LIST_STARTING, getFlatFilename()));

        try {
            final DependencyGatherer dependencyGatherer = new DependencyGatherer(getIncludedConfigurations(),
                    getExcludedModules());
            final DependencyNode rootNode = dependencyGatherer.getFullyPopulatedRootNode(getProject(), getHubProjectName(), getHubVersionName());

            BUILD_TOOL_HELPER.createFlatOutput(rootNode,
                    getHubProjectName(), getHubVersionName(), getOutputDirectory());
        } catch (final IOException e) {
            throw new GradleException(String.format(CREATE_FLAT_DEPENDENCY_LIST_ERROR, e.getMessage()), e);
        }

        logger.info(String.format(CREATE_FLAT_DEPENDENCY_LIST_FINISHED, getFlatFilename()));
    }

    private void createHubBDIO() throws GradleException {
        logger.info(String.format(CREATE_HUB_OUTPUT_STARTING, getBdioFilename()));

        try {
            final DependencyGatherer dependencyGatherer = new DependencyGatherer(getIncludedConfigurations(),
                    getExcludedModules());
            final DependencyNode rootNode = dependencyGatherer.getFullyPopulatedRootNode(getProject(), getHubProjectName(), getHubVersionName());

            BUILD_TOOL_HELPER.createHubOutput(rootNode, getProject().getName(), getHubProjectName(),
                    getHubVersionName(), getOutputDirectory());
        } catch (final IOException e) {
            throw new GradleException(String.format(CREATE_HUB_OUTPUT_ERROR, e.getMessage()), e);
        }

        logger.info(String.format(CREATE_HUB_OUTPUT_FINISHED, getBdioFilename()));
    }

    private void deployHubBDIO() throws GradleException {
        logger.info(String.format(DEPLOY_HUB_OUTPUT_STARTING, getBdioFilename()));

        try {
            BUILD_TOOL_HELPER.deployHubOutput(getHubServicesFactory(), getOutputDirectory(),
                    getProject().getName());
        } catch (IntegrationException | IllegalArgumentException e) {
            throw new GradleException(String.format(DEPLOY_HUB_OUTPUT_ERROR, e.getMessage()), e);
        }
        logger.info(String.format(DEPLOY_HUB_OUTPUT_FINISHED, getBdioFilename()));
    }

    private void createHubReport() throws GradleException {
        logger.info(String.format(CREATE_REPORT_STARTING, getBdioFilename()));
        waitForHub();
        final File reportOutput = new File(getOutputDirectory(), "report");
        try {
            BUILD_TOOL_HELPER.createRiskReport(getHubServicesFactory(), reportOutput, getHubProjectName(), getHubVersionName(), getHubScanTimeout());
        } catch (final IntegrationException e) {
            throw new GradleException(String.format(FAILED_TO_CREATE_REPORT, e.getMessage()), e);
        }
        logger.info(String.format(CREATE_REPORT_FINISHED, getBdioFilename()));
    }

    private void checkHubPolicies() throws GradleException {
        logger.info(String.format(CHECK_POLICIES_STARTING, getBdioFilename()));
        waitForHub();
        try {
            final VersionBomPolicyStatusView policyStatusItem = BUILD_TOOL_HELPER.checkPolicies(getHubServicesFactory(), getHubProjectName(),
                    getHubVersionName());
            handlePolicyStatusItem(policyStatusItem);
        } catch (IllegalArgumentException | IntegrationException e) {
            throw new GradleException(String.format(CHECK_POLICIES_ERROR, e.getMessage()), e);
        }

        logger.info(String.format(CHECK_POLICIES_FINISHED, getBdioFilename()));
    }

    public void handlePolicyStatusItem(final VersionBomPolicyStatusView policyStatusItem) {
        final PolicyStatusDescription policyStatusDescription = new PolicyStatusDescription(policyStatusItem);
        final String policyStatusMessage = policyStatusDescription.getPolicyStatusMessage();
        logger.info(policyStatusMessage);
        if (VersionBomPolicyStatusOverallStatusEnum.IN_VIOLATION == policyStatusItem.getOverallStatus()) {
            throw new GradleException(policyStatusMessage);
        }
    }

    public String getBdioFilename() {
        return BdioDependencyWriter.getFilename(getHubProjectName());
    }

    public String getFlatFilename() {
        return FlatDependencyListWriter.getFilename(getHubProjectName());
    }

    public HubServerConfigBuilder getHubServerConfigBuilder() {
        final HubServerConfigBuilder hubServerConfigBuilder = new HubServerConfigBuilder();
        hubServerConfigBuilder.setHubUrl(hubUrl);
        hubServerConfigBuilder.setUsername(hubUsername);
        hubServerConfigBuilder.setPassword(hubPassword);
        hubServerConfigBuilder.setTimeout(hubTimeout);
        hubServerConfigBuilder.setProxyHost(hubProxyHost);
        hubServerConfigBuilder.setProxyPort(hubProxyPort);
        hubServerConfigBuilder.setIgnoredProxyHosts(hubNoProxyHosts);
        hubServerConfigBuilder.setProxyUsername(hubProxyUsername);
        hubServerConfigBuilder.setProxyPassword(hubProxyPassword);

        return hubServerConfigBuilder;
    }

    public String getHubProjectName() {
        if (StringUtils.isNotBlank(hubProjectName)) {
            return hubProjectName;
        } else {
            return getProject().getName() + ":" + getProject().getVersion().toString();
        }
    }

    public String getHubVersionName() {
        if (StringUtils.isNotBlank(hubVersionName)) {
            return hubVersionName;
        } else {
            return getProject().getVersion().toString();
        }
    }

    public boolean getCreateHubReport() {
        return createHubReport;
    }

    public void setCreateHubReport(final boolean createHubReport) {
        this.createHubReport = createHubReport;
    }

    public boolean isHubIgnoreFailure() {
        return hubIgnoreFailure;
    }

    public void setHubIgnoreFailure(final boolean hubIgnoreFailure) {
        this.hubIgnoreFailure = hubIgnoreFailure;
    }

    public String getHubUrl() {
        return hubUrl;
    }

    public void setHubUrl(final String hubUrl) {
        this.hubUrl = hubUrl;
    }

    public String getHubUsername() {
        return hubUsername;
    }

    public void setHubUsername(final String hubUsername) {
        this.hubUsername = hubUsername;
    }

    public String getHubPassword() {
        return hubPassword;
    }

    public void setHubPassword(final String hubPassword) {
        this.hubPassword = hubPassword;
    }

    public int getHubTimeout() {
        return hubTimeout;
    }

    public void setHubTimeout(final int hubTimeout) {
        this.hubTimeout = hubTimeout;
    }

    public String getHubProxyHost() {
        return hubProxyHost;
    }

    public void setHubProxyHost(final String hubProxyHost) {
        this.hubProxyHost = hubProxyHost;
    }

    public String getHubProxyPort() {
        return hubProxyPort;
    }

    public void setHubProxyPort(final String hubProxyPort) {
        this.hubProxyPort = hubProxyPort;
    }

    public String getHubNoProxyHosts() {
        return hubNoProxyHosts;
    }

    public void setHubNoProxyHosts(final String hubNoProxyHosts) {
        this.hubNoProxyHosts = hubNoProxyHosts;
    }

    public String getHubProxyUsername() {
        return hubProxyUsername;
    }

    public void setHubProxyUsername(final String hubProxyUsername) {
        this.hubProxyUsername = hubProxyUsername;
    }

    public String getHubProxyPassword() {
        return hubProxyPassword;
    }

    public void setHubProxyPassword(final String hubProxyPassword) {
        this.hubProxyPassword = hubProxyPassword;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(final File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void setHubProjectName(final String hubProjectName) {
        this.hubProjectName = hubProjectName;
    }

    public void setHubVersionName(final String hubVersionName) {
        this.hubVersionName = hubVersionName;
    }

    public long getHubScanTimeout() {
        return hubScanTimeout;
    }

    public void setHubScanTimeout(final long hubScanTimeout) {
        this.hubScanTimeout = hubScanTimeout;
    }

    public String getIncludedConfigurations() {
        return includedConfigurations;
    }

    public void setIncludedConfigurations(final String includedConfigurations) {
        this.includedConfigurations = includedConfigurations;
    }

    public String getExcludedModules() {
        return excludedModules;
    }

    public void setExcludedModules(final String excludedModules) {
        this.excludedModules = excludedModules;
    }

    public boolean getCreateFlatDependencyList() {
        return createFlatDependencyList;
    }

    public void setCreateFlatDependencyList(final boolean createFlatDependencyList) {
        this.createFlatDependencyList = createFlatDependencyList;
    }

    public boolean getCreateHubBdio() {
        return createHubBdio;
    }

    public void setCreateHubBdio(final boolean createHubBdio) {
        this.createHubBdio = createHubBdio;
    }

    public boolean getDeployHubBdio() {
        return deployHubBdio;
    }

    public void setDeployHubBdio(final boolean deployHubBdio) {
        this.deployHubBdio = deployHubBdio;
    }

    public boolean getCheckPolicies() {
        return checkPolicies;
    }

    public void setCheckPolicies(final boolean checkPolicies) {
        this.checkPolicies = checkPolicies;
    }

}
