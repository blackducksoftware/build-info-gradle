package com.blackducksoftware.integration.gradle.task;

import java.io.IOException;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import com.blackducksoftware.integration.gradle.TaskHelper;

public class CreateFlatDependencyListTask extends DefaultTask {
    public TaskHelper taskHelper;

    public String outputDirectory;

    @TaskAction
    public void task() throws IOException {
        taskHelper.createFlatDependencyList(outputDirectory);
    }

}
