package com.blackducksoftware.integration.gradle;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.build.Constants;
import com.blackducksoftware.integration.build.DependencyNode;
import com.blackducksoftware.integration.build.Gav;
import com.blackducksoftware.integration.build.utils.FlatDependencyListWriter;
import com.blackducksoftware.integration.builder.ValidationResultEnum;
import com.blackducksoftware.integration.builder.ValidationResults;
import com.blackducksoftware.integration.hub.api.bom.BomImportRestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusEnum;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusItem;
import com.blackducksoftware.integration.hub.dataservices.DataServicesFactory;
import com.blackducksoftware.integration.hub.dataservices.policystatus.PolicyStatusDataService;
import com.blackducksoftware.integration.hub.dataservices.policystatus.PolicyStatusDescription;
import com.blackducksoftware.integration.hub.dataservices.scan.ScanStatusDataService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.MissingUUIDException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.global.GlobalFieldKey;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.log.Slf4jIntLogger;

public class TaskHelper {
    private final Logger logger = LoggerFactory.getLogger(TaskHelper.class);

    private final Project project;

    private File blackDuckReports;

    public TaskHelper(final Project project) {
        this.project = project;
    }

    public boolean ensureReportsDirectoryExists(final String userSpecifiedDirectory) {
        if (StringUtils.isNotBlank(userSpecifiedDirectory)) {
            blackDuckReports = new File(userSpecifiedDirectory);
        } else {
            File reportsDirectory;
            if (project.getRootProject() != null) {
                final Project rootProject = project.getRootProject();
                reportsDirectory = new File(rootProject.getBuildDir(), "reports");
            } else {
                reportsDirectory = new File(project.getBuildDir(), "reports");
            }

            blackDuckReports = new File(reportsDirectory, "blackduck");
        }

        return blackDuckReports.mkdirs();
    }

    public String getGAV(final String group, final String artifact, final String version) {
        final Gav gav = new Gav(group, artifact, version);
        gav.toString();
        return getGavString(gav);
    }

    public String getGavString(final Gav gav) {
        final StringBuilder gavBuilder = new StringBuilder();
        gavBuilder.append(gav.getGroupId());
        gavBuilder.append(":");
        gavBuilder.append(gav.getArtifactId());
        gavBuilder.append(":");
        gavBuilder.append(gav.getVersion());
        return gavBuilder.toString();
    }

    public File getBdioFile(final Project project) {
        return new File(blackDuckReports, project.getName() + Constants.BDIO_FILE_SUFFIX);
    }

    public String getHubProjectName(final String hubProjectName) {
        if (StringUtils.isNotBlank(hubProjectName)) {
            return hubProjectName;
        } else {
            return project.getName();
        }
    }

    public String getHubVersion(final String hubProjectVersion) {
        if (StringUtils.isNotBlank(hubProjectVersion)) {
            return hubProjectVersion;
        } else {
            return project.getVersion().toString();
        }
    }

    public void createFlatDependencyList(final String outputDirectory) throws IOException {
        ensureReportsDirectoryExists(outputDirectory);

        final DependencyGatherer dependencyGatherer = new DependencyGatherer(this, project);
        final DependencyNode rootNode = dependencyGatherer.getFullyPopulatedRootNode();

        final FlatDependencyListWriter flatDependencyListWriter = new FlatDependencyListWriter();
        flatDependencyListWriter.write(blackDuckReports, project.getName() + Constants.FLAT_FILE_SUFFIX, rootNode);
    }

    public void createHubOutput(final String hubProjectName, final String hubProjectVersion,
            final String outputDirectory) throws IOException {
        ensureReportsDirectoryExists(outputDirectory);

        final DependencyGatherer dependencyGatherer = new DependencyGatherer(this, project, hubProjectName,
                hubProjectVersion);
        dependencyGatherer.createBdioOutput();
    }

    public void deployHubOutput(final RestConnection restConnection, final String outputDirectory) {
        logger.info("Deploying Black Duck I/O output");
        ensureReportsDirectoryExists(outputDirectory);

        final File file = getBdioFile(project);

        final DataServicesFactory dataServicesFactory = new DataServicesFactory(restConnection);
        final BomImportRestService bomImportRestService = dataServicesFactory.getBomImportRestService();

        try {
            bomImportRestService.importBomFile(file, Constants.BDIO_FILE_MEDIA_TYPE);
        } catch (IOException | ResourceDoesNotExistException | URISyntaxException | BDRestException e) {
            throw new GradleException(
                    String.format("Could not deploy the Black Duck I/O file to the Hub: %s", e.getMessage()));
        }

        logger.info(String.format("Deployed Black Duck I/O file: %s to Hub server: %s", file.getAbsolutePath(),
                restConnection.getBaseUrl()));
    }

    public void waitForHub(final RestConnection restConnection, final String hubProjectName,
            final String hubProjectVersion, final long scanStartedTimeoutInMilliseconds,
            final long scanFinishedTimeoutInMilliseconds) {
        final String name = getHubProjectName(hubProjectName);
        final String version = getHubVersion(hubProjectVersion);

        final DataServicesFactory dataServicesFactory = new DataServicesFactory(restConnection);
        final ScanStatusDataService scanStatusDataService = dataServicesFactory.createScanStatusDataService();

        try {
            scanStatusDataService.assertBomImportScanStartedThenFinished(name, version,
                    scanStartedTimeoutInMilliseconds, scanFinishedTimeoutInMilliseconds, new Slf4jIntLogger(logger));
        } catch (IOException | BDRestException | URISyntaxException | ProjectDoesNotExistException
                | UnexpectedHubResponseException | HubIntegrationException | InterruptedException e) {
            logger.error("There was an error waiting for the scans: " + e.getMessage());
        }
    }

    public void checkPolicies(final RestConnection restConnection, final String hubProjectName,
            final String hubProjectVersion) {
        final DataServicesFactory dataServicesFactory = new DataServicesFactory(restConnection);
        final PolicyStatusDataService policyStatusDataService = dataServicesFactory.createPolicyStatusDataService();

        final String name = getHubProjectName(hubProjectName);
        final String version = getHubVersion(hubProjectVersion);
        try {
            final PolicyStatusItem policyStatusItem = policyStatusDataService.getPolicyStatusForProjectAndVersion(name,
                    version);
            final PolicyStatusDescription policyStatusDescription = new PolicyStatusDescription(policyStatusItem);
            final String policyStatusMessage = policyStatusDescription.getPolicyStatusMessage();
            if (PolicyStatusEnum.IN_VIOLATION == policyStatusItem.getOverallStatus()) {
                throw new GradleException(policyStatusMessage);
            }
            logger.info(policyStatusMessage);
        } catch (IOException | URISyntaxException | BDRestException | ProjectDoesNotExistException
                | HubIntegrationException | MissingUUIDException e) {
            throw new GradleException(
                    String.format("Could not check the Hub policies for project %s and version %s: %s", name, version,
                            e.getMessage()));
        }
    }

    private void logErrors(final ValidationResults<GlobalFieldKey, HubServerConfig> results) {
        logger.error("Invalid Hub Server Configuration: ");

        final Set<GlobalFieldKey> keySet = results.getResultMap().keySet();
        for (final GlobalFieldKey key : keySet) {
            if (results.hasWarnings(key)) {
                logger.error(results.getResultString(key, ValidationResultEnum.WARN));
            }
            if (results.hasErrors(key)) {
                logger.error(results.getResultString(key, ValidationResultEnum.ERROR));
            }
        }
    }

}
