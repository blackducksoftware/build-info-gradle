package com.blackducksoftware.integration.gradle.task;

import java.io.IOException;
import java.net.URISyntaxException;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.gradle.TaskHelper;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
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

        taskHelper.waitForHub(restConnection, hubProjectName, hubProjectVersion, hubScanStartedTimeout,
                hubScanFinishedTimeout);

        taskHelper.checkPolicies(restConnection, hubProjectName, hubProjectVersion);
    }

}
