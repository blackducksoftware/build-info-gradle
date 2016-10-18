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

import java.net.URISyntaxException;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;

public class DeployHubOutputTask extends HubTask {

    @Override
    @TaskAction
    public void performTask() {
        taskHelper.createHubOutput(hubProjectName, hubProjectVersion, outputDirectory);

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

        final HubServerConfig hubServerConfig = builder.build();
        RestConnection restConnection;
        try {
            restConnection = new RestConnection(hubServerConfig);
        } catch (IllegalArgumentException | URISyntaxException | BDRestException | EncryptionException e) {
            throw new GradleException("Could not connect to the Hub - please check the logs for configuration errors.");
        }

        taskHelper.deployHubOutput(restConnection, outputDirectory);
    }

}
