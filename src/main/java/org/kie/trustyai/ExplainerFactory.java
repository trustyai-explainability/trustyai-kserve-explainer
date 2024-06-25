package org.kie.trustyai;

import java.util.List;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.kie.trustyai.explainability.local.LocalExplainer;
import org.kie.trustyai.explainability.local.lime.LimeConfig;
import org.kie.trustyai.explainability.local.lime.LimeExplainer;
import org.kie.trustyai.explainability.local.shap.ShapConfig;
import org.kie.trustyai.explainability.local.shap.ShapKernelExplainer;
import org.kie.trustyai.explainability.model.PredictionInput;
import org.kie.trustyai.explainability.model.SaliencyResults;

@Singleton
public class ExplainerFactory {

    @Inject
    ConfigService configService;

    @Inject
    StreamingGeneratorManager streamingGeneratorManager;

    public LocalExplainer<SaliencyResults> getExplainer(ExplainerType type) throws IllegalArgumentException {
        return switch (type) {
            case LIME -> {
                final LimeConfig limeConfig = new LimeConfig()
                        .withNormalizeWeights(configService.getLimeNormalizeWeights())
                        .withSamples(configService.getLimeSamples())
                        .withRetries(configService.getLimeRetries())
                        .withUseWLRLinearModel(configService.getLimeWLR());
                Log.info("Instating LIME explainer");
                yield new LimeExplainer(limeConfig);
            }
            case SHAP -> {
                final int backgroundSize = configService.getQueueSize() + configService.getDiversitySize();
                Log.debug("Requesting " + backgroundSize + " background samples from SHAP's streaming generator");
                final List<PredictionInput> background = streamingGeneratorManager.getStreamingGenerator().generate(backgroundSize);
                Log.debug("The background has a size of " + background.size());
                final ShapConfig shapConfig = ShapConfig.builder().withRegularizer(5)
                        .withLink(ShapConfig.LinkType.IDENTITY)
                        .withBackground(background).build();
                Log.info("Instantiating SHAP explainer");
                yield new ShapKernelExplainer(shapConfig);
            }
            default -> throw new IllegalArgumentException("Unsupported explainer type: " + type);
        };
    }
}
