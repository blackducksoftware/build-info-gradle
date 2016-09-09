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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.build.bdio.BdioConverter;
import com.blackducksoftware.integration.build.bdio.CommonBomFormatter;
import com.blackducksoftware.integration.build.bdio.DependencyNode;
import com.blackducksoftware.integration.build.bdio.Gav;

public class DependencyGatherer {
	public final static String PROPERTY_HUB_PROJECT_NAME = "hubProjectName";
	public final static String PROPERTY_HUB_PROJECT_VERSION = "hubProjectVersion";
	private final Logger logger = LoggerFactory.getLogger(DependencyGatherer.class);

	private final BdioHelper bdioHelper;
	private final Project rootProject;
	final Map<String, DependencyNode> visitedMap = new HashMap<>();
	private final String hubProjectName;
	private final String hubProjectVersion;

	public DependencyGatherer(final BdioHelper bdioHelper, final Project project, final String hubProjectName,
			final String hubProjectVersion) {
		this.bdioHelper = bdioHelper;
		this.rootProject = project;
		this.hubProjectName = hubProjectName;
		this.hubProjectVersion = hubProjectVersion;

	}

	private String getArtifactId() {
		if (StringUtils.isNotBlank(hubProjectName)) {
			return hubProjectName;
		} else {
			return rootProject.getName();
		}
	}

	private String getVersion() {
		if (StringUtils.isNotBlank(hubProjectVersion)) {
			return hubProjectVersion;
		} else {
			return rootProject.getVersion().toString();
		}
	}

	public void handleBdioOutput() throws IOException {
		logger.info("creating bdio output");
		final String groupId = rootProject.getGroup().toString();
		final String artifactId = getArtifactId();
		final String version = getVersion();
		final Gav projectGav = new Gav(groupId, artifactId, version);

		final List<DependencyNode> children = new ArrayList<>();
		final DependencyNode root = new DependencyNode(projectGav, children);
		logger.info("creating bdio graph");
		for (final Project childProject : rootProject.getAllprojects()) {
			getProjectDependencies(childProject, children);
		}
		logger.info("creating bdio file");
		bdioHelper.setBlackDuckReports(new File(rootProject.getBuildDir(), "blackduck"));
		final File file = bdioHelper.getBdioFile(rootProject);
		try (final OutputStream outputStream = new FileOutputStream(file)) {
			final BdioConverter bdioConverter = new BdioConverter();
			final CommonBomFormatter commonBomFormatter = new CommonBomFormatter(bdioConverter);
			commonBomFormatter.writeProject(outputStream, artifactId, root);
		}

		logger.info("Created Black Duck I/O json: " + file.getAbsolutePath());
	}

	private void getProjectDependencies(final Project project, final List<DependencyNode> children) {
		final ScopesHelper scopesHelper = new ScopesHelper(project);
		final Set<Configuration> configurations = project.getConfigurations();
		for (final Configuration configuration : configurations) {
			final String configName = configuration.getName();
			if (scopesHelper.shouldIncludeConfigurationInDependencyGraph(configName)) {
				logger.debug("Resolving dependencies for project: " + project.getName());
				final ResolvedConfiguration resolvedConfiguration = configuration.getResolvedConfiguration();
				final Set<ResolvedDependency> resolvedDependencies = resolvedConfiguration
						.getFirstLevelModuleDependencies();
				for (final ResolvedDependency resolvedDependency : resolvedDependencies) {
					children.add(createCommonDependencyNode(resolvedDependency, 0, configName));
				}
			}
		}
	}

	private DependencyNode createCommonDependencyNode(final ResolvedDependency resolvedDependency, final int level,
			final String configuration) {
		final String gavKey = createGavKey(resolvedDependency);

		final StringBuffer sb = new StringBuffer();
		if (logger.isDebugEnabled()) {
			sb.append("|");
			for (int i = 0; i < level; i++) {
				sb.append(" ");
			}
			sb.append("(");
			sb.append(level);
			sb.append(")-> ");
		}
		final String buffer = sb.toString();
		if (visitedMap.containsKey(gavKey)) {
			if (logger.isDebugEnabled()) {
				logger.debug(buffer + gavKey + " (already visited) config: " + configuration);
			}
			return visitedMap.get(gavKey);
		} else {
			final Gav gav = createGavFromDependencyNode(resolvedDependency);
			if (logger.isDebugEnabled()) {
				logger.debug(buffer + gavKey + " (created) config: " + configuration);
			}
			final List<DependencyNode> children = new ArrayList<>();
			final DependencyNode dependencyNode = new DependencyNode(gav, children);
			for (final ResolvedDependency child : resolvedDependency.getChildren()) {
				if (child.getConfiguration().equals(configuration)) {
					children.add(createCommonDependencyNode(child, level + 1, configuration));
				}
			}
			visitedMap.put(gavKey, dependencyNode);
			return dependencyNode;
		}
	}

	private String createGavKey(final ResolvedDependency resolvedDependency) {
		final String gavKey = resolvedDependency.getModuleGroup() + ":" + resolvedDependency.getModuleName() + ":"
				+ resolvedDependency.getModuleVersion();
		return gavKey;
	}

	private Gav createGavFromDependencyNode(final ResolvedDependency resolvedDependency) {
		final String groupId = resolvedDependency.getModuleGroup();
		final String artifactId = resolvedDependency.getModuleName();
		final String version = resolvedDependency.getModuleVersion();

		final Gav gav = new Gav(groupId, artifactId, version);
		return gav;
	}

}