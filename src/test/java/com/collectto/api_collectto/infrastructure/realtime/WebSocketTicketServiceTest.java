package com.collectto.api_collectto.infrastructure.realtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class WebSocketTicketServiceTest {

    private static final class MutableClock extends Clock {
        Instant now = Instant.parse("2026-11-21T12:00:00Z");
        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    private final MutableClock clock = new MutableClock();
    private final WebSocketTicketService service = new WebSocketTicketService(clock);

    @Test
    void ticketIdentifiesTheUserOnlyOnce() {
        UUID user = UUID.randomUUID();
        String ticket = service.issue(user).ticket();

        assertThat(service.consume(ticket)).contains(user);
        assertThat(service.consume(ticket)).isEmpty(); // replay refused
    }

    @Test
    void expiredTicketIsRefused() {
        String ticket = service.issue(UUID.randomUUID()).ticket();
        clock.now = clock.now.plus(WebSocketTicketService.TTL);

        assertThat(service.consume(ticket)).isEmpty();
    }

    @Test
    void unknownOrMissingTicketIsRefused() {
        assertThat(service.consume(null)).isEmpty();
        assertThat(service.consume("")).isEmpty();
        assertThat(service.consume("made-up")).isEmpty();
    }

    @Test
    void ticketsAreUniqueAndUrlSafe() {
        String a = service.issue(UUID.randomUUID()).ticket();
        String b = service.issue(UUID.randomUUID()).ticket();
        assertThat(a).isNotEqualTo(b).matches("[A-Za-z0-9_-]{43}");
    }

    @Test
    void purgeRemovesOnlyExpiredTickets() {
        service.issue(UUID.randomUUID());
        clock.now = clock.now.plus(Duration.ofSeconds(20));
        String fresh = service.issue(UUID.randomUUID()).ticket();
        clock.now = clock.now.plus(Duration.ofSeconds(15)); // first one is now 35 s old

        service.purgeExpired();

        assertThat(service.size()).isEqualTo(1);
        assertThat(service.consume(fresh)).isPresent();
    }
}
