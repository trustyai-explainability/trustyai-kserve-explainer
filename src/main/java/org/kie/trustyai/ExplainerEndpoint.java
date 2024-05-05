package org.kie.trustyai;

import jakarta.enterprise.inject.Default;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.kie.trustyai.connectors.kserve.v1.KServeV1HTTPPredictionProvider;
import org.kie.trustyai.connectors.kserve.v1.KServeV1RequestPayload;
import org.kie.trustyai.explainability.local.LocalExplainer;
import org.kie.trustyai.explainability.model.*;

import jakarta.inject.Inject;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Path("/v1/models/{modelName}:explain")
public class ExplainerEndpoint {

    private static final Logger LOGGER = Logger.getLogger(ExplainerEndpoint.class.getName());

    @Inject
    ObjectMapper objectMapper;

    @Inject
    @Default
    ConfigService configService;

    @Inject
    CommandLineArgs cmdArgs;

    @Inject
    ExplainerFactory explainerFactory;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response explainIncome(@PathParam("modelName") String modelName, KServeV1RequestPayload data)
            throws ExecutionException, InterruptedException {
        final String predictorURI = cmdArgs.getV1HTTPPredictorURI();

        LOGGER.debug("Using explainer type [" + configService.getExplainerType() + "]");
        LOGGER.debug("Using predictor URI [" + predictorURI + "]");

        final PredictionProvider provider = new KServeV1HTTPPredictionProvider(null, null, predictorURI);
        final List<PredictionInput> input = data.toPredictionInputs();
        final PredictionOutput output = provider.predictAsync(input).get().get(0);
        final Prediction prediction = new SimplePrediction(input.get(0), output);

        final ExplainerType explainerType = configService.getExplainerType();

        try {
            final LocalExplainer<SaliencyResults> explainer = explainerFactory.getExplainer(explainerType, input);

            final SaliencyResults results = explainer.explainAsync(prediction, provider).get();

            try {
                String resultsJson = objectMapper.writeValueAsString(results);
                return Response.ok(resultsJson, MediaType.APPLICATION_JSON).build();
            } catch (Exception e) {
                return Response.serverError().entity("Error serializing SaliencyResults to JSON: " + e.getMessage())
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.serverError().entity("Explainer type not supported: " + explainerType).build();
        }

    }
}
