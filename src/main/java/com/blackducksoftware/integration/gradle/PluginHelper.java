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
import java.io.IOException;
import java.net.URISyntaxException;

import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.build.Constants;
import com.blackducksoftware.integration.build.DependencyNode;
import com.blackducksoftware.integration.build.utils.BdioDependencyWriter;
import com.blackducksoftware.integration.build.utils.FlatDependencyListWriter;
import com.blackducksoftware.integration.hub.api.bom.BomImportRestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusItem;
import com.blackducksoftware.integration.hub.dataservices.DataServicesFactory;
import com.blackducksoftware.integration.hub.dataservices.policystatus.PolicyStatusDataService;
import com.blackducksoftware.integration.hub.dataservices.scan.ScanStatusDataService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.MissingUUIDException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

public class PluginHelper {
    private final Logger logger = LoggerFactory.getLogger(PluginHelper.class);

    public void createFlatDependencyList(Project project, final String hubProjectName, final String hubProjectVersion,
            final File outputDirectory) throws IOException {
        final DependencyGatherer dependencyGatherer = new DependencyGatherer();
        DependencyNode rootNode = dependencyGatherer.getFullyPopulatedRootNode(project, hubProjectName, hubProjectVersion);

        final FlatDependencyListWriter flatDependencyListWriter = new FlatDependencyListWriter();
        flatDependencyListWriter.write(outputDirectory, hubProjectName, rootNode);
    }

    public void createHubOutput(Project project, final String hubProjectName, final String hubProjectVersion,
            final File outputDirectory) throws IOException {
        final DependencyGatherer dependencyGatherer = new DependencyGatherer();
        DependencyNode rootNode = dependencyGatherer.getFullyPopulatedRootNode(project, hubProjectName, hubProjectVersion);

        final BdioDependencyWriter bdioDependencyWriter = new BdioDependencyWriter();
        bdioDependencyWriter.write(outputDirectory, hubProjectName, rootNode);
    }

    public void deployHubOutput(final Slf4jIntLogger logger, final RestConnection restConnection,
            final File outputDirectory, final String hubProjectName) throws IOException, ResourceDoesNotExistException, URISyntaxException, BDRestException {
        final DataServicesFactory dataServicesFactory = new DataServicesFactory(restConnection);
        final BomImportRestService bomImportRestService = dataServicesFactory.getBomImportRestService();

        String filename = BdioDependencyWriter.getFilename(hubProjectName);
        final File file = new File(outputDirectory, filename);
        bomImportRestService.importBomFile(file, Constants.BDIO_FILE_MEDIA_TYPE);

        logger.info(String.format(Constants.UPLOAD_FILE_MESSAGE, file, restConnection.getBaseUrl()));
    }

    public void waitForHub(final RestConnection restConnection, final String hubProjectName,
            final String hubProjectVersion, final long scanStartedTimeout, final long scanFinishedTimeout) {
        final DataServicesFactory dataServicesFactory = new DataServicesFactory(restConnection);
        final ScanStatusDataService scanStatusDataService = dataServicesFactory.createScanStatusDataService();
        try {
            scanStatusDataService.assertBomImportScanStartedThenFinished(hubProjectName, hubProjectVersion,
                    scanStartedTimeout * 1000, scanFinishedTimeout * 1000, new Slf4jIntLogger(logger));
        } catch (IOException | BDRestException | URISyntaxException | ProjectDoesNotExistException | UnexpectedHubResponseException
                | HubIntegrationException | InterruptedException e) {
            logger.error(String.format(Constants.SCAN_ERROR_MESSAGE, e.getMessage()), e);
        }
    }

    public PolicyStatusItem checkPolicies(final RestConnection restConnection, final String hubProjectName,
            final String hubProjectVersion) throws IOException, URISyntaxException, BDRestException, ProjectDoesNotExistException, HubIntegrationException,
            MissingUUIDException, UnexpectedHubResponseException {
        final DataServicesFactory dataServicesFactory = new DataServicesFactory(restConnection);
        final PolicyStatusDataService policyStatusDataService = dataServicesFactory.createPolicyStatusDataService();

        final PolicyStatusItem policyStatusItem = policyStatusDataService
                .getPolicyStatusForProjectAndVersion(hubProjectName, hubProjectVersion);
        return policyStatusItem;
    }

}
