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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.bdio.simple.model.DependencyNode;
import com.blackducksoftware.integration.hub.bdio.simple.model.externalid.MavenExternalId;

public class DependencyGatherer {
    private final Logger logger = LoggerFactory.getLogger(DependencyGatherer.class);

    final Map<String, DependencyNode> visitedMap = new HashMap<>();

    final String includedConfigurations;

    final Set<String> excludedModules = new HashSet<>();

    public DependencyGatherer(final String includedConfigurations, final String excludedModules) {
        this.includedConfigurations = includedConfigurations;
        if (StringUtils.isNotBlank(excludedModules)) {
            final String[] pieces = excludedModules.split(",");
            for (final String piece : pieces) {
                if (StringUtils.isNotBlank(piece)) {
                    this.excludedModules.add(piece);
                }
            }
        }
    }

    public DependencyNode getFullyPopulatedRootNode(final Project project, final String hubProjectName, final String hubProjectVersion) {
        logger.info("creating the dependency graph");
        final String groupId = project.getGroup().toString();
        final String artifactId = project.getName();
        final String version = hubProjectVersion;
        final MavenExternalId projectGav = new MavenExternalId(groupId, artifactId, version);

        final Set<DependencyNode> children = new LinkedHashSet<>();
        final DependencyNode root = new DependencyNode(hubProjectName, hubProjectVersion, projectGav, children);
        for (final Project childProject : project.getAllprojects()) {
            if (!excludedModules.contains(childProject.getName())) {
                getProjectDependencies(childProject, children);
            }
        }

        return root;
    }

    private void getProjectDependencies(final Project project, final Set<DependencyNode> children) {
        final ScopesHelper scopesHelper = new ScopesHelper(project, includedConfigurations);
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
        final MavenExternalId gav = createGavFromDependencyNode(resolvedDependency);
        final String gavKey = gav.toString();

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
            if (logger.isDebugEnabled()) {
                logger.debug(buffer + gavKey + " (created) config: " + configuration);
            }
            final Set<DependencyNode> children = new LinkedHashSet<>();
            final DependencyNode dependencyNode = new DependencyNode(gav, children);
            visitedMap.put(gavKey, dependencyNode);
            for (final ResolvedDependency child : resolvedDependency.getChildren()) {
                if (child.getConfiguration().equals(configuration)) {
                    children.add(createCommonDependencyNode(child, level + 1, configuration));
                }
            }
            return dependencyNode;
        }
    }

    private MavenExternalId createGavFromDependencyNode(final ResolvedDependency resolvedDependency) {
        final String groupId = resolvedDependency.getModuleGroup();
        final String artifactId = resolvedDependency.getModuleName();
        final String version = resolvedDependency.getModuleVersion();

        final MavenExternalId gav = new MavenExternalId(groupId, artifactId, version);
        return gav;
    }

}
