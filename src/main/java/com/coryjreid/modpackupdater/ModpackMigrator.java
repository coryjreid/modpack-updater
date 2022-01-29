package com.coryjreid.modpackupdater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import com.coryjreid.modpackupdater.json.ModFile;
import com.coryjreid.modpackupdater.json.ModpackManifest;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModpackMigrator {
    private static final Logger sLogger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ModpackMigratorProperties mProperties;
    private final String mServerRootPath;
    private final String mRepositoryPath;
    private final String[] mFoldersToUpdate;

    public ModpackMigrator(final ModpackMigratorProperties properties) {
        mProperties = properties;
        mServerRootPath = (mProperties.getServerRootPath().endsWith(File.separator)
                ? mProperties.getServerRootPath()
                : mProperties.getServerRootPath() + File.separator);
        mRepositoryPath = (mProperties.getSourceRepositoryPath().endsWith(File.separator)
                ? mProperties.getSourceRepositoryPath()
                : mProperties.getSourceRepositoryPath() + File.separator);
        mFoldersToUpdate = properties.getFolders();
    }

    public final void doModpackUpdate() {
        sLogger.info("Beginning modpack update");
        doGitCheckout();
        doDockerShutdown();
        doServerRootCleanup();
        doServerRootUpdates();
        doModUpdate();
        doUpdateServerProperties();
        doDockerStart();
        sLogger.info("Finished modpack update");
    }

    private void doGitCheckout() {
        try {
            final ProcessBuilder processBuilder = new ProcessBuilder("git", "pull", "origin", mProperties.getGitBranchName());
            processBuilder.directory(new File(mRepositoryPath));
            final Process process = processBuilder.start();
            process.waitFor();
            sLogger.info("Updated the source repository");
        } catch (final IOException | InterruptedException exception) {
            sLogger.error("An error occurred updating the Git repository from origin master", exception);
        }
    }

    private void doDockerShutdown() {
        final int countdownDuration = mProperties.getShutdownNoticeTime();
        final String containerName = mProperties.getDockerContainerName();

        executeRconCommand("say Modpack upgrade scheduled in "
                + countdownDuration
                + " seconds. Get to a stopping point!", containerName);
        executeRconCommand("say In ~5 minutes restart your client to pickup the changes.", containerName);

        sLogger.info("Shutting server down in " + countdownDuration + " seconds");
        for (int seconds = (countdownDuration - 1); seconds > 0; seconds--) {
            final String secondsString = (seconds == 1 ? " second" : " seconds");
            if (seconds % 5 == 0 || seconds <= 10) {
                executeRconCommand("say Shutdown in " + seconds + secondsString, containerName);
            }
            sLogger.info("Shutting server down in " + seconds + secondsString);
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException exception) {
                sLogger.warn("Shutdown loop was interrupted", exception);
            }
        }

        sLogger.info("Stopping Docker container");
        executeDockerCommand("stop", containerName);
    }

    private void doServerRootCleanup() {
        for (final String folder : mFoldersToUpdate) {
            final String pathToDelete = mServerRootPath + folder;
            try {
                Files.walk(Path.of(pathToDelete))
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                sLogger.info("Deleted \"" + pathToDelete + "\"");
            } catch (final IOException exception) {
                sLogger.error("Failed to delete \"" + pathToDelete + "\"");
            }
        }
    }

    private void doModUpdate() {
        final String manifestFilePath = mRepositoryPath + "manifest.json";
        final File modsFolder = new File(mServerRootPath + "mods" + File.separator);

        try {
            if (!modsFolder.exists()) {
                Files.createDirectory(modsFolder.toPath());
            }

            final ObjectMapper mapper = new ObjectMapper();
            final ModpackManifest modpackManifest = mapper.readValue(new File(manifestFilePath), ModpackManifest.class);
            String filePathString;
            int count = 1;
            final int total = modpackManifest.getModFiles().length;
            sLogger.info("Beginning download of " + total + " mods");
            for (final ModFile file : modpackManifest.getModFiles()) {
                filePathString = modsFolder + File.separator + file.getModFileName();
                try (final ReadableByteChannel readableByteChannel = Channels.newChannel(new URL(file.getModDownloadUrl()).openStream());
                     final FileOutputStream fileOutputStream = new FileOutputStream(filePathString)) {

                    sLogger.info("Downloading (" + (count++) + "/" + total + ") \"" + filePathString + "\"");
                    fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, file.getModFileLength());
                }
            }
            sLogger.info("Finished download of " + modpackManifest.getModFiles().length + " mods");
        } catch (final IOException exception) {
            sLogger.error("Failed to deserialize \"" + manifestFilePath + "\"", exception);
        }
    }

    private void doServerRootUpdates() {
        for (final String folder : mFoldersToUpdate) {
            if (folder.equals("mods")) {
                continue;
            }
            final String sourcePathString = mRepositoryPath + folder;
            final String destinationPathString = mServerRootPath + folder;
            try {
                final Path source = Paths.get(sourcePathString);
                final Path target = Paths.get(destinationPathString);

                Files.walkFileTree(source, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
                            throws IOException {

                        final Path path = target.resolve(source.relativize(dir));
                        if (!path.toFile().exists()) {
                            Files.createDirectory(path);
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                            throws IOException {
                        Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                        return FileVisitResult.CONTINUE;
                    }
                });
                sLogger.info("Copied \"" + sourcePathString + "\" to \"" + destinationPathString + "\"");
            } catch (final IOException exception) {
                sLogger.error("Failed to walk \"" + sourcePathString + "\"");
            }
        }
    }

    private void doUpdateServerProperties() {
        if (!mProperties.getSetMotd()) {
            return;
        }

        final String serverPropertiesFilePath = mServerRootPath + "server.properties";
        final Properties serverProperties = new Properties();

        // Load the properties from disk
        try (final InputStream inputStream = Files.newInputStream(Paths.get(serverPropertiesFilePath))) {
            serverProperties.load(inputStream);
        } catch (final IOException exception) {
            sLogger.error("Could not read \"" + serverPropertiesFilePath + "\"");
        }

        // Update the motd and save the properties to disk
        try (final OutputStream outputStream = Files.newOutputStream(Paths.get(serverPropertiesFilePath))) {
            final String manifestFilePath = mRepositoryPath + "manifest.json";

            try {
                final ObjectMapper mapper = new ObjectMapper();
                final ModpackManifest modpackManifest =
                        mapper.readValue(new File(manifestFilePath), ModpackManifest.class);
                final String motd = "\u00A7fModpack: \u00A72"
                        + modpackManifest.getModpackName()
                        + " \u00A74"
                        + modpackManifest.getModpackVersion();

                serverProperties.setProperty(
                        "motd",
                        motd);
                sLogger.info("Set the motd to \"" + motd + "\" in \"" + serverPropertiesFilePath + "\"");
            } catch (final IOException exception) {
                sLogger.error("Failed to deserialize \"" + manifestFilePath + "\"", exception);
            }

            serverProperties.store(outputStream, null);
        } catch (final IOException exception) {
            sLogger.error("Could not write \"" + serverPropertiesFilePath + "\"");
        }
    }

    private void doDockerStart() {
        sLogger.info("Starting Docker container");
        executeDockerCommand("start", mProperties.getDockerContainerName());
    }

    private void executeDockerCommand(final String... args) {
        try {
            final List<String> commandWithArgs = new ArrayList<>();
            commandWithArgs.add("docker");
            commandWithArgs.addAll(Arrays.asList(args));
            final ProcessBuilder processBuilder = new ProcessBuilder(commandWithArgs.toArray(new String[0]));
            final Process process = processBuilder.start();
            process.waitFor();
        } catch (final IOException | InterruptedException exception) {
            sLogger.error("An error occurred executing Docker command \"docker " + Arrays.toString(args) + "\"");
        }
    }

    private void executeRconCommand(final String command, final String containerName) {
        executeDockerCommand("exec", containerName, "rcon-cli", command);
    }
}
