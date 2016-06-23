package com.blackducksoftware.integration.gradle;

import org.gradle.api.Project;

import com.blackducksoftware.integration.build.bdio.Constants;

public class HubBdioManager {
	public String getBdioFilename(final Project project) {
		return project.getName() + Constants.BDIO_FILE_SUFFIX;
	}

}
