package com.coryjreid.modpackupdater.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import checkers.igj.quals.Immutable;

@Immutable
@JsonDeserialize(builder = Mod.Builder.class)
public class Mod {
    private final int mModProjectId;
    private final String mModDisplayName;
    private final String mModFileName;
    private final String mModDownloadUrl;
    private final int mModFileLength;
    private final int mModFileId;

    private Mod(
        final int modProjectId,
        final String modDisplayName,
        final String modFileName,
        final String modDownloadUrl,
        final int modFileLength,
        final int modFileId) {
        mModProjectId = modProjectId;
        mModDisplayName = modDisplayName;
        mModFileName = modFileName;
        mModDownloadUrl = modDownloadUrl;
        mModFileLength = modFileLength;
        mModFileId = modFileId;
    }

    public int getModId() {
        return mModProjectId;
    }

    public String getDisplayName() {
        return mModDisplayName;
    }

    public String getFileName() {
        return mModFileName;
    }

    public String getDownloadUrl() {
        return mModDownloadUrl;
    }

    public int getFileLength() {
        return mModFileLength;
    }

    public int getFileId() {
        return mModFileId;
    }

    @JsonPOJOBuilder
    static final class Builder {
        private int mModProjectId;
        private String mModDisplayName;
        private String mModFileName;
        private String mModDownloadUrl;
        private int mModFileLength;
        private int mModFileId;

        @JsonProperty("modId")
        public Builder setModProjectId(final int modProjectId) {
            mModProjectId = modProjectId;
            return this;
        }

        @JsonProperty("displayName")
        public Builder setModDisplayName(final String modDisplayName) {
            mModDisplayName = modDisplayName;
            return this;
        }

        @JsonProperty("fileName")
        public Builder setModFileName(final String modFileName) {
            mModFileName = modFileName;
            return this;
        }

        @JsonProperty("downloadUrl")
        public Builder setModDownloadUrl(final String modDownloadUrl) {
            mModDownloadUrl = modDownloadUrl;
            return this;
        }

        @JsonProperty("fileLength")
        public Builder setModFileLength(final int modFileLength) {
            mModFileLength = modFileLength;
            return this;
        }

        @JsonProperty("fileId")
        public Builder setModFileId(final int modFileId) {
            mModFileId = modFileId;
            return this;
        }

        public Mod build() {
            return new Mod(
                mModProjectId,
                mModDisplayName,
                mModFileName,
                mModDownloadUrl,
                mModFileLength,
                mModFileId);
        }
    }
}
