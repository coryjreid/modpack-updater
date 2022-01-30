package com.coryjreid.modpackupdater.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import net.jcip.annotations.Immutable;

@Immutable
@JsonDeserialize(builder = DiscordWebhookMessage.Builder.class)
public class DiscordWebhookMessage {
    private final String mMessage;

    private DiscordWebhookMessage(final String message) {
        mMessage = message;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * {@code DiscordWebhookMessage} builder static inner class.
     */
    @JsonPOJOBuilder(withPrefix = "set")
    public static final class Builder {
        private String mMessage;

        private Builder() {
        }

        /**
         * Returns a {@code DiscordWebhookMessage} built from the parameters previously set.
         *
         * @return a {@code DiscordWebhookMessage} built with parameters of this {@code DiscordWebhookMessage.Builder}
         */
        public DiscordWebhookMessage build() {
            return new DiscordWebhookMessage(mMessage);
        }

        /**
         * Sets the {@code message} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code message} to set
         * @return a reference to this Builder
         */
        @JsonProperty("content")
        public Builder setMessage(final String val) {
            mMessage = val;
            return this;
        }
    }
}
