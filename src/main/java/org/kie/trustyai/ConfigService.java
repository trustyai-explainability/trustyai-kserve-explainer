package org.kie.trustyai;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;


@ApplicationScoped
public class ConfigService {

    private static final Logger LOGGER = Logger.getLogger(ConfigService.class.getName());



    @ConfigProperty(name = "explainer.type", defaultValue = "LIME")
    ExplainerType explainerType;

    public int getLimeSamples() {
        return limeSamples;
    }

    @ConfigProperty(name = "lime.samples", defaultValue = "200")
    int limeSamples;

    public int getLimeRetries() {
        return limeRetries;
    }

    @ConfigProperty(name = "lime.retries", defaultValue = "2")
    int limeRetries;

    public boolean getLimeWLR() {
        return limeWLR;
    }

    @ConfigProperty(name = "lime.wlr", defaultValue = "true")
    boolean limeWLR;


    public boolean getLimeNormalizeWeights() {
        return limeNormalizeWeights;
    }

    @ConfigProperty(name = "lime.normalize.weights", defaultValue = "true")
    boolean limeNormalizeWeights;


    @PostConstruct
    private void validateConfig() {
        if (explainerType == null) {
            LOGGER.error("Unknown explainer type configured. Falling back to LIME.");
            explainerType = ExplainerType.LIME;
        }
    }

    public ExplainerType getExplainerType() {
        return explainerType;
    }
}
