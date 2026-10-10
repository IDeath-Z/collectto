package com.collectto.api_collectto.infrastructure.persistence.tag;

import java.util.UUID;

public interface TagWeightProjection {
    UUID getTagId();
    int getWeight();
}