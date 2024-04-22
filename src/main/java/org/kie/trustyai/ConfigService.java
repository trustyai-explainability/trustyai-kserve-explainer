package org.kie.trustyai;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

@ApplicationScoped
public class ConfigService {

    private final Map<String, String> configurations = new HashMap<>();

    public void addConfig(String key, String value) {
        configurations.put(key, value);
    }

    public String getConfig(String key) {
        return configurations.get(key);
    }

    public Map<String, String> getAllConfigurations() {
        return Collections.unmodifiableMap(configurations);
    }

    public String getV1HTTPPredictorURI() {
        return "http://" + getConfig("predictor_host") + "/v1/models/" + getConfig("model_name") + ":predict";
    }
}
