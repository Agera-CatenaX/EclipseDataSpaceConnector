# SonarQube 

## Overview

[SonarQube](https://docs.sonarqube.org/latest/setup/get-started-2-minutes/) is a platform for code quality measure,emt. It offers a Community Edition 
version that is open sourced and free. 

## Run

SonarQube with minimal configuration can be started from below [docker-compose](./sonar/docker-compose.yml) file. 

```yml
version: "3"
services:
  sonarqube:
    image: sonarqube:lts
    ports:
      - 9000:9000
    environment:
      - SONAR_FORCEAUTHENTICATION=false
```

And then run 

```bash

docker-compose up

```
The application starts on http://localhost:9000.

### Add current project to sonarqube

To add project to sonarqube we have to add sonarqube plugin to gradle:

```gradle
plugin {
    id("org.sonarqube") version "3.3"
}
```
If SonarQube runs on default port then we don't have to specify other properties, otherwise 

Run tests w code coverage and add project to running SonarQube:

```bash
./gradlew clean check jacocoTestReport     
./gradlew sonarqube
```

## Advantages

- Enables a lot of metrics from static analysis, detecting potential bugs and code coverage reports in single tool

## Limitations

- Requires deploying a central instance to have a dashboard for the whole project (in contrast to Codecov and Codacy where the measurements are available 
  after logging to the online platform)
- Community Edition version doesn't support analysis of multiple branches
- Reporting quality measures to branches and pull requests in Github not supported in free version
- Automatic detection of branches/pull requests in Github Actions not supported in free version