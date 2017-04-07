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

import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.BUILD_TOOL_STEP_CAMEL;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.gradle.task.BuildBomTask;

public class HubGradlePlugin implements Plugin<Project> {
    private final Logger logger = LoggerFactory.getLogger(HubGradlePlugin.class);

    @Override
    public void apply(final Project project) {
        if ("buildSrc".equals(project.getName())) {
            return;
        }

        if (project.getTasks().findByName(BUILD_TOOL_STEP_CAMEL) == null) {
            configureBuildBomTask(project);
        }
    }

    private void configureBuildBomTask(final Project project) {
        logger.info(String.format("Configuring %s task for project path: %s", BUILD_TOOL_STEP_CAMEL,
                project.getPath()));

        final BuildBomTask createFlatDependencyList = project.getTasks()
                .create(BUILD_TOOL_STEP_CAMEL, BuildBomTask.class);
        createFlatDependencyList.setDescription(
                "Can be used to create a flat list of unique dependencies, create the bdio file, deploy the bdio file to the Hub server, "
                        + "check the project's policies on the Hub, and create a Hub risk report.");
        createFlatDependencyList.setGroup("reporting");

        logger.info(String.format("Successfully configured %s", BUILD_TOOL_STEP_CAMEL));
    }

}
