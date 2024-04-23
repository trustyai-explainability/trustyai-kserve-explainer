package org.kie.trustyai;

import org.kie.trustyai.explainability.local.LocalExplainer;
import org.kie.trustyai.explainability.local.lime.LimeConfig;
import org.kie.trustyai.explainability.local.lime.LimeExplainer;
import org.kie.trustyai.explainability.local.shap.ShapConfig;
import org.kie.trustyai.explainability.local.shap.ShapKernelExplainer;
import org.kie.trustyai.explainability.model.*;

import java.util.List;

public class ExplainerFactory {

    public static LocalExplainer<SaliencyResults> getExplainer(ExplainerType type, List<PredictionInput> background) throws IllegalArgumentException {
        return switch (type) {
            case LIME -> {
                LimeConfig limeConfig = new LimeConfig().withNormalizeWeights(true).withSamples(5000).withRetries(10).withUseWLRLinearModel(true);
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
