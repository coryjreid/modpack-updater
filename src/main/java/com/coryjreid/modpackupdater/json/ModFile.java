package com.coryjreid.modpackupdater.json;

import java.lang.invoke.MethodHandles;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import checkers.igj.quals.Immutable;

@Immutable
@JsonDeserialize(builder = ModFile.Builder.class)
public class ModFile {
    private static final Logger sLogger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final int mModProjectId;
    private final String mModDisplayName;
    private final String mModFileName;
    private final String mModDownloadUrl;
    private final int mModFileLength;

    private ModFile(
            final int modProjectId,
            final String modDisplayName,
            final String modFileName,
            final String modDownloadUrl,
            final int modFileLength) {
        mModProjectId = modProjectId;
        mModDisplayName = modDisplayName;
        mModFileName = modFileName;
        mModDownloadUrl = modDownloadUrl;
        mModFileLength = modFileLength;
    }

    public int getModProjectId() {
        return mModProjectId;
    }

    public String getModDisplayName() {
        return mModDisplayName;
    }

    public String getModFileName() {
        return mModFileName;
    }

    public String getModDownloadUrl() {
        return mModDownloadUrl;
    }

    public int getModFileLength() {
        return mModFileLength;
    }

    @JsonPOJOBuilder
    @JsonIgnoreProperties(ignoreUnknown = true)
    static final class Builder {
        private int mModProjectId;
        private String mModDisplayName;
        private String mModFileName;
        private String mModDownloadUrl;
        private int mModFileLength;

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

        public ModFile build() {
            return new ModFile(mModProjectId, mModDisplayName, mModFileName, mModDownloadUrl, mModFileLength);
        }
    }
}
