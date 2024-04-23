[![CI](https://github.com/ruivieira/trustyai-kserve-explainer/actions/workflows/ci.yaml/badge.svg)](https://github.com/ruivieira/trustyai-kserve-explainer/actions/workflows/ci.yaml)

# TrustyAI KServe Explainer

The TrustyAI KServe integration provides explanations for predictions made by AI/ML models using the built-in [KServe explainer support](https://kserve.github.io/website/0.12/modelserving/explainer/explainer/). It supports LIME and SHAP explanation methods, configurable directly within KServe `InferenceServices`.

## Features

- **Explainability**: Integrated support for LIME and SHAP explanation methods to interpret model predictions via the `:explain` endpoint.

## Deployment on KServe

The TrustyAI explainer can be added to KServe `InferenceServices`. Here are YAML configurations to deploy explainers with LIME and SHAP:

### LIME Explainer `InferenceService`

By default, the TrustyAI KServe explainer will use the LIME explainer. You can deploy the explainer using the following YAML configuration:

```yaml
apiVersion: "serving.kserve.io/v1beta1"
kind: "InferenceService"
metadata:
  name: "explainer-test-lime"
  annotations:
    sidecar.istio.io/inject: "true"
    sidecar.istio.io/rewriteAppHTTPProbers: "true"
    serving.knative.openshift.io/enablePassthrough: "true"
spec:
  predictor:
    model:
      modelFormat:
        name: sklearn
      protocolVersion: v2
      runtime: kserve-sklearnserver
      storageUri: https://github.com/trustyai-explainability/model-collection/raw/main/credit-score/model.joblib
  explainer:
    containers:
      - name: explainer
        image: quay.io/ruimvieira/trustyai-kserve-explainer:latest
```

### Example: Using the LIME Explainer

You can interact with the LIME explainer using the following `curl` command:

```bash
payload='{"data": {"ndarray": [[1.0, 2.0]]}}'  # Adjust payload as per your input requirements
curl -s -H "Host: ${HOST}" \
     -H "Content-Type: application/json" \
     "http://${GATEWAY}/v1/models/explainer-test-lime:explain" -d $payload
```

This command sends a JSON payload to the `:explain` endpoint and retrieves an explanation for the prediction. The response structure includes the saliencies of each feature contributing to the prediction, as shown below:

```json
{
  "saliencies": {
    "value": {
      "output": {"value": {"underlyingObject": 1}, "type": "NUMBER", "score": 1.0, "name": "value"},
      "perFeatureImportance": [
        {
          "feature": {"name": "f", "type": "NUMBER", "value": {"underlyingObject": 0.9}},
          "score": 0.7474712680313286
        }
        // Additional features...
      ]
    }
  },
  "availableCFs": [],
  "sourceExplainer": "LIME"
}
```

### SHAP Explainer `InferenceService`

To use the SHAP explainer, you can deploy the explainer by specifying it as an environment variable and using the following YAML configuration (initial part will be identical to the previous `InferenceService`):


```yaml
apiVersion: "serving.kserve.io/v1beta1"
kind: "InferenceService"
metadata:
  name: "explainer-test-lime"
  annotations:
    sidecar.istio.io/inject: "true"
    sidecar.istio.io/rewriteAppHTTPProbers: "true"
    serving.knative.openshift.io/enablePassthrough: "true"
spec:
  predictor:
    model:
      modelFormat:
        name: sklearn
      protocolVersion: v2
      runtime: kserve-sklearnserver
      storageUri: https://github.com/trustyai-explainability/model-collection/raw/main/credit-score/model.joblib
  explainer:
    containers:
      - name: explainer
        image: quay.io/ruimvieira/trustyai-kserve-explainer:latest
        env:
          - name: EXPLAINER_TYPE # <- specify SHAP here
            value: "SHAP"
```

The explanation request will be identical to the LIME explainer case.

## Contributing

To get started with contributing to this project:

### Prerequisites

- JDK 11+
- Maven 3.8.1+
- Docker (optional, for containerization)


### Clone the repository

```bash
git clone https://github.com/ruimvieira/trustyai-kserve.git
cd trustyai-kserve
```

### Build the project

```bash
mvn clean package
```

### Run locally

```bash
mvn quarkus:dev
```

### Docker Integration

Build and run the container:

```bash
docker build -f src/main/docker/Dockerfile.jvm -t trustyai-kserve .
docker run -i --rm -p 8080:8080 trustyai-kserve
```

## License

This project is licensed under the Apache License Version 2.0 - see the [LICENSE](LICENSE) file for details.
