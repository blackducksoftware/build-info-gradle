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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

import com.blackducksoftware.integration.gradle.task.BuildBOMTask;
import com.blackducksoftware.integration.hub.buildtool.BuildToolConstants;

public class HubGradlePluginTest {
    @Test
    public void canApplyPluginToProject() {
        final Project project = ProjectBuilder.builder().build();
        assertFalse(project.getPlugins().hasPlugin(HubGradlePlugin.class));

        project.getPluginManager().apply(HubGradlePlugin.class);

        assertTrue(project.getPlugins().hasPlugin(HubGradlePlugin.class));

        final Task task = project.getTasks().getByName(BuildToolConstants.BUILD_TOOL_STEP_CAMEL);
        assertNotNull(task);
        assertTrue(task instanceof BuildBOMTask);
    }
}
