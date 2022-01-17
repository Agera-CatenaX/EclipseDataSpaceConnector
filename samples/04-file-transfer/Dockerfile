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

FROM openjdk:11-jre-slim
# CONNECTOR_NAME can be `consumer` or `provider`.
ARG CONNECTOR_NAME=consumer

COPY samples/04-file-transfer/$CONNECTOR_NAME/build/libs/$CONNECTOR_NAME.jar app.jar

ENTRYPOINT java -jar app.jar
