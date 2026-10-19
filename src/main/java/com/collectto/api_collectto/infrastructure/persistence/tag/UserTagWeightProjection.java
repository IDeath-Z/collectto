package com.collectto.api_collectto.infrastructure.persistence.tag;

import java.util.UUID;

public interface UserTagWeightProjection {
    UUID getUserId();
    UUID getTagId();
    int getWeight();
}