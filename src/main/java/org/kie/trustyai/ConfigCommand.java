package org.kie.trustyai;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@QuarkusMain
@Command(name = "config-command", mixinStandardHelpOptions = true, description = "Processes command-line options for configuration.")
public class ConfigCommand implements QuarkusApplication {

    @Inject
    ConfigService configService;

    @Option(names = "--predictor_host", description = "The host of the predictor service")
    String predictorHost;

    @Option(names = "--model_name", description = "The name of the model")
    String modelName;

    @Override
    public int run(String... args) {
        CommandLine.populateCommand(this, args);
        configService.addConfig("predictor_host", predictorHost);
        configService.addConfig("model_name", modelName);

         Quarkus.waitForExit();

        return 0;
    }

    public String getV1HTTPPredictorURI() {
        return "http://" + configService.getConfig("predictor_host") + "/v1/models/" + configService.getConfig("model_name") + ":predict";
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ConfigCommand()).execute(args);
        System.exit(exitCode);
    }
}
