CONNECTOR_NAME=$1 # Should be `consumer` or `provider`
REGISTRY=$2 # Container registry
helm install r1 helm --set image.repository=${REGISTRY}/$CONNECTOR_NAME --set ingress.prefix=/edc${CONNECTOR_NAME} -n ${CONNECTOR_NAME}-namespace --create-namespace
