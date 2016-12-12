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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.build.DependencyNode;
import com.blackducksoftware.integration.build.Gav;

public class DependencyGatherer {
    private final Logger logger = LoggerFactory.getLogger(DependencyGatherer.class);

    final Map<String, DependencyNode> visitedMap = new HashMap<>();

    final String includedConfigurations;

    public DependencyGatherer(String includedConfigurations) {
        this.includedConfigurations = includedConfigurations;
    }

    public DependencyNode getFullyPopulatedRootNode(Project project, String hubProjectName, String hubProjectVersion) {
        logger.info("creating the dependency graph");
        final String groupId = project.getGroup().toString();
        final String artifactId = hubProjectName;
        final String version = hubProjectVersion;
        final Gav projectGav = new Gav(groupId, artifactId, version);

        final List<DependencyNode> children = new ArrayList<>();
        final DependencyNode root = new DependencyNode(projectGav, children);
        for (final Project childProject : project.getAllprojects()) {
            getProjectDependencies(childProject, children);
        }

        return root;
    }

    private void getProjectDependencies(final Project project, final List<DependencyNode> children) {
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
        final Gav gav = createGavFromDependencyNode(resolvedDependency);
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

    private Gav createGavFromDependencyNode(final ResolvedDependency resolvedDependency) {
        final String groupId = resolvedDependency.getModuleGroup();
        final String artifactId = resolvedDependency.getModuleName();
        final String version = resolvedDependency.getModuleVersion();

        final Gav gav = new Gav(groupId, artifactId, version);
        return gav;
    }

}
