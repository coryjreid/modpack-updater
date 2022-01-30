package com.coryjreid.modpackupdater.json;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import checkers.igj.quals.Immutable;

@Immutable
@JsonDeserialize(builder = InstalledManifest.Builder.class)
public class InstalledManifest {
    private final Map<Integer, Mod> mMods;

    private InstalledManifest(final Map<Integer, Mod> mods) {
        mMods = mods;
    }

    public Map<Integer, Mod> getMods() {
        return mMods;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static void writeToFile(final File file, final InstalledManifest manifest) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        writer.writeValue(file, manifest);
    }

    public static InstalledManifest deserializeFromFile(final File file) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(file, InstalledManifest.class);
    }

    @JsonPOJOBuilder(withPrefix = "set")
    public static final class Builder {
        private Map<Integer, Mod> mMods;

        @JsonProperty("mods")
        public Builder setMods(final Map<Integer, Mod> mods) {
            mMods = mods;
            return this;
        }

        public Builder setMods(final Collection<Mod> mods) {
            mMods = mods.stream().collect(Collectors.<Mod, Integer, Mod>toMap((Mod::getModId), Function.identity()));
            return this;
        }

        public InstalledManifest build() {
            return new InstalledManifest(mMods);
        }
    }
}
