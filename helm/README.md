# Helm chart

## Install helm release with minikube

Start minikube:

```bash
minikube start
```

Set minikube docker-env:

```bash
eval $(minikube docker-env)
```

Build connector image:

```bash
cd DataspaceConnector
docker build . -f launchers/ids-connector/Dockerfile -t connector:latest
```

Choose the correct values in helm/values.yaml.

Install helm release

```bash
helm install <release-name> helm
```
