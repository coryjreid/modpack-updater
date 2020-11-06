package com.coryjreid.modpackupdater;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;

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

        // TODO Validate input and actually run tool
    }

    private static void validateJsapResult(final JSAPResult jsapResult) {
        if (!jsapResult.success()) {
            final Iterator<String> errorMessageIterator = jsapResult.getErrorMessageIterator();
            while (errorMessageIterator.hasNext()) {
                System.err.println("ERROR: " + errorMessageIterator.next());
            }
            System.err.println(getUsage());
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
