package com.coryjreid.modpackupdater.json;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.CurseFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import checkers.igj.quals.Immutable;

@Immutable
@JsonDeserialize(builder = ModFile.Builder.class)
public class ModFile {
    private static final Logger sLogger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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

    public CurseFile getCurseFile() throws CurseException {
        try {
            final Optional<CurseFile> file = CurseAPI.file(mModProjectId, mModFileId);
            if (file.isPresent()) {
                return file.get();
            } else {
                throw new CurseException("CurseFile is absent");
            }
        } catch (final CurseException exception) {
            sLogger.error(
                "CurseFile could not be obtained for \"mod " + mModProjectId + "\" file " + mModFileId + "\"",
                exception);
            throw exception;
        }
    }

    @JsonPOJOBuilder
    @JsonIgnoreProperties(ignoreUnknown = true)
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
