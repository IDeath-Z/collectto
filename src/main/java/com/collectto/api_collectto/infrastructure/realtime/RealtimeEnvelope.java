package com.collectto.api_collectto.infrastructure.realtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

// Every frame the server sends: {"type": "...", "data": {...}}
public record RealtimeEnvelope(String type, Object data) {

    static String json(ObjectMapper objectMapper, String type, Object data) {
        try {
            return objectMapper.writeValueAsString(new RealtimeEnvelope(type, data));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize realtime event " + type, e);
        }
    }
}
