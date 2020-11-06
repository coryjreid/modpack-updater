package com.coryjreid.modpackupdater;

import java.util.Properties;

import checkers.igj.quals.Immutable;

@Immutable
public class ModpackMigratorProperties {

    private static final String KEY_PATHS_SOURCE_REPOSITORY = "paths.sourceRepository";
    private static final String KEY_PATHS_SERVER_ROOT = "paths.serverRoot";
    private static final String KEY_PATHS_DOCKER_CONTAINER = "docker.containerName";
    private static final String KEY_MIGRATOR_SHUTDOWN_TIME = "migrator.shutdownWarningNoticeTime";

    private final Properties mProperties;
    private final int mDefaultShutdownNoticeTime = 30;

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
        try {
            return Integer.parseInt(mProperties.getProperty(KEY_MIGRATOR_SHUTDOWN_TIME));
        } catch (final NumberFormatException ignoredException) {
            return mDefaultShutdownNoticeTime;
        }
    }
}
