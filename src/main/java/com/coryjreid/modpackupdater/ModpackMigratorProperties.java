package com.coryjreid.modpackupdater;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import checkers.igj.quals.Immutable;

@Immutable
public class ModpackMigratorProperties {
    public static final Collection<String> MANAGED_MINECRAFT_FOLDERS =
        new HashSet<>(Arrays.asList("config", "defaultconfigs", "kubejs"));

    private static final String KEY_PATHS_SOURCE_REPOSITORY = "paths.sourceRepository";
    private static final String KEY_PATHS_SERVER_ROOT = "paths.serverRoot";
    private static final String KEY_PATHS_DOCKER_CONTAINER = "docker.containerName";
    private static final String KEY_MIGRATOR_SHUTDOWN_TIME = "migrator.shutdownWarningNoticeTime";
    private static final String KEY_MINECRAFT_SET_MOTD = "minecraft.setMotd";
    private static final String KEY_MINECRAFT_EXTRA_FOLDERS = "minecraft.extraFolders";
    private static final String KEY_GIT_BRANCH_NAME = "git.branchName";
    private static final String EXTRA_FOLDER_SEPARATOR = ",";

    private final Properties mProperties;
    private final int mDefaultShutdownNoticeTime = 30;
    private final boolean mDefaultSetMotd = true;
    private final String mDefaultBranchName = "master";

    public ModpackMigratorProperties(final Properties properties) {
        mProperties = properties;
    }

    public String getSourceRepositoryPath() {
        return mProperties.getProperty(KEY_PATHS_SOURCE_REPOSITORY);
    }

    public String getServerRootPath() {
        return mProperties.getProperty(KEY_PATHS_SERVER_ROOT);
    }

    public String getDockerContainerName() {
        return mProperties.getProperty(KEY_PATHS_DOCKER_CONTAINER);
    }

    public int getShutdownNoticeTime() {
        return Integer.parseInt(mProperties.getProperty(
            KEY_MIGRATOR_SHUTDOWN_TIME,
            Integer.toString(mDefaultShutdownNoticeTime)));
    }

    public boolean getSetMotd() {
        return Boolean.parseBoolean(mProperties.getProperty(KEY_MINECRAFT_SET_MOTD, Boolean.toString(mDefaultSetMotd)));
    }

    public String getGitBranchName() {
        return mProperties.getProperty(KEY_GIT_BRANCH_NAME, mDefaultBranchName);
    }

    public Collection<String> getFolders() {
        final Set<String> allFolders = new HashSet<>(MANAGED_MINECRAFT_FOLDERS);
        allFolders.addAll(Arrays.asList(mProperties.getProperty(
                KEY_MINECRAFT_EXTRA_FOLDERS,
                generateFolderString(MANAGED_MINECRAFT_FOLDERS.toArray(new String[] {})))
            .split(EXTRA_FOLDER_SEPARATOR)));
        return allFolders;
    }

    private static String generateFolderString(final String... folders) {
        return Arrays.stream(folders).map(String::toString).collect(Collectors.joining(EXTRA_FOLDER_SEPARATOR));
    }
}
