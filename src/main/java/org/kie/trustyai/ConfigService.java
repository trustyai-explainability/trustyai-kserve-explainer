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
