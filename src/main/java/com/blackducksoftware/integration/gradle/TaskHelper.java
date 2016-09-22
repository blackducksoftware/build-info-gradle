package com.blackducksoftware.integration.gradle;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.restlet.data.MediaType;
import org.restlet.representation.FileRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.build.bdio.Constants;
import com.blackducksoftware.integration.build.bdio.Gav;
import com.blackducksoftware.integration.hub.api.CodeLocationRestService;
import com.blackducksoftware.integration.hub.api.PolicyStatusRestService;
import com.blackducksoftware.integration.hub.api.ProjectRestService;
import com.blackducksoftware.integration.hub.api.ProjectVersionRestService;
import com.blackducksoftware.integration.hub.api.ScanSummaryRestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusEnum;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusItem;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.builder.ValidationResultEnum;
import com.blackducksoftware.integration.hub.builder.ValidationResults;
import com.blackducksoftware.integration.hub.dataservices.policystatus.PolicyStatusDataService;
import com.blackducksoftware.integration.hub.dataservices.scan.ScanStatusDataService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.MissingUUIDException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.global.GlobalFieldKey;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.logging.Slf4jIntLogger;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

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

	public File getFlatFile(final Project project) {
		return new File(blackDuckReports, project.getName() + "_flat.txt");
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

	public RestConnection getRestConnectionToHub(final String hubUrl, final String hubUsername,
			final String hubPassword, final String hubTimeout, final String hubProxyHost, final String hubProxyPort,
			final String hubNoProxyHosts, final String hubProxyUsername, final String hubProxyPassword) {
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

		final ValidationResults<GlobalFieldKey, HubServerConfig> results = builder.build();
		if (results.isSuccess()) {
			final HubServerConfig hubServerConfig = results.getConstructedObject();
			final RestConnection restConnection = new RestConnection(hubServerConfig.getHubUrl().toString());
			final HubProxyInfo proxyInfo = hubServerConfig.getProxyInfo();
			if (proxyInfo.shouldUseProxyForUrl(hubServerConfig.getHubUrl())) {
				restConnection.setProxyProperties(proxyInfo);
			}

			try {
				restConnection.setCookies(hubServerConfig.getGlobalCredentials().getUsername(),
						hubServerConfig.getGlobalCredentials().getDecryptedPassword());
				return restConnection;
			} catch (final Exception e) {
				throw new GradleException(
						"Could not connect to the Hub - please check the logs for additional configuration errors: "
								+ e.getMessage());
			}
		} else {
			logErrors(results);
			throw new GradleException("Could not connect to the Hub - please check the logs for configuration errors.");
		}
	}

	public void deployToHub(final RestConnection restConnection) {
		logger.info("Deploying Black Duck I/O output");
		final File file = getBdioFile(project);

		final List<String> urlSegments = new ArrayList<>();
		urlSegments.add("api");
		urlSegments.add("v1");
		urlSegments.add("bom-import");
		final Set<SimpleEntry<String, String>> queryParameters = new HashSet<>();
		final FileRepresentation content = new FileRepresentation(file, new MediaType(Constants.BDIO_FILE_MEDIA_TYPE));
		try {
			restConnection.httpPostFromRelativeUrl(urlSegments, queryParameters, content);
		} catch (IOException | ResourceDoesNotExistException | URISyntaxException | BDRestException e) {
			throw new GradleException(
					String.format("Could not deploy the Black Duck I/O file to the Hub: %s", e.getMessage()));
		}

		logger.info(String.format("Deployed Black Duck I/O file: %s to Hub server: %s", file.getAbsolutePath(),
				restConnection.getBaseUrl()));
	}

	public void waitForScans(final RestConnection restConnection, final String hubProjectName,
			final String hubProjectVersion, final long scanStartedTimeoutInMilliseconds,
			final long scanFinishedTimeoutInMilliseconds) {
		final Gson gson = new Gson();
		final JsonParser jsonParser = new JsonParser();

		final ProjectRestService projectRestService = new ProjectRestService(restConnection, gson, jsonParser);
		final ProjectVersionRestService projectVersionRestService = new ProjectVersionRestService(restConnection, gson,
				jsonParser);
		final CodeLocationRestService codeLocationRestService = new CodeLocationRestService(restConnection, gson,
				jsonParser);
		final ScanSummaryRestService scanSummaryRestService = new ScanSummaryRestService(restConnection, gson,
				jsonParser);

		final ScanStatusDataService scanStatusDataService = new ScanStatusDataService(restConnection, gson, jsonParser,
				projectRestService, projectVersionRestService, codeLocationRestService, scanSummaryRestService);
		final String name = getHubProjectName(hubProjectName);
		final String version = getHubVersion(hubProjectVersion);

		try {
			scanStatusDataService.assertBomImportScanStartedThenFinished(name, version,
					scanStartedTimeoutInMilliseconds, scanFinishedTimeoutInMilliseconds, new Slf4jIntLogger(logger));
		} catch (IOException | BDRestException | URISyntaxException | ProjectDoesNotExistException
				| MissingUUIDException | UnexpectedHubResponseException | HubIntegrationException
				| InterruptedException e) {
			logger.error("There was an error waiting for the scans: " + e.getMessage());
		}
	}

	public void checkPolicies(final RestConnection restConnection, final String hubProjectName,
			final String hubProjectVersion) {
		final Gson gson = new Gson();
		final JsonParser jsonParser = new JsonParser();

		final ProjectRestService projectRestService = new ProjectRestService(restConnection, gson, jsonParser);
		final ProjectVersionRestService projectVersionRestService = new ProjectVersionRestService(restConnection, gson,
				jsonParser);
		final PolicyStatusRestService policyStatusRestService = new PolicyStatusRestService(restConnection, gson,
				jsonParser);
		final PolicyStatusDataService policyStatusDataService = new PolicyStatusDataService(restConnection, gson,
				jsonParser, projectRestService, projectVersionRestService, policyStatusRestService);

		final String name = getHubProjectName(hubProjectName);
		final String version = getHubVersion(hubProjectVersion);
		try {
			final PolicyStatusItem policyStatusItem = policyStatusDataService.getPolicyStatusForProjectAndVersion(name,
					version);
			final String policyStatusMessage = getPolicyStatusMessage(policyStatusItem);
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

	private String getPolicyStatusMessage(final PolicyStatusItem policyStatusItem) {
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("The Hub found: ");
		stringBuilder.append(policyStatusItem.getCountInViolation().getValue());
		stringBuilder.append(" components in violation, ");
		stringBuilder.append(policyStatusItem.getCountInViolationOverridden().getValue());
		stringBuilder.append(" components in violation, but overridden, and ");
		stringBuilder.append(policyStatusItem.getCountNotInViolation().getValue());
		stringBuilder.append(" components not in violation.");
		return stringBuilder.toString();
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
