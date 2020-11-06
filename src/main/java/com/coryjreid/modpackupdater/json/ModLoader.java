package com.coryjreid.modpackupdater.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import checkers.igj.quals.Immutable;

@Immutable
@JsonDeserialize(builder = ModLoader.Builder.class)
public class ModLoader {
    private final String mModLoaderId;
    private final boolean mIsPrimaryModLoader;

    private ModLoader(final String modLoaderId, final boolean isPrimaryModLoader) {
        mModLoaderId = modLoaderId;
        mIsPrimaryModLoader = isPrimaryModLoader;
    }

    public String getModLoaderId() {
        return mModLoaderId;
    }

    public boolean isPrimaryModLoader() {
        return mIsPrimaryModLoader;
    }

    @JsonPOJOBuilder(withPrefix = "set")
    static final class Builder {
        private String mModLoaderId;
        private boolean mIsPrimaryModLoader;

        @JsonProperty("id")
        public Builder setModLoaderId(final String modLoaderId) {
            mModLoaderId = modLoaderId;
            return this;
        }

        @JsonProperty("primary")
        public Builder setIsPrimaryModLoader(final boolean isPrimaryModLoader) {
            mIsPrimaryModLoader = isPrimaryModLoader;
            return this;
        }

        public ModLoader build() {
            return new ModLoader(mModLoaderId, mIsPrimaryModLoader);
        }
    }
}
