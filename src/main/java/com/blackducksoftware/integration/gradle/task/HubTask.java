package com.blackducksoftware.integration.gradle.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import com.blackducksoftware.integration.gradle.TaskHelper;

public abstract class HubTask extends DefaultTask {
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

    public String outputDirectory;

    @TaskAction
    public void task() {
    }

    public abstract void performTask();

}
