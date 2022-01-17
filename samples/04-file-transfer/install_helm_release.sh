#!/bin/bash
set -ex
NAMESPACE=$1
VALUES_PATH=$2
RELEASE_NAME=$3

helm install -f ${VALUES_PATH} $RELEASE_NAME helm --debug -n ${NAMESPACE} --create-namespace --wait