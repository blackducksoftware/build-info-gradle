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
import java.util.List;
import java.util.Set;

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
	private final Logger logger = LoggerFactory.getLogger(DependencyGatherer.class);

	private final PluginHelper pluginHelper;
	private final Project rootProject;
	private final File output;

	public DependencyGatherer(final PluginHelper pluginHelper, final Project project, final File output) {
		this.pluginHelper = pluginHelper;
		this.rootProject = project;
		this.output = output;
	}

	public void handleBdioOutput() throws IOException {
		logger.info("creating bdio output");
		final String groupId = rootProject.getGroup().toString();
		final String artifactId = rootProject.getName();
		final String version = rootProject.getVersion().toString();
		final Gav projectGav = new Gav(groupId, artifactId, version);

		final List<DependencyNode> children = new ArrayList<>();
		final DependencyNode root = new DependencyNode(projectGav, children);

		getProjectDependencies(rootProject, children);
		for (final Project childProject : rootProject.getChildProjects().values()) {
			getProjectDependencies(childProject, children);
		}

		final File file = pluginHelper.getBdioFile(output, artifactId);
		try (final OutputStream outputStream = new FileOutputStream(file)) {
			final BdioConverter bdioConverter = new BdioConverter();
			final CommonBomFormatter commonBomFormatter = new CommonBomFormatter(bdioConverter);
			commonBomFormatter.writeProject(outputStream, rootProject.getName(), root);
		}

		logger.info("Created Black Duck I/O json: " + file.getAbsolutePath());
	}

	private void getProjectDependencies(final Project project, final List<DependencyNode> children) {
		final ScopesHelper scopesHelper = new ScopesHelper(project);
		final Set<Configuration> configurations = project.getConfigurations();
		for (final Configuration configuration : configurations) {
			if (scopesHelper.shouldIncludeConfigurationInDependencyGraph(configuration.getName())) {
				final ResolvedConfiguration resolvedConfiguration = configuration.getResolvedConfiguration();
				final Set<ResolvedDependency> resolvedDependencies = resolvedConfiguration
						.getFirstLevelModuleDependencies();
				for (final ResolvedDependency resolvedDependency : resolvedDependencies) {
					children.add(createCommonDependencyNode(resolvedDependency));
				}
			}
		}
	}

	private DependencyNode createCommonDependencyNode(final ResolvedDependency resolvedDependency) {
		final Gav gav = createGavFromDependencyNode(resolvedDependency);
		final List<DependencyNode> children = new ArrayList<>();
		final DependencyNode dependencyNode = new DependencyNode(gav, children);

		for (final ResolvedDependency child : resolvedDependency.getChildren()) {
			children.add(createCommonDependencyNode(child));
		}

		return dependencyNode;
	}

	private Gav createGavFromDependencyNode(final ResolvedDependency resolvedDependency) {
		final String groupId = resolvedDependency.getModuleGroup();
		final String artifactId = resolvedDependency.getModuleName();
		final String version = resolvedDependency.getModuleVersion();

		final Gav gav = new Gav(groupId, artifactId, version);
		return gav;
	}

}
