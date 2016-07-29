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
You can download the latest release from Maven Central.

## Configuration
Add the following repository:
```
repositories {
  mavenCentral()
}
```

Add the following to the top of your build.gradle file:
```
buildscript {
    repositories {
        mavenCentral()
        maven { url "http://jcenter.bintray.com" }
    }
    dependencies {
        classpath group: 'com.blackducksoftware.integration', name: 'hub-gradle-plugin', version: '2.0.6'
    }
}
apply plugin: 'com.blackducksoftware.hub'
```

You can specify your own outputDirectory with the following:
```
createHubOutput {
    outputDirectory = "/any/directory/of/your/choosing"
}
```

You can specify your Hub configuration (if you created the output in /any/directory/of/your/choosing) as follows:
```
deployHubOutput {
  outputDirectory = "/any/directory/of/your/choosing"
  hubUrl = "http://localhost:8080"
	hubUsername = "sysadmin"
	hubPassword = "blackduck"
	hubTimeout = "120"
	hubProxyHost = ""
	hubProxyPort = ""
	hubNoProxyHosts = ""
	hubProxyUsername = ""
	hubProxyPassword = ""
}
```

You can find all available tasks by running:
```
gradle tasks --all
```

You can create the output for the Hub by running:
```
gradle createHubOutput
```

You can deploy the output to the Hub by running:
```
gradle deployHubOutput
```

Or combine them, to create then deploy:
```
gradle createHubOutput deployHubOutput
```

## License ##
Apache License 2.0
