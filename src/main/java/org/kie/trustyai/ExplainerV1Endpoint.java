package org.kie.trustyai;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.kie.trustyai.connectors.kserve.v1.KServeV1HTTPPredictionProvider;
import org.kie.trustyai.connectors.kserve.v1.KServeV1RequestPayload;
import org.kie.trustyai.explainability.local.LocalExplainer;
import org.kie.trustyai.explainability.model.Prediction;
import org.kie.trustyai.explainability.model.PredictionInput;
import org.kie.trustyai.explainability.model.PredictionOutput;
import org.kie.trustyai.explainability.model.PredictionProvider;
import org.kie.trustyai.explainability.model.SaliencyResults;
import org.kie.trustyai.explainability.model.SimplePrediction;
import org.kie.trustyai.payloads.SaliencyExplanationResponse;

@Path("/v1/models/{modelName}:explain")
public class ExplainerV1Endpoint {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    @Default
    ConfigService configService;

    @Inject
    CommandLineArgs cmdArgs;

    @Inject
    ExplainerFactory explainerFactory;

    @Inject
    StreamingGeneratorManager streamingGeneratorManager;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response explain(@PathParam("modelName") String modelName, KServeV1RequestPayload data)
            throws ExecutionException, InterruptedException {

        Log.info("Using explainer type [" + configService.getExplainerType() + "]");
        Log.info("Using V1 HTTP protocol");
        final String predictorURI = cmdArgs.getV1HTTPPredictorURI(modelName);
        final PredictionProvider provider = new KServeV1HTTPPredictionProvider(null, null, predictorURI, 1);
        Log.info("Using predictor URI [" + predictorURI + "]");

        final List<PredictionInput> input = data.toPredictionInputs();
        final PredictionOutput output = provider.predictAsync(input).get().get(0);
        final Prediction prediction = new SimplePrediction(input.get(0), output);
        final int dimensions = input.get(0).getFeatures().size();

        if (configService.getExplainerType() == ExplainerType.SHAP) {
            if (Objects.isNull(streamingGeneratorManager.getStreamingGenerator())) {
                Log.info("Initializing SHAP's Streaming Background Generator with dimension " + dimensions);
                streamingGeneratorManager.initialize(dimensions);
            }
            final double[] numericData = new double[dimensions];
            for (int i = 0; i < dimensions; i++) {
                numericData[i] = input.get(0).getFeatures().get(i).getValue().asNumber();
            }
            final RealVector vectorData = new ArrayRealVector(numericData);
            streamingGeneratorManager.getStreamingGenerator().update(vectorData);
        }

        final ExplainerType explainerType = configService.getExplainerType();

        try {
            final LocalExplainer<SaliencyResults> explainer = explainerFactory.getExplainer(explainerType);
            Log.info("Sending explaining request to " + predictorURI);
            final SaliencyResults results = explainer.explainAsync(prediction, provider).get();
            final SaliencyExplanationResponse response = SaliencyExplanationResponse.fromSaliencyResults(results);

            try {
                return Response.ok(response, MediaType.APPLICATION_JSON).build();
            } catch (Exception e) {
                return Response.serverError().entity("Error serializing SaliencyResults to JSON: " + e.getMessage())
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.serverError().entity("Error: " + e.getMessage()).build();
        }

    }
}
