package com.collectto.api_collectto.infrastructure.realtime;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Short-lived, single-use tickets that authenticate the WebSocket handshake.
// Browsers cannot send an Authorization header on a WebSocket, and putting the JWT itself in the URL
// would leak a 15-minute credential into proxy logs. A ticket lives 30 seconds and dies on first use.
// In-memory: valid while the API runs as a single instance (compose.yaml). With more instances, move to Redis.
@Component
public class WebSocketTicketService {

    static final Duration TTL = Duration.ofSeconds(30);
    private static final int TICKET_BYTES = 32;

    private record Entry(UUID userId, Instant expiresAt) {}
    public record Issued(String ticket, Instant expiresAt) {}

    private final Map<String, Entry> tickets = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final Clock clock;

    public WebSocketTicketService() {
        this(Clock.systemUTC());
    }

    WebSocketTicketService(Clock clock) {
        this.clock = clock;
    }

    public Issued issue(UUID userId) {
        byte[] bytes = new byte[TICKET_BYTES];
        random.nextBytes(bytes);
        String ticket = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expiresAt = clock.instant().plus(TTL);
        tickets.put(ticket, new Entry(userId, expiresAt));
        return new Issued(ticket, expiresAt);
    }

    public Optional<UUID> consume(String ticket) {
        if (ticket == null || ticket.isBlank())
            return Optional.empty();

        Entry entry = tickets.remove(ticket);
        if (entry == null || !clock.instant().isBefore(entry.expiresAt()))
            return Optional.empty();

        return Optional.of(entry.userId());
    }

    @Scheduled(fixedDelay = 60_000)
    void purgeExpired() {
        Instant now = clock.instant();
        tickets.values().removeIf(entry -> !now.isBefore(entry.expiresAt()));
    }

    int size() {
        return tickets.size();
    }
}
