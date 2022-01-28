package com.coryjreid.modpackupdater;

import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

import checkers.igj.quals.Immutable;

@Immutable
public class ModpackMigratorProperties {
    private static final String KEY_PATHS_SOURCE_REPOSITORY = "paths.sourceRepository";
    private static final String KEY_PATHS_SERVER_ROOT = "paths.serverRoot";
    private static final String KEY_PATHS_DOCKER_CONTAINER = "docker.containerName";
    private static final String KEY_MIGRATOR_SHUTDOWN_TIME = "migrator.shutdownWarningNoticeTime";
    private static final String KEY_MIGRATOR_SET_MOTD = "migrator.setMotd";
    private static final String KEY_GIT_BRANCH_NAME = "git.branchName";
    private static final String KEY_FOLDERS = "folders";
    private static final String FOLDER_SEPARATOR = ";";

    private final Properties mProperties;
    private final int mDefaultShutdownNoticeTime = 30;
    private final boolean mDefaultSetMotd = true;
    private final String mDefaultBranchName = "master";
    private final String mDefaultFolders = generateFolderString("config", "defaultconfigs", "mods");

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
        return Boolean.parseBoolean(mProperties.getProperty(KEY_MIGRATOR_SET_MOTD, Boolean.toString(mDefaultSetMotd)));
    }

    public String getGitBranchName() {
        return mProperties.getProperty(KEY_GIT_BRANCH_NAME, mDefaultBranchName);
    }

    public String[] getFolders() {
        return mProperties.getProperty(KEY_FOLDERS, mDefaultFolders).split(";");
    }

    private static String generateFolderString(final String... folders) {
        return Arrays.stream(folders).map(String::toString).collect(Collectors.joining(FOLDER_SEPARATOR));
    }
}
