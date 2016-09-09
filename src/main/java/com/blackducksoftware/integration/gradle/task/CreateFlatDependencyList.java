package com.blackducksoftware.integration.gradle.task;

import java.io.IOException;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import com.blackducksoftware.integration.gradle.DependencyGatherer;
import com.blackducksoftware.integration.gradle.TaskHelper;

public class CreateFlatDependencyList extends DefaultTask {
	public TaskHelper taskHelper;
	public String outputDirectory;

	@TaskAction
	public void gatherDependencies() throws IOException {
		taskHelper.ensureReportsDirectoryExists(outputDirectory);
		final Project project = getProject();

		final DependencyGatherer dependencyGatherer = new DependencyGatherer(taskHelper, project);
		dependencyGatherer.createFlatOutput();
	}

}
