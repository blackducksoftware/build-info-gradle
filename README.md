## Overview ##
This plugin provides ability to generate a [Black Duck I/O](https://github.com/blackducksoftware/bdio) formatted file containing the dependency information gathered from the Gradle project. The file is generated in either the specified folder, or defaults to the root of the project. This plugin also has the ability to upload the [Black Duck I/O](https://github.com/blackducksoftware/bdio) file up to the hub to create a code location in the hub. In order to generate the file and upload the contents to the hub the build.gradle file must have a section for this plugin and execute tasks specific to this plugin.

Tasks:

* createHubOutput - generates the [Black Duck I/O](https://github.com/blackducksoftware/bdio) file
* deployHubOutput - uploads the file to the hub server
* buildInfoCustomTask - generates a file in a deprecated format for your build dependencies

## Build ##
[![Build Status](https://travis-ci.org/blackducksoftware/hub-gradle-plugin.svg?branch=master)](https://travis-ci.org/blackducksoftware/hub-gradle-plugin)
[![Coverage Status](https://coveralls.io/repos/github/blackducksoftware/hub-gradle-plugin/badge.svg?branch=master)](https://coveralls.io/github/blackducksoftware/hub-gradle-plugin?branch=master)

## Where can I get the latest release? ##
You can download the latest source from GitHub: https://github.com/blackducksoftware/hub-gradle-plugin. 

## Configuration
Add the following to the top of your build.gradle file:
```
buildscript {
    repositories {
        maven { url "https://updates.suite.blackducksoftware.com/integrations" }
    }
    dependencies {
        classpath group: 'com.blackducksoftware.integration', name: 'hub-gradle-plugin', version: '2.0.3'
    }
}
apply plugin: 'com.blackducksoftware.hub'
```

## License ##
Apache License 2.0
