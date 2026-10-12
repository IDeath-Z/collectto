package com.collectto.api_collectto.infrastructure.persistence.chat;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class ChatJpaId implements Serializable {

    @Column(name = "lower_user_id")
    private UUID lowerUserId;

    @Column(name = "higher_user_id")
    private UUID higherUserId;
}
