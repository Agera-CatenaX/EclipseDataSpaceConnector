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

repositories {

    mavenLocal()
    // quick fix, in case gradle search for maven artifact in different folder
    flatDir { dir("/Users/izabela/.m2/repository") }
}

plugins {
    `java-library`
    id("application")
}

val rsApi: String by project

dependencies {
    api(project(":spi"))

    implementation("jakarta.ws.rs:jakarta.ws.rs-api:${rsApi}")
    implementation("org.junit.jupiter:junit-jupiter:5.7.0")
    implementation("net.catenax.prs:prs-client:0.0.1-SNAPSHOT")
}