package com.coryjreid.modpackupdater.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import checkers.igj.quals.Immutable;

@Immutable
@JsonDeserialize(builder = ModFile.Builder.class)
public class ModFile {
    private final int mModProjectId;
    private final int mModFileId;
    private final boolean mIsFileRequired;

    private ModFile(final int modProjectId, final int modFileId, final boolean isFileRequired) {
        mModProjectId = modProjectId;
        mModFileId = modFileId;
        mIsFileRequired = isFileRequired;
    }

    public int getModProjectId() {
        return mModProjectId;
    }

    public int getModFileId() {
        return mModFileId;
    }

    public boolean isFileRequired() {
        return mIsFileRequired;
    }

    @JsonPOJOBuilder
    static final class Builder {
        private int mModProjectId;
        private int mModFileId;
        private boolean mIsFileRequired;

        @JsonProperty("projectID")
        public Builder setModProjectId(final int modProjectId) {
            mModProjectId = modProjectId;
            return this;
        }

        @JsonProperty("fileID")
        public Builder setModFileId(final int modFileId) {
            mModFileId = modFileId;
            return this;
        }

        @JsonProperty("required")
        public Builder setIsFileRequired(final boolean isFileRequired) {
            mIsFileRequired = isFileRequired;
            return this;
        }

        public ModFile build() {
            return new ModFile(mModProjectId, mModFileId, mIsFileRequired);
        }
    }
}
