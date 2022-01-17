CONNECTOR_NAME=$1 # Should be `consumer` or `provider`.
REGISTRY=$2 # Container registry.
INGRESS_HOST=$3
INGRESS_CLASS_NAME=$4 # Install your ingress controller and pass ingress class name accordingly.

helm install r1 helm --debug --set image.repository=${REGISTRY}/$CONNECTOR_NAME --set image.tag="latest" \
            --set ingress.prefix=/edc${CONNECTOR_NAME} \
            --set ingress.host=${INGRESS_HOST} --set ingress.className=${INGRESS_CLASS_NAME} \
            -n ${CONNECTOR_NAME}-namespace --create-namespace --wait