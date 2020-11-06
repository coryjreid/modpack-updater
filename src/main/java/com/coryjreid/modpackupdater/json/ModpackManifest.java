package com.coryjreid.modpackupdater.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import checkers.igj.quals.Immutable;

@Immutable
@JsonDeserialize(builder = ModpackManifest.Builder.class)
public class ModpackManifest {
    private final Minecraft mMinecraft;
    private final String mManifestType;
    private final int mManifestVersion;
    private final String mModpackName;
    private final String mModpackVersion;
    private final String mModpackAuthor;
    private final int mModpackProjectId;
    private final ModFile[] mModFiles;

    private ModpackManifest(
        final Minecraft minecraft,
        final String manifestType,
        final int manifestVersion,
        final String modpackName,
        final String modpackVersion,
        final String modpackAuthor,
        final int modpackProjectId,
        final ModFile[] modFiles) {

        mMinecraft = minecraft;
        mManifestType = manifestType;
        mManifestVersion = manifestVersion;
        mModpackName = modpackName;
        mModpackVersion = modpackVersion;
        mModpackAuthor = modpackAuthor;
        mModpackProjectId = modpackProjectId;
        mModFiles = modFiles;
    }

    public Minecraft getMinecraft() {
        return mMinecraft;
    }

    public String getManifestType() {
        return mManifestType;
    }

    public int getManifestVersion() {
        return mManifestVersion;
    }

    public String getModpackName() {
        return mModpackName;
    }

    public String getModpackVersion() {
        return mModpackVersion;
    }

    public String getModpackAuthor() {
        return mModpackAuthor;
    }

    public int getModpackProjectId() {
        return mModpackProjectId;
    }

    public ModFile[] getModFiles() {
        return mModFiles;
    }

    @JsonPOJOBuilder(withPrefix = "set")
    @JsonIgnoreProperties(ignoreUnknown = true)
    static final class Builder {
        private Minecraft mMinecraft;
        private String mManifestType;
        private int mManifestVersion;
        private String mModpackName;
        private String mModpackVersion;
        private String mModpackAuthor;
        private int mModpackProjectId;
        private ModFile[] mModFiles;

        public Builder setMinecraft(final Minecraft minecraft) {
            mMinecraft = minecraft;
            return this;
        }

        public Builder setManifestType(final String manifestType) {
            mManifestType = manifestType;
            return this;
        }

        public Builder setManifestVersion(final int manifestVersion) {
            mManifestVersion = manifestVersion;
            return this;
        }

        @JsonProperty("name")
        public Builder setModpackName(final String modpackName) {
            mModpackName = modpackName;
            return this;
        }

        @JsonProperty("version")
        public Builder setModpackVersion(final String modpackVersion) {
            mModpackVersion = modpackVersion;
            return this;
        }

        @JsonProperty("author")
        public Builder setModpackAuthor(final String modpackAuthor) {
            mModpackAuthor = modpackAuthor;
            return this;
        }

        @JsonProperty("projectID")
        public Builder setModpackProjectId(final int modpackProjectId) {
            mModpackProjectId = modpackProjectId;
            return this;
        }

        @JsonProperty("files")
        public Builder setModpackFiles(final ModFile[] modpackFiles) {
            mModFiles = modpackFiles;
            return this;
        }

        public ModpackManifest build() {
            return new ModpackManifest(
                mMinecraft,
                mManifestType,
                mManifestVersion,
                mModpackName,
                mModpackVersion,
                mModpackAuthor,
                mModpackProjectId,
                mModFiles);
        }
    }
}
