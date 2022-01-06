/*
 *  Copyright (c) 2020, 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

plugins {
    `java-library`
}

val cosmosSdkVersion: String by project

dependencies {
    api(project(":spi"))
    api(project(":common:util"))
    implementation("com.azure:azure-cosmos:${cosmosSdkVersion}")
    api(project(":extensions:azure:cosmos-common"))

    testImplementation(testFixtures(project(":common:util")))
    testImplementation("org.testcontainers:azure:1.16.2")
}


publishing {
    publications {
        create<MavenPublication>("transfer-process-store-cosmos") {
            artifactId = "transfer-process-store-cosmos"
            from(components["java"])
        }
    }
}
