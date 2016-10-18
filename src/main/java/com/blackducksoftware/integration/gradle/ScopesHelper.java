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
import java.util.Map;
import java.util.Set;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;

public class ScopesHelper {
    public static final String INCLUDED_CONFIGURATIONS_PROPERTY = "IncludedConfigurations";

    private final Project project;

    private Set<String> allAvailableScopes;

    private Set<String> requestedScopes;

    private final Map<String, Boolean> shouldIncludeScopeMap = new HashMap<>();

    public ScopesHelper(final Project project) {
        this.project = project;
        populateRequestedScopes();
        populateAllAvailableScopes();
        populateShouldIncludeScopeMap();
    }

    public boolean shouldIncludeScope(final String scope) {
        // include all scopes if none were requested
        if (requestedScopes == null) {
            return true;
        }

        if (requestedScopes != null && requestedScopes.size() == 0) {
            return false;
        }

        if (scope == null || scope.trim().length() == 0) {
            return false;
        }

        final String scopeKey = scope.trim().toUpperCase();
        if (shouldIncludeScopeMap.containsKey(scopeKey)) {
            return shouldIncludeScopeMap.get(scopeKey);
        } else {
            return false;
        }
    }

    public boolean shouldIncludeConfigurationInDependencyGraph(final String configuration) {
        // if none were specifically requested, only include compile
        if (requestedScopes == null) {
            return "compile".equals(configuration);
        } else {
            return shouldIncludeScope(configuration);
        }
    }

    private void populateAllAvailableScopes() {
        allAvailableScopes = new HashSet<>();

        final ConfigurationContainer configurationContainer = project.getConfigurations();
        for (final Configuration configuration : configurationContainer) {
            allAvailableScopes.add(configuration.getName());
        }
    }

    private void populateRequestedScopes() {
        final String requestedScopesString = System.getProperty(INCLUDED_CONFIGURATIONS_PROPERTY);
        if (requestedScopesString != null && requestedScopesString.trim().length() > 0) {
            requestedScopes = new HashSet<>();
            if (requestedScopesString.contains(",")) {
                final String[] pieces = requestedScopesString.split(",");
                for (final String piece : pieces) {
                    requestedScopes.add(piece.trim());
                }
            } else {
                requestedScopes.add(requestedScopesString.trim());
            }
        }
    }

    private void populateShouldIncludeScopeMap() {
        if (requestedScopes != null) {
            for (final String scope : requestedScopes) {
                shouldIncludeScopeMap.put(scope.trim().toUpperCase(), Boolean.TRUE);
            }
        }
    }

}
