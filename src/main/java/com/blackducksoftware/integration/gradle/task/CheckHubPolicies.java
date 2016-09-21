package com.blackducksoftware.integration.gradle.task;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.api.policy.PolicyStatusEnum;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusItem;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.builder.ValidationResultEnum;
import com.blackducksoftware.integration.hub.builder.ValidationResults;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.global.GlobalFieldKey;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.logging.Slf4jIntLogger;
import com.blackducksoftware.integration.hub.polling.ScanStatusService;

public class CheckHubPolicies extends DefaultTask {
	private final Logger logger = LoggerFactory.getLogger(CheckHubPolicies.class);

	public String hubProjectName;
	public String hubProjectVersion;
	public String hubScanStartedTimeout;
	public String hubScanFinishedTimeout;
	public String hubUrl;
	public String hubUsername;
	public String hubPassword;
	public String hubTimeout;
	public String hubProxyHost;
	public String hubProxyPort;
	public String hubNoProxyHosts;
	public String hubProxyUsername;
	public String hubProxyPassword;

	@TaskAction
	public void checkHubPolicies() {
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
			final HubServerConfig config = results.getConstructedObject();
			try {
				final long scanStartedTimeout = NumberUtils.toLong(hubScanStartedTimeout);
				final long scanFinishedTimeout = NumberUtils.toLong(hubScanFinishedTimeout);
				final ScanStatusService scanStatusService = new ScanStatusService(config, new Slf4jIntLogger(logger));
				final PolicyStatusItem policyStatusItem = scanStatusService.checkPolicies(
						getProject().getGroup().toString(), hubProjectName, hubProjectVersion, scanStartedTimeout,
						scanFinishedTimeout);
				final String policyStatusMessage = getPolicyStatusMessage(policyStatusItem);
				System.out.println(policyStatusItem.getOverallStatus().toString());
				if (PolicyStatusEnum.IN_VIOLATION == policyStatusItem.getOverallStatus()) {
					throw new GradleException(policyStatusMessage);
				}
				logger.info(policyStatusMessage);
			} catch (IllegalArgumentException | URISyntaxException | BDRestException | EncryptionException | IOException
					| UnexpectedHubResponseException | InterruptedException | HubIntegrationException e) {
				logger.error("Error checking the policy status: " + e.getMessage());
			}
		} else {
			logErrors(results);
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
		logger.error("Invalid Hub Server Configuration skipping file deployment:");

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
