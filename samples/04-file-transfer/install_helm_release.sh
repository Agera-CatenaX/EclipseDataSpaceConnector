#!/bin/bash
set -ex
NAMESPACE=$1
RELEASE_NAME=$2
IMAGE=$3
INGRESS_CLASS_NAME=$4 # Install your ingress controller and pass ingress class name accordingly.
INGRESS_PREFIX=$5
INGRESS_HOST=$6
IMAGE_PULL_POLICY=${7:-IfNotPresent}

helm install $RELEASE_NAME helm --debug --set image.repository=${IMAGE} --set image.tag="latest" \
            --set ingress.prefix=${INGRESS_PREFIX} --set ingress.host=${INGRESS_HOST} \
            --set ingress.className=${INGRESS_CLASS_NAME} --set image.pullPolicy=${IMAGE_PULL_POLICY} \
            -n ${NAMESPACE} --create-namespace --wait