package com.blackducksoftware.integration.gradle;

import java.io.File;

import org.gradle.api.Project;

import com.blackducksoftware.integration.build.bdio.Constants;

public class BdioHelper {
	private final Project project;
	private File blackDuckReports;

	public BdioHelper(final Project project) {
		this.project = project;
	}

	public boolean ensureReportsDirectoryExists() {
		File reportsDirectory;
		if (project.getRootProject() != null) {
			final Project rootProject = project.getRootProject();
			reportsDirectory = new File(rootProject.getBuildDir(), "reports");
		} else {
			reportsDirectory = new File(project.getBuildDir(), "reports");
		}

		final File blackDuckReports = new File(reportsDirectory, "blackduck");
		return blackDuckReports.mkdirs();
	}

	public String getGAV(final String group, final String artifact, final String version) {
		final StringBuilder gavBuilder = new StringBuilder();
		gavBuilder.append(group);
		gavBuilder.append(":");
		gavBuilder.append(artifact);
		gavBuilder.append(":");
		gavBuilder.append(version);
		return gavBuilder.toString();
	}

	public File getBdioFile(final Project project) {
		return new File(blackDuckReports, project.getName() + Constants.BDIO_FILE_SUFFIX);
	}

}
