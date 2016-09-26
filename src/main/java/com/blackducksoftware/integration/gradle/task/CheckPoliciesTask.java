package com.blackducksoftware.integration.gradle.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import com.blackducksoftware.integration.gradle.TaskHelper;
import com.blackducksoftware.integration.hub.rest.RestConnection;

public class CheckPoliciesTask extends DefaultTask {
	public TaskHelper taskHelper;
	public String hubProjectName;
	public String hubProjectVersion;
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
	public void task() {
		final RestConnection restConnection = taskHelper.getRestConnectionToHub(hubUrl, hubUsername, hubPassword,
				hubTimeout, hubProxyHost, hubProxyPort, hubNoProxyHosts, hubProxyUsername, hubProxyPassword);

		taskHelper.checkPolicies(restConnection, hubProjectName, hubProjectVersion);
	}

}
