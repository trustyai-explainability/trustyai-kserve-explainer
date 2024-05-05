package org.kie.trustyai;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.kie.trustyai.explainability.local.LocalExplainer;
import org.kie.trustyai.explainability.local.lime.LimeConfig;
import org.kie.trustyai.explainability.local.lime.LimeExplainer;
import org.kie.trustyai.explainability.local.shap.ShapConfig;
import org.kie.trustyai.explainability.local.shap.ShapKernelExplainer;
import org.kie.trustyai.explainability.model.*;

import java.util.List;

@Singleton
public class ExplainerFactory {

    @Inject
    ConfigService configService;

    public LocalExplainer<SaliencyResults> getExplainer(ExplainerType type, List<PredictionInput> background) throws IllegalArgumentException {
        return switch (type) {
            case LIME -> {
                final LimeConfig limeConfig = new LimeConfig()
                        .withNormalizeWeights(configService.getLimeNormalizeWeights())
                        .withSamples(configService.getLimeSamples())
                        .withRetries(configService.getLimeRetries())
                        .withUseWLRLinearModel(configService.getLimeWLR());
                yield new LimeExplainer(limeConfig);
            }
            case SHAP -> {
                ShapConfig shapConfig = ShapConfig.builder().withLink(ShapConfig.LinkType.IDENTITY).withBackground(background).build();
                yield new ShapKernelExplainer(shapConfig);
            }
            default -> throw new IllegalArgumentException("Unsupported explainer type: " + type);
        };
    }
}
