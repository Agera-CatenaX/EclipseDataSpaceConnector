CONNECTOR_NAME=$1 # Should be `consumer` or `provider`.
REGISTRY=$2 # Container registry.
NAMESPACE=$3
RELEASE_NAME=$4
INGRESS_HOST=$5
INGRESS_CLASS_NAME=$6 # Install your ingress controller and pass ingress class name accordingly.

helm install $RELEASE_NAME helm --debug --set image.repository=${REGISTRY}/${CONNECTOR_NAME} --set image.tag="latest" \
            --set ingress.prefix=/edc${CONNECTOR_NAME} \
            --set ingress.host=${INGRESS_HOST} --set ingress.className=${INGRESS_CLASS_NAME} \
            -n ${NAMESPACE} --create-namespace --wait