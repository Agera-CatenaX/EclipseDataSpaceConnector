#
#  Copyright (c) 2021 Daimler TSS GmbH
#
#  This program and the accompanying materials are made available under the
#  terms of the Apache License, Version 2.0 which is available at
#  https://www.apache.org/licenses/LICENSE-2.0
#
#  SPDX-License-Identifier: Apache-2.0
#
#  Contributors:
#       Daimler TSS GmbH - Initial API and Implementation
#
#

#CONNECTOR_NAME can be `consumer` or `provider`.
ARG CONNECTOR_NAME=consumer

FROM gradle:7.2.0-jdk11-hotspot AS build
ARG CONNECTOR_NAME
COPY --chown=gradle:gradle . /home/gradle/project/
WORKDIR /home/gradle/project/
RUN gradle samples:04-file-transfer:$CONNECTOR_NAME:build

FROM openjdk:11-jre
ARG CONNECTOR_NAME
ENV CONNECTOR_NAME=$CONNECTOR_NAME
WORKDIR /app
COPY --from=build /home/gradle/project/samples/04-file-transfer/$CONNECTOR_NAME/build/libs/$CONNECTOR_NAME.jar /app/app.jar

EXPOSE 8181

ENTRYPOINT java \
    -Dids.webhook.address="http://ouphi1101test.switzerlandnorth.cloudapp.azure.com/edc${CONNECTOR_NAME}" -jar app.jar
