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

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.api.policy.PolicyStatusEnum;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusItem;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.buildtool.BuildToolHelper;
import com.blackducksoftware.integration.hub.buildtool.FlatDependencyListWriter;
import com.blackducksoftware.integration.hub.buildtool.bdio.BdioDependencyWriter;
import com.blackducksoftware.integration.hub.dataservice.policystatus.PolicyStatusDescription;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

public abstract class HubTask extends DefaultTask {
    // NOTE: getClass() is a little strange here, but we want the runtime class,
    // not HubTask, as it is abstract.
    public final Logger logger = LoggerFactory.getLogger(getClass());

    public static final BuildToolHelper BUILD_TOOL_HELPER = new BuildToolHelper(new Slf4jIntLogger(LoggerFactory.getLogger(HubTask.class)));

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

    private boolean createHubReport = true;

    private long hubScanTimeout = 300;

    private File outputDirectory;

    private String includedConfigurations = "compile";

    private String excludedModules = "";

    public HubTask() {
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

    public abstract void performTask();

    public void handlePolicyStatusItem(final PolicyStatusItem policyStatusItem) {
        final PolicyStatusDescription policyStatusDescription = new PolicyStatusDescription(policyStatusItem);
        final String policyStatusMessage = policyStatusDescription.getPolicyStatusMessage();
        logger.info(policyStatusMessage);
        if (PolicyStatusEnum.IN_VIOLATION == policyStatusItem.getOverallStatus()) {
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
            return getProject().getName();
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

}
