package com.blackducksoftware.integration.gradle.task;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskPropertyOverrider {
    private final Logger logger = LoggerFactory.getLogger(TaskPropertyOverrider.class);

    private final Map<String, String> environmentVariables;

    private final Properties systemProperties;

    private final Map<String, ?> projectProperties;

    public TaskPropertyOverrider(final Map<String, ?> projectProperties) {
        this.environmentVariables = System.getenv();
        this.systemProperties = System.getProperties();
        this.projectProperties = projectProperties;
    }

    public void overrideProperties(final BuildBomTask buildBomTask) {
        // the order of applying the properties here matters - the environment properties can be overridden by the
        // project properties, which can be overridden by the system properties.
        final Map<String, Object> finalProperties = new HashMap<>();
        applyProperties(finalProperties, environmentVariables);
        applyProperties(finalProperties, projectProperties);
        applyProperties(finalProperties, systemProperties);

        for (final String propertyKey : finalProperties.keySet()) {
            final Object propertyValue = finalProperties.get(propertyKey);
            if (propertyValue != null) {
                setPropertyUsingSetter(buildBomTask, propertyKey, propertyValue.toString());
            }
        }
    }

    private void applyProperties(final Map<String, Object> properties, final Map<? extends Object, ? extends Object> propertiesToAdd) {
        final String prefix = "blackduck_";
        for (final Map.Entry<? extends Object, ? extends Object> entry : propertiesToAdd.entrySet()) {
            if (entry.getKey() != null && entry.getKey() instanceof String && ((String) entry.getKey()).startsWith(prefix)) {
                properties.put(((String) entry.getKey()).substring(prefix.length()), entry.getValue());
            }
        }
    }

    private void setPropertyUsingSetter(final BuildBomTask buildBomTask, final String propertyFieldName, final String propertyValue) {
        final String setterName = "set" + StringUtils.capitalize(propertyFieldName);
        final Method[] methods = buildBomTask.getClass().getMethods();
        for (final Method method : methods) {
            if (method.getName().equals(setterName)) {
                final Class<?>[] parameters = method.getParameterTypes();
                if (parameters.length == 1) {
                    final String parameterName = parameters[0].getName();
                    try {
                        if ("java.lang.String".equals(parameterName)) {
                            method.invoke(buildBomTask, propertyValue);
                        } else if ("int".equals(parameterName)) {
                            method.invoke(buildBomTask, NumberUtils.toInt(propertyValue));
                        } else if ("long".equals(parameterName)) {
                            method.invoke(buildBomTask, NumberUtils.toLong(propertyValue));
                        } else if ("boolean".equals(parameterName)) {
                            method.invoke(buildBomTask, Boolean.parseBoolean(propertyValue));
                        }
                    } catch (final InvocationTargetException | IllegalAccessException e) {
                        logger.warn(String.format("Could not invoke %s with %s", method.getName(), propertyValue));
                    }
                }
            }
        }
    }

}
