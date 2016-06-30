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

import java.io.File;
import java.net.URISyntaxException;
import java.util.Set;

import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.builder.ValidationResultEnum;
import com.blackducksoftware.integration.hub.builder.ValidationResults;
import com.blackducksoftware.integration.hub.global.GlobalFieldKey;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;

public class HubBdioDeployer {
	private final Logger logger = LoggerFactory.getLogger(HubBdioDeployer.class);

	private final PluginHelper pluginHelper;
	private final Project project;
	private final File output;
	private final String hubUrl;
	private final String hubUsername;
	private final String hubPassword;
	private final String hubTimeout;
	private final String hubProxyHost;
	private final String hubProxyPort;
	private final String hubNoProxyHosts;
	private final String hubProxyUsername;
	private final String hubProxyPassword;

	public HubBdioDeployer(final PluginHelper pluginHelper, final Project project, final File output,
			final String hubUrl, final String hubUsername, final String hubPassword, final String hubTimeout,
			final String hubProxyHost, final String hubProxyPort, final String hubNoProxyHosts,
			final String hubProxyUsername, final String hubProxyPassword) {
		this.pluginHelper = pluginHelper;
		this.project = project;
		this.output = output;
		this.hubUrl = hubUrl;
		this.hubUsername = hubUsername;
		this.hubPassword = hubPassword;
		this.hubTimeout = hubTimeout;
		this.hubProxyHost = hubProxyHost;
		this.hubProxyPort = hubProxyPort;
		this.hubNoProxyHosts = hubNoProxyHosts;
		this.hubProxyUsername = hubProxyUsername;
		this.hubProxyPassword = hubProxyPassword;
	}

	public void deployToHub() {
		logger.info("deploying bdio output");
		final File file = pluginHelper.getBdioFile(output, project.getName());

		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setHubUrl(hubUrl);
		builder.setUsername(hubUsername);
		builder.setPassword(hubPassword);
		builder.setTimeout(hubTimeout);
		builder.setProxyHost(hubProxyHost);
		builder.setProxyPort(hubProxyPort);
		builder.setIgnoredProxyHosts(hubNoProxyHosts);
		builder.setProxyUsername(hubProxyUsername);
		builder.setProxyPassword(hubProxyPassword);

		final ValidationResults<GlobalFieldKey, HubServerConfig> results = builder.build();
		if (results.isSuccess()) {
			try {
				final HubServerConfig config = results.getConstructedObject();
				uploadFileToHub(config);
			} catch (final URISyntaxException e) {
				logger.error("Hub URI invalid: " + e.getMessage());
			}
		} else {
			logErrors(results);
		}

		logger.info("Deployed Black Duck I/O json: " + file.getAbsolutePath());
	}

	private void uploadFileToHub(final HubServerConfig config) throws URISyntaxException {
		final RestConnection connection = new RestConnection(config.getHubUrl().toString());
		final HubProxyInfo proxyInfo = config.getProxyInfo();
		if (proxyInfo.shouldUseProxyForUrl(config.getHubUrl())) {
			connection.setProxyProperties(proxyInfo);
		}

		// final HubIntRestService service = new HubIntRestService(connection);
		// TODO: implement the rest call to upload the BDIO file
	}

	private void logErrors(final ValidationResults<GlobalFieldKey, HubServerConfig> results) {
		logger.error("Invalid Hub Server Configuration skipping file deployment:");

		final Set<GlobalFieldKey> keySet = results.getResultMap().keySet();
		for (final GlobalFieldKey key : keySet) {
			if (results.hasWarnings(key)) {
				logger.error(results.getResultString(key, ValidationResultEnum.WARN));
			}
			if (results.hasErrors(key)) {
				logger.error(results.getResultString(key, ValidationResultEnum.ERROR));
			}
		}
	}

}
