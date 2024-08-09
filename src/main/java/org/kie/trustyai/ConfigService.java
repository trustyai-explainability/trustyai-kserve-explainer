package org.kie.trustyai;

import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ConfigService {

    @ConfigProperty(name = "explainer.type")
    ExplainerType explainerType;
    @ConfigProperty(name = "lime.samples", defaultValue = "200")
    int limeSamples;
    @ConfigProperty(name = "lime.retries", defaultValue = "2")
    int limeRetries;
    @ConfigProperty(name = "lime.wlr", defaultValue = "true")
    boolean limeWLR;
    @ConfigProperty(name = "lime.normalize.weights", defaultValue = "true")
    boolean limeNormalizeWeights;
    @ConfigProperty(name = "explainer.shap.background.queue", defaultValue = "10")
    int queueSize;
    @ConfigProperty(name = "explainer.shap.background.diversity", defaultValue = "10")
    int diversitySize;

    public int getLimeSamples() {
        return limeSamples;
    }

    public int getLimeRetries() {
        return limeRetries;
    }

    public boolean getLimeWLR() {
        return limeWLR;
    }

    public boolean getLimeNormalizeWeights() {
        return limeNormalizeWeights;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public int getDiversitySize() {
        return diversitySize;
    }

    @PostConstruct
    private void validateConfig() {
        if (explainerType == null) {
            Log.error("Unknown explainer type configured. Falling back to LIME and SHAP.");
        }
    }

    public ExplainerType getExplainerType() {
        return explainerType;
    }
}
