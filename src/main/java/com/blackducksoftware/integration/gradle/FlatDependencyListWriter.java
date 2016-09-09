package com.blackducksoftware.integration.gradle;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.build.bdio.DependencyNode;

public class FlatDependencyListWriter {
	private final Logger logger = LoggerFactory.getLogger(FlatDependencyListWriter.class);

	private final TaskHelper taskHelper;

	public FlatDependencyListWriter(final TaskHelper taskHelper) {
		this.taskHelper = taskHelper;
	}

	public void write(final OutputStream outputStream, final DependencyNode root) throws IOException {
		final Set<String> gavs = new HashSet<>();
		addAllGavs(gavs, root);

		final List<String> gavList = new ArrayList<>(gavs);
		Collections.sort(gavList);

		for (final String gav : gavList) {
			logger.info(gav);
			IOUtils.write(gav, outputStream, "UTF8");
			IOUtils.write("\n", outputStream, "UTF8");
		}
	}

	private void addAllGavs(final Set<String> gavs, final DependencyNode node) {
		final String gavString = taskHelper.getGavString(node.getGav());
		gavs.add(gavString);

		for (final DependencyNode child : node.getChildren()) {
			addAllGavs(gavs, child);
		}
	}

}
