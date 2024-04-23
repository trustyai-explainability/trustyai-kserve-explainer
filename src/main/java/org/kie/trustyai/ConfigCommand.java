package org.kie.trustyai;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import picocli.CommandLine;
import org.jboss.logging.Logger;

@QuarkusMain
public class ConfigCommand implements QuarkusApplication {

    private static final Logger LOGGER = Logger.getLogger(ConfigCommand.class.getName());

    @Inject
    CommandLineArgs cmdArgs;

    @Override
    public int run(String... args) {
        LOGGER.debug("Starting application...");
        final CommandLine commandLine = new CommandLine(cmdArgs);

        try {
            commandLine.parseArgs(args);
            if (commandLine.isUsageHelpRequested()) {
                commandLine.usage(System.out);
                return 0;
            }


            LOGGER.debug("Configuration loaded successfully.");
        } catch (CommandLine.ParameterException e) {
            LOGGER.error("Error parsing command line: " + e.getMessage());
            commandLine.usage(System.err);
            return 1;
        }

        Quarkus.waitForExit();  // Wait for Quarkus shutdown events
        LOGGER.debug("Quarkus is waiting for exit...");
        return 0;
    }
}
