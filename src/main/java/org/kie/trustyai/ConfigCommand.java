package org.kie.trustyai;

import io.quarkus.logging.Log;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import picocli.CommandLine;

import java.util.Arrays;

@QuarkusMain
public class ConfigCommand implements QuarkusApplication {

    @Inject
    CommandLineArgs cmdArgs;

    @Override
    public int run(String... args) {
        Log.info("Starting application...");
        final CommandLine commandLine = new CommandLine(cmdArgs);
        Log.debug("Using command-line arguments: " + Arrays.toString(args));
        try {
            commandLine.parseArgs(args);
            if (commandLine.isUsageHelpRequested()) {
                commandLine.usage(System.out);
                return 0;
            }

            Log.info("Configuration loaded successfully.");
        } catch (CommandLine.ParameterException e) {
            Log.error("Error parsing command line: " + e.getMessage());
            commandLine.usage(System.err);
            return 1;
        }

        Quarkus.waitForExit();  // Wait for Quarkus shutdown events
        Log.info("Quarkus is waiting for exit...");
        return 0;
    }
}
