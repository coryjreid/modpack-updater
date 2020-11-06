package com.coryjreid.modpackupdater.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import checkers.igj.quals.Immutable;

@Immutable
@JsonDeserialize(builder = Minecraft.Builder.class)
public class Minecraft {
    private final String mMinecraftVersion;
    private final ModLoader[] mModLoaders;

    private Minecraft(final String minecraftVersion, final ModLoader[] modLoaders) {
        mMinecraftVersion = minecraftVersion;
        mModLoaders = modLoaders;
    }

    public String getMinecraftVersion() {
        return mMinecraftVersion;
    }

    public ModLoader[] getModLoaders() {
        return mModLoaders;
    }

    @JsonPOJOBuilder(withPrefix = "set")
    static final class Builder {
        private String mMinecraftVersion;
        private ModLoader[] mModLoaders;

        @JsonProperty("version")
        public Builder setMinecraftVersion(final String minecraftVersion) {
            mMinecraftVersion = minecraftVersion;
            return this;
        }

        public Builder setModLoaders(final ModLoader[] modLoaders) {
            mModLoaders = modLoaders;
            return this;
        }

        public Minecraft build() {
            return new Minecraft(mMinecraftVersion, mModLoaders);
        }
    }
}
