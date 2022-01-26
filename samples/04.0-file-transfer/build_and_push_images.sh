REGISTRY=$1 # Container registry
gradle samples:04-file-transfer:consumer:build samples:04-file-transfer:provider:build
docker build --build-arg CONNECTOR_NAME=consumer -f samples/04-file-transfer/Dockerfile . -t ${REGISTRY}/consumer:latest
docker build --build-arg CONNECTOR_NAME=provider -f samples/04-file-transfer/Dockerfile . -t ${REGISTRY}/provider:latest
docker push ${REGISTRY}/consumer:latest
docker push ${REGISTRY}/provider:latest
