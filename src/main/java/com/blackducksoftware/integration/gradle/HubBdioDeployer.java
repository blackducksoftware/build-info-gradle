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
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gradle.api.Project;
import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.build.bdio.Constants;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.builder.ValidationResultEnum;
import com.blackducksoftware.integration.hub.builder.ValidationResults;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.global.GlobalFieldKey;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;

public class HubBdioDeployer {
	private final Logger logger = LoggerFactory.getLogger(HubBdioDeployer.class);

	private final BdioHelper bdioHelper;
	private final Project project;
	private final String hubUrl;
	private final String hubUsername;
	private final String hubPassword;
	private final String hubTimeout;
	private final String hubProxyHost;
	private final String hubProxyPort;
	private final String hubNoProxyHosts;
	private final String hubProxyUsername;
	private final String hubProxyPassword;

	public HubBdioDeployer(final BdioHelper bdioHelper, final Project project, final String hubUrl,
			final String hubUsername, final String hubPassword, final String hubTimeout, final String hubProxyHost,
			final String hubProxyPort, final String hubNoProxyHosts, final String hubProxyUsername,
			final String hubProxyPassword) {
		this.bdioHelper = bdioHelper;
		this.project = project;
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
		logger.info("Deploying Black Duck I/O output");
		final File file = bdioHelper.getBdioFile(project);

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
				uploadFileToHub(config, file);
			} catch (final URISyntaxException e) {
				logger.error("Hub URI invalid: ", e);
			} catch (final IllegalArgumentException | BDRestException | EncryptionException | IOException e) {
				logger.error("Cannot communicate with hub server.", e);
			} catch (final ResourceDoesNotExistException e) {
				logger.error("Cannot upload the file to the hub server.", e);
			}
		} else {
			logErrors(results);
		}

		logger.info("Deployed Black Duck I/O file: " + file.getAbsolutePath());
	}

	private void uploadFileToHub(final HubServerConfig config, final File file) throws URISyntaxException,
			IllegalArgumentException, BDRestException, EncryptionException, IOException, ResourceDoesNotExistException {
		final RestConnection connection = new RestConnection(config.getHubUrl().toString());
		final HubProxyInfo proxyInfo = config.getProxyInfo();
		if (proxyInfo.shouldUseProxyForUrl(config.getHubUrl())) {
			connection.setProxyProperties(proxyInfo);
		}

		connection.setCookies(config.getGlobalCredentials().getUsername(),
				config.getGlobalCredentials().getDecryptedPassword());

		final List<String> urlSegments = new ArrayList<>();
		urlSegments.add("api");
		urlSegments.add("v1");
		urlSegments.add("bom-import");
		final Set<SimpleEntry<String, String>> queryParameters = new HashSet<>();
		final FileRepresentation content = new FileRepresentation(file, new MediaType(Constants.BDIO_FILE_MEDIA_TYPE));
		final String location = connection.httpPostFromRelativeUrl(urlSegments, queryParameters, content);

		logger.info("Uploaded the file: " + file + " to " + config.getHubUrl().toString());
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
