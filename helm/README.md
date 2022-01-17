# Helm chart

## Install helm release

- Build your image. Push it to your container repository if needed.
- Create a [values file](https://helm.sh/docs/chart_template_guide/values_files/). Use (helm/values.yaml) as a template.
- Install your helm release:

```bash
  helm install -f <your-values-file> <release-name> helm --wait
```

## Install helm release with minikube

Start minikube:

```bash
minikube start
```

Set minikube docker-env:

```bash
eval $(minikube docker-env)
```

Build connector image, for example:

```bash
cd DataspaceConnector
docker build . -f launchers/ids-connector/Dockerfile -t connector:latest
```

Choose the correct values in helm/values.yaml.

Install helm release

```bash
helm install <release-name> helm
```
