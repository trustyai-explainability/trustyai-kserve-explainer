[![CI](https://github.com/trustyai-explainability/trustyai-kserve-explainer/actions/workflows/ci.yaml/badge.svg)](https://github.com/trustyai-explainability/trustyai-kserve-explainer/actions/workflows/ci.yaml)

# TrustyAI KServe Explainer

The TrustyAI KServe integration provides explanations for predictions made by AI/ML models using the built-in [KServe explainer support](https://kserve.github.io/website/0.12/modelserving/explainer/explainer/). It supports LIME and SHAP explanation methods, configurable directly within KServe `InferenceServices`.

## Features

- **Explainability**: Integrated support for LIME and SHAP explanation methods to interpret model predictions via the `:explain` endpoint.

## Deployment on KServe

The TrustyAI explainer can be added to KServe `InferenceServices`. Here are YAML configurations to deploy explainers with LIME and SHAP:

### LIME and SHAP Explainer `InferenceService`

By default, the TrustyAI KServe explainer will use the **both the LIME and SHAP explainer**. You can deploy the explainers using the following YAML configuration:

```yaml
apiVersion: "serving.kserve.io/v1beta1"
kind: "InferenceService"
metadata:
  name: "explainer-test-all"
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
        image: quay.io/trustyai/trustyai-kserve-explainer:latest
```

### Example: Using the both the LIME and SHAP Explainer

You can interact with the LIME and SHAP explainer using the following `curl` command:

```bash
payload='{"data": {"ndarray": [[1.0, 2.0]]}}'  # Adjust payload as per your input requirements
curl -s -H "Host: ${HOST}" \
     -H "Content-Type: application/json" \
     "http://${GATEWAY}/v1/models/explainer-test-all:explain" -d $payload
```

This command sends a JSON payload to the `:explain` endpoint and retrieves an explanation for the prediction. The response structure includes the explainer type and saliencies of each feature contributing to the prediction, as shown below:

```json
{
    "timestamp": "2024-05-06T21:42:45.307+00:00",
    "LIME": {
      "saliencies": {
          "outputs-0": [
              {
                  "name": "inputs-12",
                  "score": 0.8496797810357467,
                  "confidence": 0
              },
              {
                  "name": "inputs-5",
                  "score": 0.6830766647546147,
                  "confidence": 0
              },
              {
                  "name": "inputs-7",
                  "score": 0.6768475400887952,
                  "confidence": 0
              },
              // Additional features
            ]
        }
    }
    "SHAP": {
      "saliencies": {
        // Additional features
      }
    }
}
```

### LIME Explainer `InferenceService`

To use the **LIME explainer only**, you can deploy the explainer by specifying it as an environment variable and using the following YAML configuration (initial part will be identical to the previous `InferenceService`):

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
        image: quay.io/trustyai/trustyai-kserve-explainer:latest
        env:
          - name: EXPLAINER_TYPE # <- specify LIME here
            value: "LIME"
```

### SHAP Explainer `InferenceService`

To use the **SHAP explainer only**:


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
        image: quay.io/trustyai/trustyai-kserve-explainer:latest
        env:
          - name: EXPLAINER_TYPE # <- specify SHAP here
            value: "SHAP"
```

The explanation request for either LIME or SHAP will be identical to both LIME and SHAP.

## Configuration

The following environment variables can be used in the `InferenceService` to customize the explainer:

| Name                                                                     | Description                                                        | Default       |
|--------------------------------------------------------------------------|--------------------------------------------------------------------|---------------|
| `EXPLAINER_TYPE`                                                         | `ALL`, `LIME` or `SHAP`, the explainer to use.                     | `ALL`         |
| `LIME_SAMPLES`                                                           | The number of samples to use in LIME                               | `200` |
| `LIME_RETRIES`                                                           | Number of LIME retries                                             | `2`   |
| `LIME_WLR`                                                               | Use LIME Weighted Linear Regression, `true` or `false`             | `true`        |
| `LIME_NORMALIZE_WEIGHTS`                                                 | Whether LIME should normalize the weights, `true` or `false`       | `true`        |
| `EXPLAINER_SHAP_BACKGROUND_QUEUE`                                        | The number of observations to keep in memory for SHAP's background | `10`          |
| `EXPLAINER_SHAP_BACKGROUND_DIVERSITY` | The number of synthetic samples to generate for diversity          | `10`          |

## Contributing

To get started with contributing to this project:

### Prerequisites

- JDK 11+
- Maven 3.8.1+
- Docker (optional, for containerization)


### Clone the repository

```bash
git clone https://github.com/trustyai/trustyai-kserve-explainer.git
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
