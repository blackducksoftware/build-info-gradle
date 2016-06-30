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
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import com.blackducksoftware.integration.gradle.HubBdioDeployer;
import com.blackducksoftware.integration.gradle.PluginHelper;

public class DeployHubOutput extends DefaultTask {
	private PluginHelper pluginHelper;
	private String outputDirectory;
	private String hubUrl;
	private String hubUsername;
	private String hubPassword;
	private String hubTimeout;
	private String hubProxyHost;
	private String hubProxyPort;
	private String hubNoProxyHosts;
	private String hubProxyUsername;
	private String hubProxyPassword;

	@TaskAction
	public void deployBdioFileToHub() throws IOException {
		final Project project = getProject();
		File output = pluginHelper.getBlackDuckDirectory();
		if (StringUtils.isNotBlank(outputDirectory)) {
			output = new File(outputDirectory);
		}

		final HubBdioDeployer hubBdioDeployer = new HubBdioDeployer(pluginHelper, project, output, hubUrl, hubUsername,
				hubPassword, hubTimeout, hubProxyHost, hubProxyPort, hubNoProxyHosts, hubProxyUsername,
				hubProxyPassword);
		hubBdioDeployer.deployToHub();
	}

	public PluginHelper getPluginHelper() {
		return pluginHelper;
	}

	public void setPluginHelper(final PluginHelper pluginHelper) {
		this.pluginHelper = pluginHelper;
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(final String outputDirectory) {
		this.outputDirectory = outputDirectory;
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

	public String getHubTimeout() {
		return hubTimeout;
	}

	public void setHubTimeout(final String hubTimeout) {
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

}
