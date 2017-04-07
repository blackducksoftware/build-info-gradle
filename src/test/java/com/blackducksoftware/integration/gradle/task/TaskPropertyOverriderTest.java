package com.blackducksoftware.integration.gradle.task;

import static com.blackducksoftware.integration.hub.buildtool.BuildToolConstants.BUILD_TOOL_STEP_CAMEL;
import static org.junit.Assert.assertNotEquals;

import java.util.HashMap;
import java.util.Map;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

public class TaskPropertyOverriderTest {
    @Test
    public void testSettingStringProperty() {
        final Project project = ProjectBuilder.builder().build();
        final BuildBomTask buildBomTask = project.getTasks().create(BUILD_TOOL_STEP_CAMEL, BuildBomTask.class);

        final Map<String, String> properties = new HashMap<>();
        final String initialValue = buildBomTask.getHubUrl();
        properties.put("blackduck.hubUrl", initialValue + " test");

        final TaskPropertyOverrider taskPropertyOverrider = new TaskPropertyOverrider(properties);
        taskPropertyOverrider.overrideProperties(buildBomTask);
        assertNotEquals(initialValue, buildBomTask.getHubUrl());
    }

    @Test
    public void testSettingBooleanProperty() {
        final Project project = ProjectBuilder.builder().build();
        final BuildBomTask buildBomTask = project.getTasks().create(BUILD_TOOL_STEP_CAMEL, BuildBomTask.class);

        final Map<String, String> properties = new HashMap<>();
        final boolean initialValue = buildBomTask.getCheckPolicies();
        properties.put("blackduck.checkPolicies", Boolean.toString(!initialValue));

        final TaskPropertyOverrider taskPropertyOverrider = new TaskPropertyOverrider(properties);
        taskPropertyOverrider.overrideProperties(buildBomTask);
        assertNotEquals(initialValue, buildBomTask.getCheckPolicies());
    }

    @Test
    public void testSettingIntProperty() {
        final Project project = ProjectBuilder.builder().build();
        final BuildBomTask buildBomTask = project.getTasks().create(BUILD_TOOL_STEP_CAMEL, BuildBomTask.class);

        final Map<String, String> properties = new HashMap<>();
        final int initialValue = buildBomTask.getHubTimeout();
        properties.put("blackduck.hubTimeout", Integer.toString(initialValue + 3));

        final TaskPropertyOverrider taskPropertyOverrider = new TaskPropertyOverrider(properties);
        taskPropertyOverrider.overrideProperties(buildBomTask);
        assertNotEquals(initialValue, buildBomTask.getHubTimeout());
    }

    @Test
    public void testSettingLongProperty() {
        final Project project = ProjectBuilder.builder().build();
        final BuildBomTask buildBomTask = project.getTasks().create(BUILD_TOOL_STEP_CAMEL, BuildBomTask.class);

        final Map<String, String> properties = new HashMap<>();
        final long initialValue = buildBomTask.getHubScanTimeout();
        properties.put("blackduck.hubScanTimeout", Long.toString(initialValue + 3));

        final TaskPropertyOverrider taskPropertyOverrider = new TaskPropertyOverrider(properties);
        taskPropertyOverrider.overrideProperties(buildBomTask);
        assertNotEquals(initialValue, buildBomTask.getHubScanTimeout());
    }

}
