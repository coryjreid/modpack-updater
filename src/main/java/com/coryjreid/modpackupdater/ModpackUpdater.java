package com.coryjreid.modpackupdater;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Properties;

import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.UnflaggedOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModpackUpdater {
    private static final Logger sLogger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final JSAP sArgumentParser = new JSAP();
    private static final String CONFIGURATION_PATH_KEY = "configurationFilePath";

    public static void main(final String[] args) {
        try {
            initializeArguments();
        } catch (final JSAPException exception) {
            sLogger.error("Failed to initialize program argument parser", exception);
            System.exit(1);
        }

        final JSAPResult jsapResult = sArgumentParser.parse(args);
        validateJsapResult(jsapResult);

        final String configFilePath = jsapResult.getString(CONFIGURATION_PATH_KEY);
        final File configFile = new File(configFilePath);
        if (!configFile.exists()) {
            sLogger.error("The file " + configFilePath + " does not exist");
            System.exit(1);
        }

        try (final InputStream inputStream = Files.newInputStream(configFile.toPath())) {
            final Properties properties = new Properties();
            properties.load(inputStream);

            final ModpackMigrator migrator = new ModpackMigrator(new ModpackMigratorProperties(properties));
            migrator.doModpackUpdate();
            // This is nasty and is currently required because CurseAPI does not cleanly shutdown its resources
            System.exit(0);
        } catch (final IOException exception) {
            sLogger.error("Failed to read the config file", exception);
            System.exit(1);
        }

    }

    private static void validateJsapResult(final JSAPResult jsapResult) {
        if (!jsapResult.success()) {
            final Iterator<String> errorMessageIterator = jsapResult.getErrorMessageIterator();
            while (errorMessageIterator.hasNext()) {
                sLogger.error("ERROR: " + errorMessageIterator.next());
            }
            sLogger.error(getUsage());
            System.exit(1);
        }
    }

    private static String getUsage() {
        return "USAGE: java -jar <jarName> " + sArgumentParser.getUsage();
    }

    private static void initializeArguments() throws JSAPException {
        final UnflaggedOption configurationPath = new UnflaggedOption(CONFIGURATION_PATH_KEY)
            .setStringParser(JSAP.STRING_PARSER)
            .setRequired(true);

        sArgumentParser.registerParameter(configurationPath);
    }
}
