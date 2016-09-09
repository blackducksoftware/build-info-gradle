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

import java.io.IOException;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import com.blackducksoftware.integration.gradle.HubBdioDeployer;
import com.blackducksoftware.integration.gradle.TaskHelper;

public class DeployHubOutput extends DefaultTask {
	public TaskHelper taskHelper;
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
	public void deployBdioFileToHub() throws IOException {
		taskHelper.ensureReportsDirectoryExists(outputDirectory);
		final Project project = getProject();

		final HubBdioDeployer hubBdioDeployer = new HubBdioDeployer(taskHelper, project, hubUrl, hubUsername,
				hubPassword, hubTimeout, hubProxyHost, hubProxyPort, hubNoProxyHosts, hubProxyUsername,
				hubProxyPassword);
		hubBdioDeployer.deployToHub();
	}

}
