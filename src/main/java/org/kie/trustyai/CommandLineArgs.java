package org.kie.trustyai;

import jakarta.inject.Singleton;
import picocli.CommandLine;

@Singleton
public class CommandLineArgs {

    @CommandLine.Option(names = "--predictor_host", description = "The host of the predictor service")
    private String predictorHost;

    @CommandLine.Option(names = "--model_name", description = "The name of the model")
    private String modelName;

    @CommandLine.Option(names = "--http_port", description = "The HTTP port of the predictor")
    private int httpPort;
    @CommandLine.Option(names = "--predictor_protocol", defaultValue = "v1", description = "The predictor protocol version (v1 or v2)")
    private String predictorProtocol;

    public String getPredictorProtocol() {
        return predictorProtocol;
    }

    public String getPredictorHost() {
        return predictorHost;
    }

    public String getModelName() {
        return modelName;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public String getV1HTTPPredictorURI(String modelName) {
        return "http://" + predictorHost + "/v1/models/" + modelName + ":predict";
    }

    public String getV2HTTPPredictorURI(String modelName) {

        return "http://" +
                predictorHost +
                "/v2/models/" +
                modelName +
                "/infer";
    }

}
