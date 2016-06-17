package com.blackducksoftware.integration.gradle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;

public class ScopesManager {
	private final Project project;
	private Set<String> allAvailableScopes;
	private Set<String> requestedScopes;
	private final Map<String, Boolean> shouldIncludeScopeMap = new HashMap<>();

	public ScopesManager(final Project project) {
		this.project = project;
		populateRequestedScopes();
		populateAllAvailableScopes();
		populateShouldIncludeScopeMap();
	}

	public boolean shouldIncludeScope(final String scope) {
		// include all scopes if none were requested
		if (requestedScopes == null) {
			return true;
		}

		if (requestedScopes != null && requestedScopes.size() == 0) {
			return false;
		}

		if (scope == null || scope.trim().length() == 0) {
			return false;
		}

		final String scopeKey = scope.trim().toUpperCase();
		if (shouldIncludeScopeMap.containsKey(scopeKey)) {
			return shouldIncludeScopeMap.get(scopeKey);
		} else {
			return false;
		}
	}

	private void populateAllAvailableScopes() {
		allAvailableScopes = new HashSet<>();

		final ConfigurationContainer configurationContainer = project.getConfigurations();
		for (final Configuration configuration : configurationContainer) {
			allAvailableScopes.add(configuration.getName());
		}
	}

	private void populateRequestedScopes() {
		final String requestedScopesString = System.getProperty(GradleUtil.INCLUDED_CONFIGURATIONS_PROPERTY);
		if (requestedScopesString != null && requestedScopesString.trim().length() > 0) {
			requestedScopes = new HashSet<String>();
			if (requestedScopesString.contains(",")) {
				final String[] pieces = requestedScopesString.split(",");
				for (final String piece : pieces) {
					requestedScopes.add(piece.trim());
				}
			} else {
				requestedScopes.add(requestedScopesString.trim());
			}
		}
	}

	private void populateShouldIncludeScopeMap() {
		if (null != requestedScopes) {
			for (final String scope : requestedScopes) {
				shouldIncludeScopeMap.put(scope.trim().toUpperCase(), Boolean.TRUE);
			}
		}
	}

}
