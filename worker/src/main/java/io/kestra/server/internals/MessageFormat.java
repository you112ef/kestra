package io.kestra.server.internals;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.protobuf.ByteString;
import io.kestra.core.utils.Enums;

/**
 * Represents a specific message format.
 * <p>
 * Each gRPC message contain a generic byte array `message` field.
 */
public interface MessageFormat {
    
    <T> T fromByteString(ByteString data, Class<T> type);
    
    <T> T fromByteString(ByteString data, TypeReference<T> type);
    
    ByteString toByteString(Object value);
    
    static MessageFormat resolve(final String format) {
        try {
            return Enums.getForNameIgnoreCase(format, MessageFormats.class);
        } catch (IllegalArgumentException e) {
            return MessageFormats.JSON; // default
        }
    }
}
