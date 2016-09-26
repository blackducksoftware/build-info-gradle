package com.blackducksoftware.integration.gradle.task;

import java.io.IOException;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import com.blackducksoftware.integration.gradle.TaskHelper;
import com.blackducksoftware.integration.hub.rest.RestConnection;

public class DeployHubOutputAndCheckPoliciesTask extends DefaultTask {
	public TaskHelper taskHelper;
	public String hubProjectName;
	public String hubProjectVersion;
	public long hubScanStartedTimeout;
	public long hubScanFinishedTimeout;
	public String hubUrl;
	public String hubUsername;
	public String hubPassword;
	public String hubTimeout;
	public String hubProxyHost;
	public String hubProxyPort;
	public String hubNoProxyHosts;
	public String hubProxyUsername;
	public String hubProxyPassword;
	public String outputDirectory;

	@TaskAction
	public void task() throws IOException {
		taskHelper.createHubOutput(hubProjectName, hubProjectVersion, outputDirectory);

		final RestConnection restConnection = taskHelper.getRestConnectionToHub(hubUrl, hubUsername, hubPassword,
				hubTimeout, hubProxyHost, hubProxyPort, hubNoProxyHosts, hubProxyUsername, hubProxyPassword);
		taskHelper.deployHubOutput(restConnection, outputDirectory);

		taskHelper.waitForHub(restConnection, hubProjectName, hubProjectVersion, hubScanStartedTimeout,
				hubScanFinishedTimeout);

		taskHelper.checkPolicies(restConnection, hubProjectName, hubProjectVersion);
	}

}
