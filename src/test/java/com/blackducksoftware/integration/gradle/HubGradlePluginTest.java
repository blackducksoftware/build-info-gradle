/*******************************************************************************
 * Black Duck Software Suite SDK
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *******************************************************************************/
package com.blackducksoftware.integration.gradle;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;

import com.blackducksoftware.integration.gradle.task.BuildInfoCustomTask;

public class HubGradlePluginTest {
	@Test
	public void canApplyPluginToProject() {
		final Project project = ProjectBuilder.builder().build();
		assertFalse(project.getPlugins().hasPlugin(HubGradlePlugin.class));

		project.getPluginManager().apply(HubGradlePlugin.class);

		assertTrue(project.getPlugins().hasPlugin(HubGradlePlugin.class));

		final Task task = project.getTasks().getByName("buildInfoCustomTask");
		assertNotNull(task);
		assertTrue(task instanceof BuildInfoCustomTask);
	}

}
