package org.kie.trustyai;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.kie.trustyai.connectors.kserve.v1.KServeV1HTTPPredictionProvider;
import org.kie.trustyai.connectors.kserve.v1.KServeV1RequestPayload;
import org.kie.trustyai.explainability.local.lime.LimeConfig;
import org.kie.trustyai.explainability.local.lime.LimeExplainer;
import org.kie.trustyai.explainability.model.*;

import jakarta.inject.Inject;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Path("/v1/models/{modelName}:explain")
public class ExplainerEndpoint {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    ConfigService configService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response explainIncome(@PathParam("modelName") String modelName, KServeV1RequestPayload data) throws ExecutionException, InterruptedException {
        final String predictorURI = configService.getV1HTTPPredictorURI();
        final PredictionProvider provider = new KServeV1HTTPPredictionProvider(null, null, predictorURI);
        final List<PredictionInput> input = data.toPredictionInputs();
        final PredictionOutput output = provider.predictAsync(input).get().get(0);
        final Prediction prediction = new SimplePrediction(input.get(0), output);
        final LimeConfig config = new LimeConfig().withNormalizeWeights(true).withSamples(5000).withRetries(10).withUseWLRLinearModel(true);

        final LimeExplainer explainer = new LimeExplainer(config);
        final SaliencyResults results = explainer.explainAsync(prediction, provider).get();

        try {
            String resultsJson = objectMapper.writeValueAsString(results);
            return Response.ok(resultsJson, MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            return Response.serverError().entity("Error serializing SaliencyResults to JSON: " + e.getMessage()).build();
        }
    }
}
