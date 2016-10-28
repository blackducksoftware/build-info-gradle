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

import static com.blackducksoftware.integration.build.Constants.CHECK_POLICIES_ERROR;
import static com.blackducksoftware.integration.build.Constants.CHECK_POLICIES_FINISHED;
import static com.blackducksoftware.integration.build.Constants.CHECK_POLICIES_STARTING;

import java.io.IOException;
import java.net.URISyntaxException;

import org.gradle.api.GradleException;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.MissingUUIDException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.rest.RestConnection;

public class CheckPoliciesTask extends HubTask {
    @Override
    public void performTask() {
        logger.info(String.format(CHECK_POLICIES_STARTING, getBdioFilename()));

        final HubServerConfig hubServerConfig = getHubServerConfigBuilder().build();
        try {
            final RestConnection restConnection = new CredentialsRestConnection(hubServerConfig);
            final PolicyStatusItem policyStatusItem = PLUGIN_HELPER.checkPolicies(restConnection, getHubProjectName(),
                    getHubVersionName());
            handlePolicyStatusItem(policyStatusItem);
        } catch (IllegalArgumentException | URISyntaxException | BDRestException | EncryptionException | IOException
                | ProjectDoesNotExistException | HubIntegrationException | MissingUUIDException | UnexpectedHubResponseException e) {
            throw new GradleException(String.format(CHECK_POLICIES_ERROR, e.getMessage()), e);
        }

        logger.info(String.format(CHECK_POLICIES_FINISHED, getBdioFilename()));
    }

}
