# Helm chart

## Install helm release

- Build your image. Push it to your container repository if needed.
- Create a [values file](https://helm.sh/docs/chart_template_guide/values_files/). Use (helm/values-template.yaml) as a template.
- Install your helm release:

```bash
  helm install -f <your-values-file> <release-name> helm --wait
```

## Consideration for building the final chart.

- Helm chart is tested in github actions on PRs with medyagh/setup-minikube@master.
- Ingress should be configurable.
- env variables and secrets should be configurable.
- The chart should be tested with sample 05 to make sure it covers well use case with cloud resources.

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

Choose the correct values in helm/values-template.yaml.

Install helm release

```bash
helm install <release-name> helm
```
