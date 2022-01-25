# SonarQube 

## Overview

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

```bash
./gradlew clean check jacocoTestReport     
./gradlew sonarqube
```

## Limitations
