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

import static com.blackducksoftware.integration.build.Constants.CREATE_HUB_OUTPUT_ERROR;
import static com.blackducksoftware.integration.build.Constants.DEPLOY_HUB_OUTPUT_ERROR;
import static com.blackducksoftware.integration.build.Constants.DEPLOY_HUB_OUTPUT_FINISHED;
import static com.blackducksoftware.integration.build.Constants.DEPLOY_HUB_OUTPUT_STARTING;

import java.io.IOException;
import java.net.URISyntaxException;

import org.gradle.api.GradleException;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

public class DeployHubOutputTask extends HubTask {
    @Override
    public void performTask() {
        logger.info(String.format(DEPLOY_HUB_OUTPUT_STARTING, getBdioFilename()));

        try {
            PLUGIN_HELPER.createHubOutput(getProject(), getHubProjectName(), getHubVersionName(), getOutputDirectory());
        } catch (final IOException e) {
            throw new GradleException(String.format(CREATE_HUB_OUTPUT_ERROR, e.getMessage()), e);
        }

        final HubServerConfig hubServerConfig = getHubServerConfigBuilder().build();
        RestConnection restConnection;
        try {
            restConnection = new CredentialsRestConnection(hubServerConfig);
            PLUGIN_HELPER.deployHubOutput(new Slf4jIntLogger(logger), restConnection, getOutputDirectory(),
                    getHubProjectName());
        } catch (IllegalArgumentException | URISyntaxException | BDRestException | EncryptionException | IOException
                | ResourceDoesNotExistException e) {
            throw new GradleException(String.format(DEPLOY_HUB_OUTPUT_ERROR, e.getMessage()), e);
        }

        logger.info(String.format(DEPLOY_HUB_OUTPUT_FINISHED, getBdioFilename()));
    }

}
