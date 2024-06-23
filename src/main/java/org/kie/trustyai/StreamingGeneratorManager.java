package org.kie.trustyai;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.kie.trustyai.explainability.local.shap.background.StreamingGenerator;
import org.kie.trustyai.statistics.MultivariateOnlineEstimator;
import org.kie.trustyai.statistics.distributions.gaussian.MultivariateGaussianParameters;
import org.kie.trustyai.statistics.estimators.WelfordOnlineEstimator;

@Singleton
public class StreamingGeneratorManager {

    @Inject
    ConfigService configService;

    private StreamingGenerator streamingGenerator = null;

    public synchronized void initialize(int dimensions) {
        if (streamingGenerator == null && configService.getExplainerType() == ExplainerType.SHAP) {
            final MultivariateOnlineEstimator<MultivariateGaussianParameters> estimator = new WelfordOnlineEstimator(dimensions);
            streamingGenerator = new StreamingGenerator(dimensions, configService.getQueueSize(), configService.getDiversitySize(), estimator);
        }
    }

    public StreamingGenerator getStreamingGenerator() {
        return streamingGenerator;
    }

}
