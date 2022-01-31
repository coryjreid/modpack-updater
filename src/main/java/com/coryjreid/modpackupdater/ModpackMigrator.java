package com.coryjreid.modpackupdater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.coryjreid.modpackupdater.json.InstalledManifest;
import com.coryjreid.modpackupdater.json.Mod;
import com.coryjreid.modpackupdater.json.ModpackManifest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goebl.david.Webb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModpackMigrator {
    private static final Logger sLogger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * This is the manifest of mods provided by the modpack and is used to know which mods to install to the server.
     */
    private static final String MODPACK_MANIFEST_FILE_NAME = "manifest.json";

    /**
     * This is the manifest file this tool writes to the mods folder to track the versions of installed mods.
     */
    private static final String INSTALLED_MANIFEST_FILE_NAME = "installed_manifest.json";
    private static final String MODS_FOLDER = "mods";

    private final ModpackMigratorProperties mProperties;
    private final String mServerRootPath;
    private final String mRepositoryPath;
    private final Set<String> mFoldersToUpdate;

    private Path mModpackManifestFile;
    private Path mInstalledManifestFile;

    public ModpackMigrator(final ModpackMigratorProperties properties) {
        mProperties = properties;
        mServerRootPath = (mProperties.getServerRootPath().endsWith(File.separator)
                               ? mProperties.getServerRootPath()
                               : mProperties.getServerRootPath() + File.separator);
        mRepositoryPath = (mProperties.getSourceRepositoryPath().endsWith(File.separator)
                               ? mProperties.getSourceRepositoryPath()
                               : mProperties.getSourceRepositoryPath() + File.separator);
        mFoldersToUpdate = new HashSet<>(properties.getManagedFolders());
    }

    public final void doModpackUpdate() {
        doGitCheckout();
        verifyRequiredFilesExist();
        sLogger.info("Beginning modpack update");
        if (mProperties.isDiscordWebhookEnabled()) {
            postDiscordMessage(String.format(
                "%s Server shutting down for an update in %s seconds! Please wait...",
                mProperties.getDiscordMentionId().isEmpty() ? "" : "<@&" + mProperties.getDiscordMentionId() + ">",
                mProperties.getShutdownNoticeTime()));
        }
        doDockerShutdown();
        doServerRootCleanup();
        doServerRootUpdates();
        doServerConfigUpdate();
        doModUpdate();
        doUpdateServerProperties();
        doDockerStart();
        sLogger.info("Finished modpack update");
        if (mProperties.isDiscordWebhookEnabled()) {
            postDiscordMessage(String.format(
                "%s Server update complete! Please restart your clients to pickup the changes.",
                mProperties.getDiscordMentionId().isEmpty() ? "" : "<@&" + mProperties.getDiscordMentionId() + ">"));
        }
    }

    private void copyDirectoryContents(final String sourcePathString, final String destinationPathString) {
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
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
            sLogger.info("Copied \"" + sourcePathString + "\" to \"" + destinationPathString + "\"");
        } catch (final IOException exception) {
            sLogger.error("Failed to walk \"" + sourcePathString + "\"", exception);
        }
    }

    private void deleteModFiles(final Collection<Mod> mods) throws IOException {
        int count = 1;
        for (final Mod mod : mods) {
            final Path path = Paths.get(mProperties.getServerRootPath(), MODS_FOLDER, mod.getFileName());
            sLogger.info(String.format("Deleting (%s/%s) %s", count++, mods.size(), path));
            Files.delete(path);
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

    private void doDockerStart() {
        sLogger.info("Starting Docker container");
        executeDockerCommand("start", mProperties.getDockerContainerName());
    }

    private void doGitCheckout() {
        try {
            final ProcessBuilder processBuilder =
                new ProcessBuilder("git", "pull", "origin", mProperties.getGitBranchName());
            processBuilder.directory(new File(mRepositoryPath));
            final Process process = processBuilder.start();
            process.waitFor();
            sLogger.info("Updated the source repository");
        } catch (final IOException | InterruptedException exception) {
            sLogger.error("An error occurred updating the Git repository from origin master", exception);
        }
    }

    private void doModUpdate() {
        final Path modsFolder = Paths.get(mProperties.getServerRootPath(), MODS_FOLDER);

        try {
            if (Files.notExists(modsFolder)) {
                Files.createDirectory(modsFolder);
            }

            final ObjectMapper mapper = new ObjectMapper();
            final ModpackManifest modpackManifest =
                mapper.readValue(mModpackManifestFile.toFile(), ModpackManifest.class);

            final boolean isNewInstall = Files.notExists(mInstalledManifestFile);
            final InstalledManifest installedManifest = isNewInstall
                ? InstalledManifest.builder().setMods(modpackManifest.getModFiles()).build()
                : InstalledManifest.deserializeFromFile(mInstalledManifestFile.toFile());

            final List<Mod> toDownload = new ArrayList<>();
            final List<Mod> toReplace = new ArrayList<>();
            if (isNewInstall) {
                toDownload.addAll(installedManifest.getMods().values());
            } else {
                // Handle modpack removals
                if (installedManifest.getMods().size() > modpackManifest.getModFiles().size()) {
                    final List<Mod> modpackRemovals = new ArrayList<>(installedManifest.getMods().values());
                    modpackRemovals.removeAll(modpackManifest.getModFiles());
                    for (final Mod mod : modpackRemovals) {
                        installedManifest.getMods().remove(mod.getModId());
                        Files.delete(Paths.get(mProperties.getServerRootPath(), MODS_FOLDER, mod.getFileName()));
                    }
                }

                for (final Mod mod : modpackManifest.getModFiles()) {
                    final Map<Integer, Mod> currentlyInstalled = installedManifest.getMods();
                    if (!currentlyInstalled.containsKey(mod.getModId())
                        || currentlyInstalled.get(mod.getModId()).getFileId() != mod.getFileId()) {
                        toDownload.add(mod);
                    }
                    if (currentlyInstalled.containsKey(mod.getModId())
                        && currentlyInstalled.get(mod.getModId()).getFileId() != mod.getFileId()) {
                        toReplace.add(currentlyInstalled.get(mod.getModId()));
                    }
                }
            }

            deleteModFiles(toReplace);
            downloadModFiles(toDownload, modsFolder);

            toDownload.forEach(mod -> installedManifest.getMods().put(mod.getModId(), mod));

            InstalledManifest.writeToFile(mInstalledManifestFile.toFile(), installedManifest);
        } catch (final IOException exception) {
            sLogger.error("An error has occurred while performing the mod updates", exception);
            System.exit(1);
        }
    }

    private void doServerConfigUpdate() {
        final String pathToDelete =
            mServerRootPath + mProperties.getMinecraftWorldName() + File.separator + "serverconfig";
        try {
            Files.walk(Path.of(pathToDelete)).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            sLogger.info("Deleted \"" + pathToDelete + "\"");
        } catch (final IOException exception) {
            sLogger.error("Failed to delete \"" + pathToDelete + "\"");
        }

        copyDirectoryContents(
            mRepositoryPath + "defaultconfigs",
            mServerRootPath + File.separator + mProperties.getMinecraftWorldName() + File.separator + "serverconfig");
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

    private void doServerRootUpdates() {
        for (final String folder : mFoldersToUpdate) {
            if (folder.equals(MODS_FOLDER)) {
                continue;
            }
            copyDirectoryContents(mRepositoryPath + folder, mServerRootPath + folder);
        }
    }

    private void doUpdateServerProperties() {
        if (!mProperties.isSetMotd()) {
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

                serverProperties.setProperty("motd", motd);
                sLogger.info("Set the motd to \"" + motd + "\" in \"" + serverPropertiesFilePath + "\"");
            } catch (final IOException exception) {
                sLogger.error("Failed to deserialize \"" + manifestFilePath + "\"", exception);
            }

            serverProperties.store(outputStream, null);
        } catch (final IOException exception) {
            sLogger.error("Could not write \"" + serverPropertiesFilePath + "\"");
        }
    }

    private void downloadModFiles(final Collection<Mod> mods, final Path installLocation) throws IOException {
        sLogger.info("Beginning download of " + mods.size() + " mods");
        int downloaded = 1;
        for (final Mod mod : mods) {
            final Path modFilePath = installLocation.resolve(mod.getFileName());
            try (
                final InputStream inputStream = new URL(mod.getDownloadUrl()).openStream();
                final FileOutputStream outputStream = new FileOutputStream(modFilePath.toFile())) {

                sLogger.info("Downloading (" + (downloaded++) + "/" + mods.size() + ") \"" + modFilePath + "\"");
                final byte[] buffer = new byte[4096];
                int totalRead = 0;
                for (int length; (length = inputStream.read(buffer)) != -1; ) {
                    totalRead += length;
                    outputStream.write(buffer, 0, length);
                }
                if (totalRead == mod.getFileLength()) {
                    sLogger.info(String.format("Received %s/%s bytes", totalRead, mod.getFileLength()));
                } else {
                    throw new IOException(String.format(
                        "Only received %s/%s bytes for %s",
                        modFilePath,
                        totalRead,
                        mod.getFileLength()));
                }
            }
        }
        sLogger.info("Finished download of " + mods.size() + " mods");
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

    private void postDiscordMessage(final String message) {
        Webb.create()
            .post(mProperties.getDiscordWebhookUrl())
            .header("Content-Type", "application/json")
            .body(String.format("{\"content\":\"%s\"}", message))
            .asString()
            .ensureSuccess();
    }

    private void verifyRequiredFilesExist() {
        mModpackManifestFile = Paths.get(mProperties.getSourceRepositoryPath(), MODPACK_MANIFEST_FILE_NAME);
        mInstalledManifestFile = Paths.get(mProperties.getServerRootPath(), MODS_FOLDER, INSTALLED_MANIFEST_FILE_NAME);

        if (Files.notExists(mModpackManifestFile)) {
            sLogger.error(String.format(
                "The Modpack Manifest file '%s' does not exist; Shutting down",
                mModpackManifestFile));
            System.exit(1);
        }

        if (Files.notExists(mInstalledManifestFile)) {
            sLogger.warn(String.format(
                "The Installed Manifest file '%s' does not exist; It will be created",
                mInstalledManifestFile));
        }
    }
}
