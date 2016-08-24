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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.build.BuildArtifact;
import com.blackducksoftware.integration.build.BuildDependency;
import com.blackducksoftware.integration.build.BuildInfo;
import com.blackducksoftware.integration.gradle.PluginHelper;
import com.blackducksoftware.integration.gradle.ScopesHelper;
import com.google.gson.Gson;

public class BuildInfoCustomTask extends DefaultTask {
	private final Logger logger = LoggerFactory.getLogger(BuildInfoCustomTask.class);

	private PluginHelper pluginHelper;

	@TaskAction
	public void gatherDependencies() throws IOException {
		final Project project = getProject();

		BuildInfo oldBuildInfo = null;
		final File file = new File(
				pluginHelper.getBlackDuckDirectory().getCanonicalPath() + File.separator + BuildInfo.OUTPUT_FILE_NAME);
		if (file.exists()) {
			// Read in the old build-info
			// if it has the same build id, its probably a Gradle build with
			// multiple projects
			// so we add to the dependencies, instead of creating a new
			// build-info.json
			final StringBuilder buildInfoStringBuilder = new StringBuilder();
			BufferedReader br = null;
			try {
				// We read the build-info.json file into a StringBuilder
				br = new BufferedReader(new FileReader(file));
				String line;
				while ((line = br.readLine()) != null) {
					buildInfoStringBuilder.append(line);
				}
			} finally {
				if (br != null) {
					br.close();
				}
			}

			// We use the StringBuilder to make a single json String from the
			// file contents
			final String buildInfoString = buildInfoStringBuilder.toString();
			if (buildInfoString != null && buildInfoString.trim().length() > 0) {
				// build-info.json is not an empty file
				final Gson gson = new Gson();
				// We use Gson to turn the json string into a BuildInfo object
				oldBuildInfo = gson.fromJson(buildInfoString, BuildInfo.class);
			}
		}
		final String buildId = System.getProperty(PluginHelper.BUILD_ID_PROPERTY);
		logger.info("BUILD ID : " + buildId);
		BuildInfo buildInfo;
		if (oldBuildInfo != null && oldBuildInfo.getBuildArtifact() != null && buildId != null
				&& oldBuildInfo.getBuildId().equals(buildId)) {
			// This must be a sub project in a multi-project gradle Build
			logger.info("Will add to the build-info.json file");

			buildInfo = oldBuildInfo;
		} else {
			// Either this is the first time the project is being built, there
			// was no build-info.json file OR
			// the buildId did not match in which case the build-info.json is
			// from a different Build
			logger.info("Will create build-info.json file");

			buildInfo = new BuildInfo();
			buildInfo.setBuildId(buildId);

			final BuildArtifact buildArtifact = new BuildArtifact();
			buildArtifact.setType("org.gradle");
			buildArtifact.setGroup(project.getGroup().toString());
			buildArtifact.setArtifact(project.getName());
			buildArtifact.setVersion(project.getVersion().toString());

			buildInfo.setBuildArtifact(buildArtifact);
		}

		final Map<String, BuildDependency> resolvedDependenciesMap = new HashMap<String, BuildDependency>();
		if (buildInfo.getDependencies() != null && !buildInfo.getDependencies().isEmpty()) {
			for (final BuildDependency dependency : buildInfo.getDependencies()) {
				// Adding previously discovered dependencies to the new map
				final String externalId = dependency.getId();
				resolvedDependenciesMap.put(externalId, dependency);
			}
			buildInfo.setDependencies(new HashSet<BuildDependency>());
		}

		final ScopesHelper scopesHelper = new ScopesHelper(project);
		final Set<Configuration> configurations = project.getConfigurations();
		for (final Configuration configuration : configurations) {
			final Set<ResolvedDependency> dependencies = configuration.getResolvedConfiguration()
					.getFirstLevelModuleDependencies();
			final String scope = configuration.getName();
			for (final ResolvedDependency dependency : dependencies) {
				if (dependency.getAllModuleArtifacts().size() > 0) {
					for (final ResolvedArtifact artifact : dependency.getAllModuleArtifacts()) {
						final ModuleVersionIdentifier id = artifact.getModuleVersion().getId();
						final String groupId = id.getGroup();
						final String artifactId = id.getName();
						final String version = id.getVersion();
						addDependency(resolvedDependenciesMap, scope, groupId, artifactId, version, scopesHelper);
					}
				} else {
					final String groupId = dependency.getModuleGroup();
					final String artifactId = dependency.getModuleName();
					final String version = dependency.getModuleVersion();
					addDependency(resolvedDependenciesMap, scope, groupId, artifactId, version, scopesHelper);
				}
			}
		}
		final Set<BuildDependency> dependencies = new HashSet<BuildDependency>();
		dependencies.addAll(resolvedDependenciesMap.values());
		buildInfo.setDependencies(dependencies);
		buildInfo.close(pluginHelper.getBlackDuckDirectory());
	}

	private void addDependency(final Map<String, BuildDependency> dependenciesMap, final String scope,
			final String group, final String artifact, final String version, final ScopesHelper scopesHelper) {
		final String externalId = group + ":" + artifact + ":" + version;
		if (dependenciesMap.containsKey(externalId)) {
			final BuildDependency existing = dependenciesMap.get(externalId);
			if (!existing.getScopes().contains(scope)) {
				existing.getScopes().add(scope);
			}
		} else {
			if (scopesHelper.shouldIncludeScope(scope)) {
				final BuildDependency buildDependency = new BuildDependency();
				buildDependency.setGroup(group);
				buildDependency.setArtifact(artifact);
				buildDependency.setVersion(version);

				final Set<String> scopeList = new HashSet<String>();
				scopeList.add(scope);
				buildDependency.setScopes(scopeList);
				dependenciesMap.put(externalId, buildDependency);
			}
		}
	}

	public void setPluginHelper(final PluginHelper pluginHelper) {
		this.pluginHelper = pluginHelper;
	}

}
