package com.collectto.api_collectto.presentation.controllers;

import java.time.Instant;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.collectto.api_collectto.infrastructure.config.WebSocketConfig;
import com.collectto.api_collectto.infrastructure.realtime.WebSocketTicketService;
import com.collectto.api_collectto.infrastructure.security.SecurityUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/realtime")
public final class RealtimeController {

    private final WebSocketTicketService ticketService;

    @Schema(name = "RealtimeTicketResponse", description = "One-time ticket to open the WebSocket")
    public record TicketResponse(
            @Schema(description = "Single-use ticket, valid for 30 seconds") String ticket,
            @Schema(description = "Ticket expiration") Instant expiresAt,
            @Schema(description = "Path to connect to, adding ?ticket=...", example = "/ws") String path) {}

    @PostMapping("/ticket")
    @Operation(summary = "Issue a WebSocket ticket", description = "Returns a single-use ticket valid for 30 seconds. Connect with wss://<api host>/ws?ticket=<ticket>. Ask for a new ticket on every (re)connection.")
    public ResponseEntity<TicketResponse> issueTicket(@AuthenticationPrincipal SecurityUserDetails userDetails) {
        UUID userId = userDetails.getUser().getId();
        WebSocketTicketService.Issued issued = ticketService.issue(userId);
        return ResponseEntity.ok(new TicketResponse(issued.ticket(), issued.expiresAt(), WebSocketConfig.WEBSOCKET_PATH));
    }
}
