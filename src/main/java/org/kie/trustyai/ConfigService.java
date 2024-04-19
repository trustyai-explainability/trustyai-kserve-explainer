package org.kie.trustyai;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import io.quarkus.runtime.annotations.CommandLineArguments;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class ConfigService {

    private Map<String, String> configurations = new HashMap<>();

    @Inject
    @CommandLineArguments
    String[] args;

    void onStart(@Observes StartupEvent ev) {
        System.out.println("Parsing command-line arguments:");
        // Assume args are always in pairs
        for (int i = 0; i < args.length; i += 2) {
            String key = args[i].startsWith("--") ? args[i].substring(2) : args[i];
            String value = args[i + 1];
            configurations.put(key, value);
        }

        // Make configurations unmodifiable to ensure immutability
        configurations = Collections.unmodifiableMap(configurations);
        configurations.forEach((key, value) -> System.out.println(key + ": " + value));
    }

    public String getConfig(String key) {
        return configurations.get(key);
    }

    public String getV1HTTPPredictorURI() {
        return "http://" + configurations.get("predictor_host") + "/v1/models/" + configurations.get("model_name") + ":predict";
    }
}
