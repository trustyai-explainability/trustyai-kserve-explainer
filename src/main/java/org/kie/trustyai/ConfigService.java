package org.kie.trustyai;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ConfigService {


    @ConfigProperty(name = "explainer.type", defaultValue = "LIME")
    String explainerType;

    public String getExplainerType() {
        return explainerType;
    }
}
