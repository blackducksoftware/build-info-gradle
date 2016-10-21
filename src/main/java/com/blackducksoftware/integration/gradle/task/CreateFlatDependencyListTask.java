package com.blackducksoftware.integration.gradle.task;

import static com.blackducksoftware.integration.build.Constants.CREATE_FLAT_DEPENDENCY_LIST_ERROR;
import static com.blackducksoftware.integration.build.Constants.CREATE_FLAT_DEPENDENCY_LIST_FINISHED;
import static com.blackducksoftware.integration.build.Constants.CREATE_FLAT_DEPENDENCY_LIST_STARTING;

import java.io.IOException;

import org.gradle.api.GradleException;

public class CreateFlatDependencyListTask extends HubTask {
    @Override
    public void performTask() {
        logger.info(String.format(CREATE_FLAT_DEPENDENCY_LIST_STARTING, getFlatFilename()));

        try {
            PLUGIN_HELPER.createFlatDependencyList(getProject(), getHubProjectName(), getHubVersionName(), getOutputDirectory());
        } catch (final IOException e) {
            throw new GradleException(String.format(CREATE_FLAT_DEPENDENCY_LIST_ERROR, e.getMessage()), e);
        }

        logger.info(String.format(CREATE_FLAT_DEPENDENCY_LIST_FINISHED, getFlatFilename()));
    }

}
